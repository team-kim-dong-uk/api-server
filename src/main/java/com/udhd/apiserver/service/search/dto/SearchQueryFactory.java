package com.udhd.apiserver.service.search.dto;

import java.net.URL;
import java.util.List;

public class SearchQueryFactory {
  public static SearchQuery generatePhotoIdQuery(List<String> photoIds) {
    PhotoIdSearchQuery retval = new PhotoIdSearchQuery();
    //retval.set(photoIds);
    return retval;
  }
  public static SearchQuery generateURLQuery(List<URL> urls) {
    URLSearchQuery retval = new URLSearchQuery();
    //retval.set(urls);
    return retval;
  }
}
