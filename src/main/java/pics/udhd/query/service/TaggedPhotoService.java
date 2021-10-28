package pics.udhd.query.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhotoRepository;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhotoVO;
import pics.udhd.query.service.dto.TaggedPhoto;

@Service
@Slf4j
public class TaggedPhotoService {
  @Resource
  TaggedPhotoRepository taggedPhotoRepository;
  @Resource
  HashService hashService;

  /**
   * This method is a wrapper method to access into TaggedPhotoRepository.
   * TaggedPhotoRepository에 접근하기 위한 wrappwer method
   * @param photoId
   * @return taggedPhoto Object. When there is no object with photoId, return null.
   *         taggedPhoto 객체를 반환. 만약 photoId를 가진 객체가 없다면, null 반환
   */
  public TaggedPhoto fetchByPhotoId(String photoId) {
    Optional<TaggedPhotoVO> fetchResult = taggedPhotoRepository.findById(new ObjectId(photoId));
    if (fetchResult.isEmpty()) {
      log.info("photoId : " + photoId + ", but there is no data");
      return null;
    }
    TaggedPhotoVO taggedPhotoVO = fetchResult.get();
    return TaggedPhoto.builder()
        .photoId(taggedPhotoVO.getPhotoId().toString())
        .url(taggedPhotoVO.getUrl())
        .hash(hashService.generateHash(taggedPhotoVO.getHash()))
        .build();
  }

  public TaggedPhoto save(TaggedPhoto taggedPhoto) {
    TaggedPhotoVO taggedPhotoVO = convertToVO(taggedPhoto);
    TaggedPhotoVO savedTaggedPhotoVO = taggedPhotoRepository.save(taggedPhotoVO);
    return convertToDto(savedTaggedPhotoVO);
  }

  public List<TaggedPhoto> saveAll(TaggedPhoto[] taggedPhotos) {
    return saveAll(Arrays.asList(taggedPhotos));
  }
  public List<TaggedPhoto> saveAll(Collection<TaggedPhoto> taggedPhotos) {
    List<TaggedPhotoVO> savedTaggedPhotoVOs = taggedPhotoRepository.saveAll(
        taggedPhotos.stream().map(this::convertToVO).collect(Collectors.toList()));
    return savedTaggedPhotoVOs.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  protected TaggedPhotoVO convertToVO(TaggedPhoto taggedPhoto) {
    String photoId = taggedPhoto.getPhotoId();
    ObjectId objectId = (photoId != null) ? new ObjectId(photoId) : new ObjectId();
    return TaggedPhotoVO.builder()
        .photoId(objectId)
        .url(taggedPhoto.getUrl())
        .hash(hashService.convertToString(taggedPhoto.getHash()))
        .build();
  }

  protected TaggedPhoto convertToDto(TaggedPhotoVO taggedPhotoVO) {
    return TaggedPhoto.builder()
        .photoId(taggedPhotoVO.getPhotoId().toString())
        .url(taggedPhotoVO.getUrl())
        .hash(hashService.generateHash(taggedPhotoVO.getHash()))
        .build();
  }
}
