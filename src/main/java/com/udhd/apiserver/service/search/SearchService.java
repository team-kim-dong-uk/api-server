package com.udhd.apiserver.service.search;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.search.dto.TaggedPhotoDto;
import com.udhd.apiserver.util.ImageUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import dev.brachtendorf.jimagehash.hash.Hash;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class SearchService {

  private static final int SEARCH_RECOMMEND_TAG_COUNT = 10;
  private static final int SEARCH_RECOMMEND_USER_COUNT = 3;
  private final TaggedPhotoService taggedPhotoService;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final PhotoRepository photoRepository;
  private final PersistentPhotoBkTreeService bkTreeService;
  private final HashService hashService;
  private final AlbumService albumService;
  private final FeedRepository feedRepository;


  public List<SearchCandidateDto> getRecommendedKeywords(String keyword) {
    List<Tag> tagStartsWith = tagRepository.findTagsByTagStartingWith(keyword);
    List<Tag> tagContains = tagRepository.findTagsByTagContaining(keyword);

    List<User> userStartsWith = userRepository.findUsersByNicknameStartingWith(keyword);
    List<User> userContains = userRepository.findUsersByNicknameContaining(keyword);

    Stream<SearchCandidateDto> tags
        = Stream
        .concat(tagStartsWith.stream(), tagContains.stream())
        .distinct()
        .limit(SEARCH_RECOMMEND_TAG_COUNT)
        .map(tag -> SearchCandidateDto.fromTag(tag));

    Stream<SearchCandidateDto> users
        = Stream
        .concat(userStartsWith.stream(), userContains.stream())
        .distinct()
        .limit(SEARCH_RECOMMEND_USER_COUNT)
        .map(user -> SearchCandidateDto.fromUser(user));

    return Stream.concat(tags, users).collect(Collectors.toList());
  }

  public List<String> searchSimilarPhoto(String photoId, int distance, int count) {
    TaggedPhotoDto taggedPhoto = taggedPhotoService.fetchByPhotoId(photoId);
    if (taggedPhoto == null) {
      return Collections.emptyList();
    }
    return bkTreeService.search(taggedPhoto, distance, count)
        .stream()
        .map(TaggedPhotoDto::getPhotoId)
        .limit(count)
        .collect(Collectors.toList());
  }

  public void registerPhoto(PhotoDto photoDto) {
    try {
      Hash hash = hashService.generateHash(ImageUtils.load(photoDto.getUrl()));
      bkTreeService.insert(
          TaggedPhotoDto.builder()
              .photoId(photoDto.getPhotoId())
              .hash(hash)
              .build()
      );
    } catch (IOException e) {
      log.info("error", e);
    }
  }

  public List<String> searchPhotoByTags(String photoId, int count) {
    if (StringUtils.isEmpty(photoId) || !ObjectId.isValid(photoId)) {
      return Collections.emptyList();
    }

    ObjectId photoObjectId = new ObjectId(photoId);
    Optional<Photo> photoOptional = photoRepository.findById(photoObjectId);
    if (photoOptional.isEmpty()) {
      return Collections.emptyList();
    }
    Photo photo = photoOptional.get();
    System.out.println(photo);
    List<String> tags = photo.getTags();
    if (tags == null || tags.isEmpty()) {
      return Collections.emptyList();
    }
    List<Photo> photos = photoRepository.findAllByTagsInRandom(photo.getTags(), count);
    System.out.println(photos);
    if (photos.isEmpty()) {
      return Collections.emptyList();
    }
    return photos.stream().map(p -> p.getId().toString()).collect(Collectors.toList());
  }

  public List<TaggedPhotoDto> searchSimilarPhotos(List<TaggedPhotoDto> photoDtos) {
    List<TaggedPhotoDto> retval = new ArrayList<>();
    try {
      photoDtos.forEach(photoDto -> {
        retval.addAll(bkTreeService.search(photoDto));
      });
    } catch (Exception e) {
      log.info("queryCommander has created exception", e);
    }

    return retval;
  }


  public List<String> remainNotOwned(String userId, List<String> photoIds) {
    /* 모든 값이 일단 가지고 있지 않다고 가정한다. */
    Set<String> retval = new HashSet<>(photoIds);

    /* 비슷한 이미지 photoId를 모두 가져온다. */

    /* 역으로 참조해야하기 때문에 이를 위한 Mapping Table을 만든다. */
    Map<ObjectId, String> reverseMap = new HashMap<>();

    List<TaggedPhotoDto> taggedPhotoDtos = taggedPhotoService.findByPhotoIds(photoIds);
    List<ObjectId> similarPhotos = searchSimilarPhotos(taggedPhotoDtos)
        .stream()
        .map(taggedPhotoDto -> new ObjectId(taggedPhotoDto.getPhotoId())).collect(Collectors.toList());

    List<Feed> feeds = feedRepository.findAllByPhotoIdIn(similarPhotos);
    feeds.forEach(feed -> {
      reverseMap.put(feed.getId(), feed.getPhoto().getId().toString());
    });
    List<ObjectId> feedObjectIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());

    List<Album> alreadyHas = albumService.findAllByUserIdAndFeedIdIn(userId, feedObjectIds);
    for (Album album : alreadyHas) {
      String containedPhotoId = reverseMap.get(album.getFeedId());
      /* Don't have to check that it contains photoId. remove() is ignore it. */
      retval.remove(containedPhotoId);
    }

    return new ArrayList<>(retval);
  }

  public List<PhotoOutlineDto> searchPhotoByAllTags(List<String> tags, Integer page) {
    final int defaultPageSize = 21;
    return photoRepository.findAllByAllTags(tags, PageRequest.of(page, defaultPageSize))
        .stream().map(PhotoService::toPhotoOutlineDto).collect(Collectors.toList());
  }
}
