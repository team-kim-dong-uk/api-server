package com.udhd.apiserver.service.search.dto;

import com.udhd.apiserver.domain.taggedphoto.TaggedPhoto;
import com.udhd.apiserver.service.search.HashService;
import com.udhd.apiserver.service.search.TaggedPhotoDto;
import com.udhd.apiserver.web.dto.EntityMapper;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaggedPhotoDtoMapper implements EntityMapper<TaggedPhotoDto, TaggedPhoto> {
  final HashService hashService;
  public TaggedPhotoDto toDto(TaggedPhoto e) {
    return TaggedPhotoDto.builder()
        .photoId(map(e.getPhotoId()))
        .url(e.getUrl())
        .hash(hashService.generateHash(e.getHash()))
        .build();
  }
  public TaggedPhoto toEntity(TaggedPhotoDto d) {
    String photoId = d.getPhotoId();
    ObjectId objectId;
    if (StringUtils.isEmpty(photoId) || ! ObjectId.isValid(photoId))
      objectId = new ObjectId();
    else
      objectId = new ObjectId(photoId);

    return TaggedPhoto.builder()
        .photoId(objectId)
        .url(d.getUrl())
        .hash(hashService.convertToString(d.getHash()))
        .build();
  }
}
