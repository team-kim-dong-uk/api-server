package com.udhd.apiserver.service.search;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.search.dto.SearchQuery;
import com.udhd.apiserver.service.search.dto.SearchQueryFactory;
import com.udhd.apiserver.service.search.dto.SearchResult;
import com.udhd.apiserver.service.search.dto.SearchResultFactory;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import java.net.URL;
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
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import pics.udhd.kafka.QueryCommander;
import pics.udhd.kafka.dto.QueryResultDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class SearchService {

  private static final int SEARCH_RECOMMEND_TAG_COUNT = 10;
  private static final int SEARCH_RECOMMEND_USER_COUNT = 3;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final AlbumService albumService;
  private final PhotoService photoService;
  private final QueryCommander queryCommander;


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


  /**
   * 현재 등록되어 있는 사진 중 유사한 이미지를 찾아서 반환
   * 내부적으로 활용하는 함수이기에 private 처리, 만약 바깥쪽에서 유사한 이미지 전체를 가져올 필요가
   * 있다면 public으로 전환해도 무관
   * @param photoIds : 사진의 ObjectId의 String 값
   * @return : (PhotoId, PhotoIds) 로 구성되는 Map
   */
  public SearchResult searchSimilarPhotos(SearchQuery query) {
    QueryResultDto searched = null;
    SearchResult result = SearchResultFactory.generate(query);
    try {
      searched = queryCommander.search(query.getPhotoDtos());
    } catch (Exception e) {
      log.info("queryCommander has created exception", e);
    }

    if (searched != null) {
      searched.getValue().forEach(result::setSimilarPhotoIds);
    }

    return result;
  }

  public List<String> searchSimilarPhotoNoOwned(String userId, String photoId) {
    /* 비슷한 이미지 photoId를 모두 가져온다. */
    List<String> similarPhotoIds = searchSimilarPhoto(photoId);
    List<ObjectId> searchQuery = new ArrayList<>();

    List<Album> alreadyHas = albumService.findAllByUserIdAndPhotoIdIn(userId, searchQuery);
    Set<String> retval = new HashSet<>(similarPhotoIds);
    for (Album album : alreadyHas) {
      retval.remove(album.getPhotoId().toString());
    }
    return new ArrayList<>(retval);
  }

  public List<String> searchSimilarPhoto(String photoId) {
    SearchQuery searchQuery = SearchQueryFactory.generatePhotoIdQuery(Collections.singletonList(photoId));
    SearchResult searched = searchSimilarPhotos(searchQuery);
    return searched.getSimilarPhotoIds(photoId);
  }

  public List<String> searchSimilarPhoto(Photo photo) {
    return searchSimilarPhoto(photo.getId().toString());
  }

  public List<String> remainNotOwned(String userId, List<String> photoIds) {
    /* 모든 값이 일단 가지고 있지 않다고 가정한다. */
    Set<String> retval = new HashSet<>(photoIds);

    /* 비슷한 이미지 photoId를 모두 가져온다. */
    SearchQuery searchQuery = SearchQueryFactory.generatePhotoIdQuery(photoIds);
    SearchResult searched = searchSimilarPhotos(searchQuery);

    /* 역으로 참조해야하기 때문에 이를 위한 Mapping Table을 만든다. */
    Map<ObjectId, String> reverseMap = new HashMap<>();
    List<ObjectId> flattenSimilarPhotos = new ArrayList<>();

    photoIds.forEach(photoId -> {
      List<String> similarPhotos = searched.getSimilarPhotoIds(photoId);
      similarPhotos.forEach(similarPhotoId -> {
        ObjectId e = new ObjectId(similarPhotoId);
        reverseMap.put(e, photoId);
        flattenSimilarPhotos.add(e);
      });
    });

    List<Album> alreadyHas = albumService.findAllByUserIdAndPhotoIdIn(userId, flattenSimilarPhotos);
    for (Album album : alreadyHas) {
      String containedPhotoId = reverseMap.get(album.getPhotoId());
      /* Don't have to check that it contains photoId. remove() is ignore it. */
      retval.remove(containedPhotoId);
    }

    return new ArrayList<>(retval);
  }

  public List<PhotoOutlineDto> searchTag(String userId,
      List<String> tags, String uploaderId, String sortBy, String findAfter, Integer fetchSize) {
    List<PhotoOutlineDto> retval = new ArrayList<>();

    // 원하는 크기가 될때까지 데이터를 가져온다.
    // 20개를 원한다. -> db 에서 20개를 긁고,
    while (retval.size() < fetchSize) {
      // TODO : fetch를 해올 때 fetchSize * 2 정도로 더 긁어서 DB 콜을 줄여라.
      List<PhotoOutlineDto> fetchedData = photoService
          .findPhotos(tags, sortBy, uploaderId, findAfter, fetchSize);

      if (fetchedData.isEmpty())
        break;
      findAfter = fetchedData.get(fetchedData.size() - 1).getPhotoId();

      List<String> notDuplicatedPhotoIds = remainNotOwned(userId,
          fetchedData.stream().map(PhotoOutlineDto::getPhotoId).collect(Collectors.toList())
      );

      Set<String> notDuplicatedPhotoIdsSet = new HashSet<>(notDuplicatedPhotoIds);

      for (PhotoOutlineDto photoOutlineDto : fetchedData) {
        if (notDuplicatedPhotoIdsSet.contains(photoOutlineDto.getPhotoId())) {
          retval.add(photoOutlineDto);
        }
      }
    }
    // trim Data for afterId
    retval.subList(0, Math.min(fetchSize, retval.size()));

    return retval;
  }
}
