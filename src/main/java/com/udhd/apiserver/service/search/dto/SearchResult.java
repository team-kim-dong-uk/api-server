package com.udhd.apiserver.service.search.dto;


import java.util.Collection;
import java.util.List;

public interface SearchResult {
  boolean isEmpty();
  List<String> getSimilarPhotoIds(Object key);
  void setQuery(SearchQuery query);
  void setSimilarPhotoIds(Object key, List<String> similarPhotoIds);
  void addSimilarPhotoId(Object key, String similarPhotoId);
  void addSimilarPhotoIdAll(Object key, Collection<String> similarPhotoId);
}
