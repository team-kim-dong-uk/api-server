package com.udhd.apiserver.service.search;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.service.search.dto.SearchQuery;
import com.udhd.apiserver.service.search.dto.SearchQueryFactory;
import com.udhd.apiserver.service.search.dto.SearchResult;
import com.udhd.apiserver.service.search.dto.SearchResultFactory;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import pics.udhd.query.domain.TaggedPhotoRepository;
import pics.udhd.query.service.PhotoBkTreeService;
import pics.udhd.query.service.TaggedPhotoService;
import pics.udhd.query.service.dto.TaggedPhoto;

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
    TaggedPhoto taggedPhoto = taggedPhotoService.fetchByPhotoId(photoId);
    return bkTreeService.search(taggedPhoto, 20, count)
        .stream()
        .map(TaggedPhoto::getPhotoId).collect(Collectors.toList());
  }
}
