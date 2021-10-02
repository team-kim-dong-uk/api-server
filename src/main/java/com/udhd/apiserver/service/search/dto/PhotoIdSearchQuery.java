package com.udhd.apiserver.service.search.dto;

import java.util.List;
import java.util.stream.Collectors;
import pics.udhd.kafka.dto.PhotoDto;

public class PhotoIdSearchQuery implements SearchQuery {
  List<String> photoIds;
  public void set(List<String> photoIds) {
    this.photoIds = photoIds;
  }

  public List<PhotoDto> getPhotoDtos() {
    return photoIds.stream().map(photoId -> PhotoDto.builder()
            .photoId(photoId)
            .build())
        .collect(Collectors.toList());
  }
}
