package com.udhd.apiserver.domain.feed;

import java.time.LocalDateTime;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FeedRepository extends MongoRepository<Feed, ObjectId> {
  Feed insert(Feed feed);
  List<Feed> findAllByOrder(Integer order);
  List<Feed> findAllByOrder(Integer order, Pageable pageable);
  List<Feed> findAllByOrderGreaterThanEqual(Integer order, Pageable pageable);
  List<Feed> findAllByCreatedDateBetween(LocalDateTime s, LocalDateTime e, Pageable pageable);

  @Query("{ 'likes': { $elemMatch : { '_id': ?0 }}}")
  List<Feed> findAllLikedFeedsByUserId(ObjectId userId);
}
