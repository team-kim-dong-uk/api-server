package com.udhd.apiserver.service.search.dto;

public class SearchResultFactory {
  public static SearchResult generate(SearchQuery query) {
    if (query instanceof PhotoIdSearchQuery) {
      PhotoIdSearchResult result = new PhotoIdSearchResult();
      result.setQuery(query);
      return result;
    }
    if (query instanceof URLSearchQuery) {
      URLSearchResult result = new URLSearchResult();
      result.setQuery(query);
      return result;
    }
    throw new IllegalArgumentException("Unsupported query type");
  }
}