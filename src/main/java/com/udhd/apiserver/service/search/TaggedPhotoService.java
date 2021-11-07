package com.udhd.apiserver.service.search;

import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.service.search.dto.TaggedPhotoDto;
import com.udhd.apiserver.service.search.dto.TaggedPhotoDtoMapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaggedPhotoService {

  final PhotoRepository photoRepository;
  final HashService hashService;
  final TaggedPhotoDtoMapper taggedPhotoDtoMapper;

  /**
   * This method is a wrapper method to access into TaggedPhotoRepository. TaggedPhotoRepository에
   * 접근하기 위한 wrappwer method
   *
   * @return taggedPhoto Object. When there is no object with photoId, return null. taggedPhoto 객체를
   * 반환. 만약 photoId를 가진 객체가 없다면, null 반환
   */
  public TaggedPhotoDto fetchByPhotoId(String photoId) {
    Optional<Photo> fetchResult = photoRepository.findById(new ObjectId(photoId));
    if (fetchResult.isEmpty()) {
      log.info("photoId : " + photoId + ", but there is no data");
      return null;
    }
    Photo photo = fetchResult.get();
    return TaggedPhotoDto.builder()
        .photoId(photo.toString())
        .hash(hashService.generateHash(photo.getHash()))
        .build();
  }

  public TaggedPhotoDto save(
      TaggedPhotoDto taggedPhoto) {
    String photoId = taggedPhoto.getPhotoId();
    if (StringUtils.isEmpty(photoId) || !ObjectId.isValid(photoId)) {
      throw new IllegalArgumentException("photo id is invalid : " + photoId);
    }
    Optional<Photo> optionalPhoto = photoRepository.findById(new ObjectId(photoId));
    if (optionalPhoto.isEmpty()) {
      throw new IllegalArgumentException("there is no proper object :" + photoId);
    }

    Photo photo = optionalPhoto.get();
    photo.setHash(hashService.convertToString(taggedPhoto.getHash()));

    Photo savedPhoto = photoRepository.save(photo);
    return taggedPhotoDtoMapper.toDto(savedPhoto);
  }

  public List<TaggedPhotoDto> saveAll(
      TaggedPhotoDto[] taggedPhotos) {
    return saveAll(Arrays.asList(taggedPhotos));
  }

  public List<TaggedPhotoDto> saveAll(Collection<TaggedPhotoDto> taggedPhotos) {
    List<Photo> savedTaggedPhotoVOs = photoRepository.saveAll(
        taggedPhotos.stream().map(taggedPhotoDtoMapper::toEntity).collect(Collectors.toList()));
    return savedTaggedPhotoVOs.stream().map(taggedPhotoDtoMapper::toDto)
        .collect(Collectors.toList());
  }

  public List<TaggedPhotoDto> findAll() {
    return photoRepository.findAll().stream().map(taggedPhotoDtoMapper::toDto)
        .collect(Collectors.toList());
  }

  public List<TaggedPhotoDto> findByPhotoIds(List<String> photoIds) {
    List<ObjectId> photoObjectIds = photoIds.stream().map(ObjectId::new).collect(Collectors.toList());
    List<Photo> photos = photoRepository.findAllById(photoObjectIds);
    return photos.stream().map(photo -> {
        return TaggedPhotoDto.builder()
        .photoId(photo.toString())
        .hash(hashService.generateHash(photo.getHash()))
        .build();
    }).collect(Collectors.toList());
  }
}
