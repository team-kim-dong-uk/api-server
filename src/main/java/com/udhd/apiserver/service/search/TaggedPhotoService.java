package com.udhd.apiserver.service.search;

import com.udhd.apiserver.service.search.dto.TaggedPhotoDtoMapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhotoRepository;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhoto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaggedPhotoService {
  @Resource
  TaggedPhotoRepository taggedPhotoRepository;
  final HashService hashService;
  final TaggedPhotoDtoMapper taggedPhotoDtoMapper;

  /**
   * This method is a wrapper method to access into TaggedPhotoRepository.
   * TaggedPhotoRepository에 접근하기 위한 wrappwer method
   * @param photoId
   * @return taggedPhoto Object. When there is no object with photoId, return null.
   *         taggedPhoto 객체를 반환. 만약 photoId를 가진 객체가 없다면, null 반환
   */
  public TaggedPhotoDto fetchByPhotoId(String photoId) {
    Optional<TaggedPhoto> fetchResult = taggedPhotoRepository.findById(new ObjectId(photoId));
    if (fetchResult.isEmpty()) {
      log.info("photoId : " + photoId + ", but there is no data");
      return null;
    }
    TaggedPhoto taggedPhotoVO = fetchResult.get();
    return TaggedPhotoDto.builder()
        .photoId(taggedPhotoVO.getPhotoId().toString())
        .url(taggedPhotoVO.getUrl())
        .hash(hashService.generateHash(taggedPhotoVO.getHash()))
        .build();
  }

  public TaggedPhotoDto save(
      TaggedPhotoDto taggedPhoto) {
    TaggedPhoto taggedPhotoVO = taggedPhotoDtoMapper.toEntity(taggedPhoto);
    TaggedPhoto savedTaggedPhotoVO = taggedPhotoRepository.save(taggedPhotoVO);
    return taggedPhotoDtoMapper.toDto(savedTaggedPhotoVO);
  }

  public List<TaggedPhotoDto> saveAll(
      TaggedPhotoDto[] taggedPhotos) {
    return saveAll(Arrays.asList(taggedPhotos));
  }
  public List<TaggedPhotoDto> saveAll(Collection<TaggedPhotoDto> taggedPhotos) {
    List<TaggedPhoto> savedTaggedPhotoVOs = taggedPhotoRepository.saveAll(
        taggedPhotos.stream().map(taggedPhotoDtoMapper::toEntity).collect(Collectors.toList()));
    return savedTaggedPhotoVOs.stream().map(taggedPhotoDtoMapper::toDto).collect(Collectors.toList());
  }

  public List<TaggedPhotoDto> findAll() {
    return taggedPhotoRepository.findAll().stream().map(taggedPhotoDtoMapper::toDto)
        .collect(Collectors.toList());
  }
}
