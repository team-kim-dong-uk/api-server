package com.udhd.apiserver.web.dto.feed;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class PhotoDto {
  String id;
  String uploaderId;
  String thumbnailLink;
  String originalLink;
  String checksum;
  List<String> tags;
  Long createdDate;
  Long modifiedDate;
}
