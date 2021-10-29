package com.udhd.apiserver.service.search.dto;

import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.service.search.HashService;
import com.udhd.apiserver.web.dto.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaggedPhotoDtoMapper implements EntityMapper<TaggedPhotoDto, Photo> {
  final HashService hashService;
  public TaggedPhotoDto toDto(Photo e) {
    return TaggedPhotoDto.builder()
        .photoId(map(e.getId()))
        .hash(hashService.generateHash(e.getHash()))
        .build();
  }

  public Photo toEntity(TaggedPhotoDto d) {
    String photoId = d.getPhotoId();
    ObjectId objectId;
    if (StringUtils.isEmpty(photoId) || ! ObjectId.isValid(photoId))
      objectId = new ObjectId();
    else
      objectId = new ObjectId(photoId);

    return Photo.builder()
        .id(objectId)
        .hash(hashService.convertToString(d.getHash()))
        .build();
  }
}
