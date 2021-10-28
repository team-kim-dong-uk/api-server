package com.udhd.apiserver.service.search;

import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final PhotoBkTreeService bkTreeService;


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

  public List<String> searchSimilarPhoto(String photoId, int count) {
    TaggedPhotoDto taggedPhoto = taggedPhotoService.fetchByPhotoId(photoId);
    if (taggedPhoto == null)
      return Collections.emptyList();
    return bkTreeService.search(taggedPhoto, 20, count)
        .stream()
        .map(TaggedPhotoDto::getPhotoId).collect(Collectors.toList());
  }
}
