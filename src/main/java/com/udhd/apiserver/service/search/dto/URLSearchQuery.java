package com.udhd.apiserver.service.search.dto;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import pics.udhd.kafka.dto.PhotoDto;

public class URLSearchQuery implements SearchQuery {
  List<String> urls;

  public void set(List<URL> urls) {
    this.urls = urls.stream().map(URL::toString).collect(Collectors.toList());
  }

  @Override
  public List<PhotoDto> getPhotoDtos() {
    return urls.stream().map(url -> PhotoDto.builder()
        .url(url).build())
        .collect(Collectors.toList());
  }
}
