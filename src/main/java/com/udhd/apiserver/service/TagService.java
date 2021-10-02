package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.service.search.dto.SearchQuery;
import com.udhd.apiserver.service.search.dto.SearchQueryFactory;
import com.udhd.apiserver.service.search.dto.SearchResult;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import pics.udhd.kafka.TagCommander;

@RequiredArgsConstructor
@Service
@Slf4j
public class TagService {
  private final TagCommander tagCommander;
  private final SearchService searchService;
  private final PhotoRepository photoRepository;

  /**
   * 아직 검색풀에 등록되지 않은 사진에 대해서 태그를 추천받기 위해서 사용하는 함수
   * @param url
   * @return
   */
  public List<String> recommendTags(URL url) {
    List<String> tags;
    Map<URL, List<String>> tagMap = findAlreadyUploadedPhotosTagByURL(Collections.singletonList(url));
    tags = tagMap.get(url);
    if (tags == null) {
      tags = tagCommander.fetchRecommendationalTags();
    }
    return tags;
  }

  /**
   * 반드시 PhotoId가 UploadService::registerPhoto에 의해서 호출됬어야 한다.
   * 그렇지 않다면 recommendTags(URL) 함수를 사용해서 동작해야한다.
   * @param photoId
   * @return
   */
  public List<String> recommendTags(String photoId) {
    List<String> tags;
    Map<String, List<String>> tagMap = findAlreadyUploadedPhotosTagByPhotoId(Collections.singletonList(photoId));
    tags = tagMap.get(photoId);
    if (photoId == null) {
      tags = tagCommander.fetchRecommendationalTags();
    }
    return tags;
  }

  /**
   * 만약 이미 업로드된 사진중 중복된 사진이 있을 경우 그 사진에서 tag를 가져온다.
   * (key, value) 중 value 가 null일 경우 업로드되어 있는 사진 중 중복이 없다는 의미이며
   * value 가 empty list일 경우 업로드되어 있는 사진은 있지만, 그 사진의 tag가 empty라는 뜻이므로
   * 혼동하지 않도록 주의한다.
   * @return
   */
  private Map<URL, List<String>> findAlreadyUploadedPhotosTagByURL(List<URL> urls) {
    SearchQuery searchQuery = SearchQueryFactory.generateURLQuery(urls);
    SearchResult searchResult = searchService.searchSimilarPhotos(searchQuery);
    Map<URL, List<String>> tagsTable = new HashMap<>();
    for (URL url : urls) {
      List<String> similarPhotoIds = searchResult.getSimilarPhotoIds(url);
      if (similarPhotoIds.isEmpty()) {
        tagsTable.put(url, null);
        continue;
      }

      // TODO: 현재는 그냥 리스트의 맨 앞을 선택하는데, 정확한 기준을 정해서 추출해야함.
      String representativePhotoId = similarPhotoIds.get(0);
      // TODO: 변수 명이 이상한데 뭘로 바꿔야지?
      Optional<Photo> fetchedData = photoRepository.findById(new ObjectId(representativePhotoId));
      if (fetchedData.isPresent()) {
        Photo photo = fetchedData.get();
        tagsTable.put(url, photo.getTags());
      }
      // TODO: 여긴 버그의 영역임.. DB에 없는 PhotoId를 유사 사진이라고 하는 경우임.
      log.info(String.format("similar Photo must be in db url(%s) representativeId(%s)",
          url.toString(), representativePhotoId));
    }
    return tagsTable;
  }

  private Map<String, List<String>> findAlreadyUploadedPhotosTagByPhotoId(List<String> photoIds) {
    SearchQuery searchQuery = SearchQueryFactory.generatePhotoIdQuery(photoIds);
    SearchResult searchResult = searchService.searchSimilarPhotos(searchQuery);
    Map<String, List<String>> tagsTable = new HashMap<>();
    for (String photoId: photoIds) {
      List<String> similarPhotoIds = searchResult.getSimilarPhotoIds(photoId);
      if (similarPhotoIds.isEmpty()) {
        tagsTable.put(photoId, null);
        continue;
      }

      // TODO: 현재는 그냥 리스트의 맨 앞을 선택하는데, 정확한 기준을 정해서 추출해야함.
      String representativePhotoId = similarPhotoIds.get(0);
      // TODO: 변수 명이 이상한데 뭘로 바꿔야지?
      Optional<Photo> fetchedData = photoRepository.findById(new ObjectId(representativePhotoId));
      if (fetchedData.isPresent()) {
        Photo photo = fetchedData.get();
        tagsTable.put(photoId, photo.getTags());
      }
      // TODO: 여긴 버그의 영역임.. DB에 없는 PhotoId를 유사 사진이라고 하는 경우임.
      log.info(String.format("similar Photo must be in db photoId(%s) representativeId(%s)",
          photoId, representativePhotoId));
    }
    return tagsTable;
  }
}
