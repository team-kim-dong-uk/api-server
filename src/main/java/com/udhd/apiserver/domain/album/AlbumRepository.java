package com.udhd.apiserver.domain.album;

import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlbumRepository extends MongoRepository<Album, ObjectId> {
    Album insert(Album album);
    Optional<Album> findById(ObjectId albumId);
    Optional<Album> findByUserIdAndPhotoId(ObjectId userId, ObjectId photoId);
    List<Album> findAllByUserId(ObjectId userId);
    List<Album> findAllByUserIdAndIdAfter(Object userId, ObjectId findAfter);
    List<Album> findAllByUserIdAndTagsIn(ObjectId userId, List<String> tags);
    List<Album> findAllByUserIdAndTagsInAndIdAfter(ObjectId userId, List<String> tags, ObjectId findAfter);
    List<Album> findAllByUserIdAndPhotoIdIn(ObjectId userId, List<ObjectId> photoIds);
}
