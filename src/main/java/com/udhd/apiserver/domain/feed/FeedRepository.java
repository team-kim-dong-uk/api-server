package com.udhd.apiserver.domain.feed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FeedRepository extends MongoRepository<Feed, ObjectId> {

  Feed insert(Feed feed);

  List<Feed> findAllByOrder(Long order);

  List<Feed> findAllByOrder(Long order, Pageable pageable);

  List<Feed> findAllByOrderGreaterThanEqual(Long order, Pageable pageable);
  List<Feed> findAllByCreatedTimestampAfterOrderByOrder(Long createdTimestamp);

  List<Feed> findAllByCreatedDateBetween(LocalDateTime s, LocalDateTime e, Pageable pageable);

  @Query("{ 'likes': { $elemMatch : { '_id': ?0 }}}")
  List<Feed> findAllLikedFeedsByUserId(ObjectId userId, Pageable pageable);

  @Query("{ 'id' : ?0 , " +
      "'likes': { $elemMatch : { '_id': ?1 }}}")
  Optional<Feed> existsFeedByUserId(ObjectId id, ObjectId userId);

  List<Feed> findAllByIdIn(List<ObjectId> feedIds);

  List<Feed> findAllByPhotoIdOrderByOrder(ObjectId photoId, Pageable pageable);

  List<Feed> findAllByPhotoIdInOrderByOrder(List<ObjectId> photoIds, Pageable pageable);

  @Query("{ 'photo._id' : ?0 }")
  List<Feed> findAllByPhotoId(ObjectId photoObjectId);
}
