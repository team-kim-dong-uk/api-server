package com.udhd.apiserver.domain.album;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends MongoRepository<Album, ObjectId> {
    Album insert(Album album);
    Optional<Album> findById(ObjectId albumId);
    List<Album> findAllByUserIdAndTagsIn(ObjectId userId, List<String> tags);
    List<Album> findAllByUserIdAndTagsInAndIdAfter(ObjectId userId, List<String> tags, ObjectId findAfter);
}
