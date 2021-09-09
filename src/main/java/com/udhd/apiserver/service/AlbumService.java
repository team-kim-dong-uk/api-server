package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.exception.album.AlbumNotFoundException;
import com.udhd.apiserver.exception.photo.PhotoNotFoundException;
import com.udhd.apiserver.web.dto.album.AlbumDetailDto;
import com.udhd.apiserver.web.dto.album.AlbumOutlineDto;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import pics.udhd.kafka.QueryCommander;
import pics.udhd.kafka.dto.PhotoDto;
import pics.udhd.kafka.dto.QueryResultDto;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final QueryCommander queryCommander;


    /**
     * user userId saves photo photoId
     *
     * @param userId  the user id
     * @param photoId the photo id
     * @return the album detail dto
     * @throws PhotoNotFoundException the photo not found exception
     */
    public AlbumDetailDto saveAlbum(String userId, String photoId) throws PhotoNotFoundException {
        ObjectId userObjectId = new ObjectId(userId);
        ObjectId photoObjectId = new ObjectId(photoId);

        Optional<Album> existingAlbum = albumRepository.findByUserIdAndPhotoId(userObjectId, photoObjectId);
        if (existingAlbum.isPresent()){
            throw new DuplicateKeyException("이미 가지고 있는 사진입니다.");
        }

        Photo photo = photoRepository.findById(photoObjectId)
                .orElseThrow(() -> new PhotoNotFoundException(photoObjectId));

        Album albumTobeSaved = Album.builder()
                .userId(userObjectId)
                .photoId(photoObjectId)
                .thumbnailLink(photo.getThumbnailLink())
                .lastViewed(new Date())
                .tags(photo.getTags())
                .build();

        Album album = albumRepository.insert(albumTobeSaved);

        return toAlbumDetailDto(album, photo);
    }


    /**
     * Find albums list.
     *
     * @param userId the user id
     * @param tags   the tags
     * @return the list
     */
    public List<AlbumOutlineDto> findAlbums(String userId, List<String> tags, String findAfterId, int fetchSize) {
        ObjectId userObjectId = new ObjectId(userId);

        List<Album> albums;
        if (findAfterId == null) {
            if (tags.size() == 0) {
                albums = albumRepository.findAllByUserId(userObjectId);
            } else {
                albums = albumRepository.findAllByUserIdAndTagsIn(userObjectId, tags);
            }
        } else {
            ObjectId findAfterObjectId = new ObjectId(findAfterId);
            if (tags.size() == 0) {
                albums = albumRepository.findAllByUserIdAndIdAfter(userObjectId, findAfterObjectId);
            } else {
                albums = albumRepository.findAllByUserIdAndTagsInAndIdAfter(userObjectId, tags, findAfterObjectId);
            }
        }

        return albums.stream().limit(fetchSize)
                .map(album -> toAlbumOutlineDto(album))
                .collect(Collectors.toList());
    }

    /**
     * Find albums detail.
     * userId, photoId로 앨범 데이터 하나 가져오기
     *
     * @param userId the user id
     * @param photoId  the photo Id
     * @return the album data
     */
    public Album getAlbumDetail(String userId, String photoId){
        ObjectId objectPhotoId = new ObjectId(photoId);
        return albumRepository.findByUserIdAndPhotoId(new ObjectId(userId), objectPhotoId)
                .orElseThrow(() -> new AlbumNotFoundException(objectPhotoId));
    }

    /**
     * 저장한 사진의 태그 정보 변경.
     * userId가 업로드한 사진일 경우, 다른 유저들의 검색 결과에도 반영된다.
     *
     * @param userId  the user id
     * @param albumId the album id
     * @param tags    the tags
     * @return the album detail dto
     * @throws AlbumNotFoundException the album not found exception
     * @throws PhotoNotFoundException the photo not found exception
     */
    public AlbumDetailDto updateAlbumTags(String userId, String albumId, List<String> tags)
            throws AlbumNotFoundException, PhotoNotFoundException{
        ObjectId albumObjectId = new ObjectId(albumId);

        Album album = albumRepository.findById(albumObjectId)
                .orElseThrow(() -> new AlbumNotFoundException(albumObjectId));
        Photo photo = photoRepository.findById(album.getPhotoId())
                .orElseThrow(() -> new PhotoNotFoundException(album.getPhotoId()));

        album.setTags(tags);
        albumRepository.save(album);

        // userId가 해당 사진의 uploader인 경우, photo collection에서도 태그를 수정한다
        if (userId.equals(photo.getUploaderId().toString())) {
            photo.setTags(tags);
            photoRepository.save(photo);
        }
        return toAlbumDetailDto(album, photo);
    }


    /**
     * Delete album.
     *
     * @param userId  the user id
     * @param albumId the album id
     * @throws AlbumNotFoundException the album not found exception
     */
    public void deleteAlbum(String userId, String albumId) throws AlbumNotFoundException{
        ObjectId albumObjectId = new ObjectId(albumId);

        Album album = albumRepository.findById(albumObjectId)
                .orElseThrow(() -> new AlbumNotFoundException(albumObjectId));

        albumRepository.deleteById(albumObjectId);
    }

    private AlbumDetailDto toAlbumDetailDto(Album album, Photo photo) {
        return AlbumDetailDto.builder()
                .albumId(album.getId().toString())
                .photoId(photo.getId().toString())
                .uploaderId(photo.getUploaderId().toString())
                .uploaderNickname("가짜닉")    // TODO
                .originalLink(photo.getOriginalLink())
                .savedAt(album.getId().getDate())
                .tags(album.getTags())
                .build();
    }

    private AlbumOutlineDto toAlbumOutlineDto(Album album) {
        return AlbumOutlineDto.builder()
                .albumId(album.getId().toString())
                .photoId(album.getPhotoId().toString())
                .thumbnailLink(album.getThumbnailLink())
                .build();
    }

    public List<String> remainNotOwned(String userIdStr, List<String> photoIds) {
      /* 모든 값이 일단 가지고 있지 않다고 가정한다. */
      Set<String> retval = new HashSet<>();
      retval.addAll(photoIds);

      /* 비슷한 이미지 photoId를 모두 가져온다. */
      Map<String, List<String>> searched = searchSimilarPhotos(photoIds);

      /* 역으로 참조해야하기 때문에 이를 위한 Mapping Table을 만든다. */
      Map<ObjectId, String> reverseMap = new HashMap<>();
      List<ObjectId> searchQuery = new ArrayList<>();

      searched.forEach((key, value) -> {
        value.forEach((elem) -> {
            ObjectId e = new ObjectId(elem);
            reverseMap.put(e, key);
            searchQuery.add(e);
        });
      });

      ObjectId userId = new ObjectId(userIdStr);
      List<Album> alreadyHas = albumRepository.findAllByUserIdAndPhotoIdIn(userId, searchQuery);
      for (Album album: alreadyHas) {
          String containedPhotoId = reverseMap.get(album.getPhotoId());
          /* Don't have to check that it contains photoId. remove() is ignore it. */
          retval.remove(containedPhotoId);
      }

      return new ArrayList<>(retval);
    }

    public List<String> searchSimilarPhotoNoOwned(String userIdStr, String photoId) {
        /* 비슷한 이미지 photoId를 모두 가져온다. */
        List<String> similarPhotoIds = searchSimilarPhoto(photoId);
        ObjectId userId = new ObjectId(userIdStr);
        List<ObjectId> searchQuery = new ArrayList<>();

        List<Album> alreadyHas = albumRepository.findAllByUserIdAndPhotoIdIn(userId, searchQuery);
        Set<String> retval = new HashSet<>(similarPhotoIds);
        for (Album album : alreadyHas) {
            retval.remove(album.getPhotoId().toHexString());
        }
        return new ArrayList<>(retval);
    }

    public List<String> searchSimilarPhoto(String photoId) {
        Map<String, List<String>> searched = searchSimilarPhotos(Collections.singletonList(photoId));
        return searched.get(photoId);
    }

    public List<String> searchSimilarPhoto(Photo photo) {
        return searchSimilarPhoto(photo.getId().toHexString());
    }

    public Map<String, List<String>> searchSimilarPhotos(List<String> photos) {
        QueryResultDto searched = queryCommander.search(photos.stream()
            .map(AlbumService::toPhotoDto)
            .collect(Collectors.toList()));

        if (searched == null) {
            Map<String, List<String>> retval = new HashMap<>();
            photos.forEach(photoId -> {
                retval.put(photoId, Collections.singletonList(photoId));
            });
            return retval;
        }
        Map<String, List<String>> retval = new HashMap<>();
        searched.getValue().forEach((key, value) -> {
            // TODO : Photo 객체를 키로 재활용
            // TODO : 지금은 그냥 객체 탐색해서 일일이 비교 연산하지만, 이럴게 아니라 다른 방식으로 조회해야함.
            List<String> matched = photos.stream().filter(photo-> photo.equals(key)).collect(Collectors.toList());
            if (matched.isEmpty())
                return;
            // Photo
            // PhotoId만 가지고 있는 객체에서 변경
            retval.put(matched.get(0), value);
        });

        return retval;
    }

    private static PhotoDto toPhotoDto(String photoId) {
        PhotoDto photoDto = new PhotoDto();
        photoDto.setPhotoId(photoId);
        photoDto.setUrl(photoDto.getUrl());
        return photoDto;
    }
}
