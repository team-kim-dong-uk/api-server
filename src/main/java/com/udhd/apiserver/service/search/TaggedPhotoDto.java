package com.udhd.apiserver.service.search;

import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaggedPhotoDto {
  String photoId;
  Hash hash;
  String url;

  @Override
  public String toString() {
    return "TaggedPhoto(photoId : " + photoId + ", hash :" + hash.toString() + ", url : " + url;
  }
}
