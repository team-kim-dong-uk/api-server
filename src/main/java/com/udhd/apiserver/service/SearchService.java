package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SearchService {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private static final int SEARCH_RECOMMEND_TAG_COUNT = 10;
    private static final int SEARCH_RECOMMEND_USER_COUNT = 3;

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
}
