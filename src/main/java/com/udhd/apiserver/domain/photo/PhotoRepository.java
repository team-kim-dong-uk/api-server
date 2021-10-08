package com.udhd.apiserver.domain.photo;

import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoRepository extends MongoRepository<Photo, ObjectId> {
    Photo insert(Photo photo);
    Optional<Photo> findById(ObjectId id);
    Optional<Photo> findByChecksum(String checksum);
    List<Photo> findAll();
    List<Photo> findAllByIdAfter(ObjectId findAfter);
    List<Photo> findAllByTagsInAndIdAfter(List<String> tags, ObjectId findAfter);
    List<Photo> findAllByUploaderIdAndTagsInAndIdAfter(ObjectId uploaderId, List<String> tags, ObjectId findAfter);
    List<Photo> findAllByUploaderId(ObjectId uploaderId);
    List<Photo> findAllByUploaderIdAndIdAfter(ObjectId uploaderId, ObjectId findAfter);
    Boolean existsPhotoByChecksum(String checksum);
    List<Photo> findAllByTagsIn(List<String> tags);
}
