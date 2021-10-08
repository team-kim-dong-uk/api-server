package com.udhd.apiserver.domain.tag;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TagRepository extends MongoRepository<Tag, String> {

  List<Tag> findTagsByTagStartingWith(String keyword);

  List<Tag> findTagsByTagContaining(String keyword);
}
