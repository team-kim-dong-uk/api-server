package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
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
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
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
    private final FeedRepository feedRepository;


    /**
     * user userId saves photo photoId
     *
     * @param userId  the user id
     * @param feedId the feed id
     * @return the album detail dto
     * @throws PhotoNotFoundException the photo not found exception
     */
    public AlbumDetailDto saveAlbum(String userId, String feedId) throws PhotoNotFoundException {
        if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
            throw new IllegalArgumentException("userId cannot be converted to ObjectId. userId: " + userId);

        if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
            throw new IllegalArgumentException("feedId cannot be converted to ObjectId. feedId: " + feedId);

        ObjectId userObjectId = new ObjectId(userId);
        ObjectId feedObjectId = new ObjectId(feedId);

        Optional<Album> existingAlbum = albumRepository.findByUserIdAndFeedId(userObjectId, feedObjectId);
        if (existingAlbum.isPresent()){
            throw new DuplicateKeyException("이미 가지고 있는 사진입니다.");
        }

        Feed feed = feedRepository.findById(feedObjectId)
            .orElseThrow(() -> new PhotoNotFoundException(feedObjectId));

        Album albumTobeSaved = Album.builder()
                .userId(userObjectId)
                .feedId(feedObjectId)
                .thumbnailLink(feed.getPhoto().getThumbnailLink())
                .lastViewed(new Date())
                .tags(feed.getPhoto().getTags())
                .build();

        Album album = albumRepository.insert(albumTobeSaved);

        return toAlbumDetailDto(album, feed.getPhoto());
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
        return albumRepository.findByUserIdAndFeedId(new ObjectId(userId), objectPhotoId)
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
        Feed feed = feedRepository.findById(album.getFeedId())
                .orElseThrow(() -> new AlbumNotFoundException(albumObjectId));
        Photo photo = photoRepository.findById(album.getFeedId())
                .orElseThrow(() -> new PhotoNotFoundException(album.getFeedId()));

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
     * @param feedId the feed id
     * @throws AlbumNotFoundException the album not found exception
     */
    public void deleteAlbum(String userId, String feedId) throws AlbumNotFoundException{
        ObjectId userObjectId = new ObjectId(userId);
        ObjectId feedObjectId = new ObjectId(feedId);

        Album album = albumRepository.findByUserIdAndFeedId(userObjectId, feedObjectId)
                .orElseThrow(() -> new AlbumNotFoundException(feedObjectId));

        album.setDeleted(true);
        albumRepository.save(album);
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
                .photoId(album.getFeedId().toString())
                .thumbnailLink(album.getThumbnailLink())
                .build();
    }

    public List<Album> findAllByUserIdAndPhotoIdIn(String userId, List<ObjectId> searchQuery) {
        return albumRepository.findAllByUserIdAndFeedIdIn(new ObjectId(userId), searchQuery);
    }

    public List<Album> findAllByUserId(String userId, Pageable pageable) {
        return albumRepository.findAllByUserId(new ObjectId(userId), pageable);
    }
}
