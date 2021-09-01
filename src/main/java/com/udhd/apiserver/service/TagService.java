package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class TagService {
    private final TagRepository tagRepository;

    public List<Tag> getRecommendedTags(String keyword) {
        List<Tag> startsWith = tagRepository.findTagsByTagStartingWith(keyword);
        List<Tag> contains = tagRepository.findTagsByTagContaining(keyword);

        return Stream
                .concat(startsWith.stream(), contains.stream())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }
}
