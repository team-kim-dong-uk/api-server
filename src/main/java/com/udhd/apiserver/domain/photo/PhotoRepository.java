package com.udhd.apiserver.domain.photo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoRepository extends MongoRepository<Photo, ObjectId> {
    Photo insert(Photo photo);
    Optional<Photo> findById(ObjectId id);
    Optional<Photo> findByChecksum(String checksum);
    List<Photo> findAll();
    List<Photo> findAllByIdAfter(ObjectId findAfter);
    List<Photo> findAllByIdAfter(ObjectId findAfter, Pageable pageable);
    Stream<Photo> findAllByIdAfter(ObjectId findAfter, Sort sort);
    List<Photo> findAllByTagsInAndIdAfter(List<String> tags, ObjectId findAfter);
    List<Photo> findAllByTagsInAndIdAfter(List<String> tags, ObjectId findAfter, Pageable pageable);
    Stream<Photo> findAllByTagsInAndIdAfter(List<String> tags, ObjectId findAfter, Sort sort);
    List<Photo> findAllByUploaderIdAndTagsInAndIdAfter(ObjectId uploaderId, List<String> tags, ObjectId findAfter);
    List<Photo> findAllByUploaderIdAndTagsInAndIdAfter(ObjectId uploaderId, List<String> tags, ObjectId findAfter, Pageable pageable);
    Stream<Photo> findAllByUploaderIdAndTagsInAndIdAfter(ObjectId uploaderId, List<String> tags, ObjectId findAfter, Sort sort);
    List<Photo> findAllByUploaderId(ObjectId uploaderId);
    Stream<Photo> findAllByUploaderId(ObjectId uploaderId, Sort sort);
    List<Photo> findAllByUploaderIdAndIdAfter(ObjectId uploaderId, ObjectId findAfter);
    List<Photo> findAllByUploaderIdAndIdAfter(ObjectId uploaderId, ObjectId findAfter, Pageable pageable);
    Stream<Photo> findAllByUploaderIdAndIdAfter(ObjectId uploaderId, ObjectId findAfter, Sort sort);
    Boolean existsPhotoByChecksum(String checksum);
    List<Photo> findAllByTagsIn(List<String> tags);
}
