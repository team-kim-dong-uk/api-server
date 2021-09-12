package com.udhd.apiserver.domain.tag;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TagRepository  extends MongoRepository<Tag, String> {
    List<Tag> findTagsByTagStartingWith(String keyword);
    List<Tag> findTagsByTagContaining(String keyword);
}
