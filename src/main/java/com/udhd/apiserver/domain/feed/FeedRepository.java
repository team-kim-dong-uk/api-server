package com.udhd.apiserver.domain.feed;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FeedRepository extends MongoRepository<Feed, ObjectId> {
}
