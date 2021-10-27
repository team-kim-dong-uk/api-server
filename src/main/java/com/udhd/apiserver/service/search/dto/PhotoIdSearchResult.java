package com.udhd.apiserver.service.search.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoIdSearchResult implements SearchResult {
  Set<String> photoIds;
  Map<String, List<String>> resultTable;

  PhotoIdSearchResult() {
    photoIds = new HashSet<>();
    resultTable = new HashMap<>();
  }

  @Override
  public boolean isEmpty() {
    return resultTable.isEmpty();
  }

  @Override
  public List<String> getSimilarPhotoIds(Object _key) {
    String key = castKeyType(_key);
    List<String> retval = resultTable.get(key);

    // 만약, 결과가 없더라도 요청한 내용이라면 null 이 아닌 못찾았다는 결과를 내뱉는다.
    if (retval == null && photoIds.contains(key))
      return Collections.emptyList();

    return retval;
  }

  @Override
  public void setQuery(SearchQuery _query) {
    if (_query instanceof PhotoIdSearchQuery) {
      PhotoIdSearchQuery query = (PhotoIdSearchQuery) _query;
      //photoIds.addAll(query.photoIds);
    } else {
      throw new IllegalArgumentException("Unsupported type for PhotoIdSearchQuery");
    }
  }

  @Override
  public void setSimilarPhotoIds(Object _key, List<String> similarPhotoIds) {
    String key = castKeyType(_key);
    resultTable.put(key, similarPhotoIds);
  }

  @Override
  public void addSimilarPhotoId(Object _key, String similarPhotoId) {
    String key = castKeyType(_key);
    if (!resultTable.containsKey(key)) {
      resultTable.put(key, Collections.emptyList());
    }
    resultTable.get(key).add(similarPhotoId);
  }

  @Override
  public void addSimilarPhotoIdAll(Object _key, Collection<String> similarPhotoIds) {
    String key = castKeyType(_key);
    if (!resultTable.containsKey(key)) {
      resultTable.put(key, Collections.emptyList());
    }
    resultTable.get(key).addAll(similarPhotoIds);
  }

  private String castKeyType(Object key) {
    if (key instanceof String) {
      return (String) key;
    }
    throw new IllegalArgumentException("Unsupported key type. Only support String type.");
  }
}
