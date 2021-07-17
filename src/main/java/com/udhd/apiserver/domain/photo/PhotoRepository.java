package com.udhd.apiserver.domain.photo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends MongoRepository<Photo, ObjectId> {
    Photo insert(Photo photo);
    Optional<Photo> findById(ObjectId id);
    List<Photo> findAllByTagsIn(List<String> tags);
    List<Photo> findAllByTagsInAndIdAfter(List<String> tags, ObjectId findAfter);
}
