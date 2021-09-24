package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import pics.udhd.kafka.QueryCommander;
import pics.udhd.kafka.dto.PhotoDto;
import pics.udhd.kafka.dto.QueryResultDto;

@RequiredArgsConstructor
@Service
public class SearchService {

  private static final int SEARCH_RECOMMEND_TAG_COUNT = 10;
  private static final int SEARCH_RECOMMEND_USER_COUNT = 3;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final AlbumService albumService;
  private final QueryCommander queryCommander;

  private static PhotoDto toPhotoDto(Photo photo) {
    return PhotoDto.builder()
        .photoId(photo.getId().toHexString())
        .url(photo.getOriginalLink())
        .build();
  }

  private static PhotoDto toPhotoDto(String photoId) {
    return PhotoDto.builder()
        .photoId(photoId)
        .build();
  }

  public List<SearchCandidateDto> getRecommendedKeywords(String keyword) {
    List<Tag> tagStartsWith = tagRepository.findTagsByTagStartingWith(keyword);
    List<Tag> tagContains = tagRepository.findTagsByTagContaining(keyword);

    List<User> userStartsWith = userRepository.findUsersByNicknameStartingWith(keyword);
    List<User> userContains = userRepository.findUsersByNicknameContaining(keyword);

    Stream<SearchCandidateDto> tags
        = Stream
        .concat(tagStartsWith.stream(), tagContains.stream())
        .distinct()
        .limit(SEARCH_RECOMMEND_TAG_COUNT)
        .map(tag -> SearchCandidateDto.fromTag(tag));

    Stream<SearchCandidateDto> users
        = Stream
        .concat(userStartsWith.stream(), userContains.stream())
        .distinct()
        .limit(SEARCH_RECOMMEND_USER_COUNT)
        .map(user -> SearchCandidateDto.fromUser(user));

    return Stream.concat(tags, users).collect(Collectors.toList());
  }

  public void registerPhoto(Photo photo) {
    queryCommander.insert(toPhotoDto(photo));
  }

  public Map<String, List<String>> searchSimilarPhotos(List<String> photos) {
    QueryResultDto searched = queryCommander.search(photos.stream()
        .map(SearchService::toPhotoDto)
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
      List<String> matched = photos.stream().filter(photo -> photo.equals(key))
          .collect(Collectors.toList());
      if (matched.isEmpty()) {
        return;
      }
      // Photo
      // PhotoId만 가지고 있는 객체에서 변경
      retval.put(matched.get(0), value);
    });

    return retval;
  }

  public List<String> searchSimilarPhotoNoOwned(String userId, String photoId) {
    /* 비슷한 이미지 photoId를 모두 가져온다. */
    List<String> similarPhotoIds = searchSimilarPhoto(photoId);
    List<ObjectId> searchQuery = new ArrayList<>();

    List<Album> alreadyHas = albumService.findAllByUserIdAndPhotoIdIn(userId, searchQuery);
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

  public List<String> remainNotOwned(String userId, List<String> photoIds) {
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

    List<Album> alreadyHas = albumService.findAllByUserIdAndPhotoIdIn(userId, searchQuery);
    for (Album album : alreadyHas) {
      String containedPhotoId = reverseMap.get(album.getPhotoId());
      /* Don't have to check that it contains photoId. remove() is ignore it. */
      retval.remove(containedPhotoId);
    }

    return new ArrayList<>(retval);
  }

}
