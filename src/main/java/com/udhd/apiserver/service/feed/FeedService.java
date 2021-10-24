package com.udhd.apiserver.service.feed;

import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.feed.Like;
import com.udhd.apiserver.domain.photo.Photo;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  @Autowired
  FeedRepository feedRepository;

  private static Integer FEED_HEAD_ID = 0;
  private static int DEFAULT_FEED_COUNT = 20;

  private static List<Feed> mockupFeeds = Arrays.asList(Feed.builder()
      .photo(Photo.builder()
          .id(new ObjectId())
          .uploaderId(new ObjectId())
          .checksum("3426318387510b2276beb5447c4f4142") // random md5 dummy hash
          .originalLink("https://udhdbucket.s3.ap-northeast-2.amazonaws.com/3426318387510b2276beb5447c4f4142")
          .thumbnailLink("https://udhdbucket.s3.ap-northeast-2.amazonaws.com/3426318387510b2276beb5447c4f4142")
          .createdDate(LocalDateTime.now())
          .modifiedDate(LocalDateTime.now())
          .tags(Arrays.asList("오마이걸", "1집"))
          .build())
      .comments(Collections.singletonList(
          Comment.builder()
              .id(new ObjectId())
              .userId(new ObjectId())
              .userName("dummy username")
              .content("dummy comment")
              .createdDate(LocalDateTime.now())
              .modifiedDate(LocalDateTime.now())
              .deleted(false)
              .build()))
          .order(11)
          .build(), Feed.builder()
          .photo(Photo.builder()
              .id(new ObjectId())
              .uploaderId(new ObjectId())
              .checksum("dcf4af04c49cd976eb1d1862d528d365") // random md5 dummy hash
              .originalLink("https://udhdbucket.s3.ap-northeast-2.amazonaws.com/dcf4af04c49cd976eb1d1862d528d365")
              .thumbnailLink("https://udhdbucket.s3.ap-northeast-2.amazonaws.com/dcf4af04c49cd976eb1d1862d528d365")
              .createdDate(LocalDateTime.now())
              .modifiedDate(LocalDateTime.now())
              .tags(Arrays.asList("오마이걸", "2집"))
              .build())
          .comments(Collections.singletonList(
              Comment.builder()
                  .id(new ObjectId())
                  .userId(new ObjectId())
                  .userName("dummy user2")
                  .content("dummy comment 2")
                  .createdDate(LocalDateTime.now())
                  .modifiedDate(LocalDateTime.now())
                  .deleted(false)
                  .build()
          ))
          .order(10)
          .build()
      );

  public List<Feed> getFeeds(String userId) throws FeedException {
    return getFeeds(userId, FEED_HEAD_ID);
  }

  public List<Feed> getFeeds(String userId, Integer lastOrder) throws FeedException {
    return getFeeds(userId, lastOrder, DEFAULT_FEED_COUNT);
  }

  public List<Feed> getFeeds(String userId, Integer lastOrder, int feedCount) throws FeedException {
    // Unused userId
    Pageable pageable = PageRequest.of(0, feedCount);
    return feedRepository.findAllByOrderGreaterThanEqual(lastOrder, pageable);
  }

  public List<Feed> getRelatedFeeds(String userId, String photoId) throws FeedException {
    return mockupFeeds;
  }

  public void registerComment(String userId, String feedId, String content) throws CommentException {
  }

  public void deleteComment(String userId, String feedId, String commentId) throws CommentException {
  }

  public void addFavorite(String userId, String feedId) throws FeedException {
  }

  public void deleteFavorite(String userId, String feedId) throws FeedException {
  }

  public void saveFeed(String userId, String feedId) throws FeedException {
  }

  public void deleteSavedFeed(String userId, String feedId) throws FeedException {
  }

  protected void pushComment(ObjectId feedId, Comment comment) {
    push(feedId, "comments", comment);
  }

  protected void deleteComment(ObjectId feedId, ObjectId commentId) {
    pull(feedId, "comments", Comment.builder().id(commentId).build());
  }

  protected void pushLike(ObjectId feedId, Like like) {
    push(feedId, "likes", like);
  }

  protected void deleteLike(ObjectId feedId, ObjectId userId) {
    pull(feedId, "likes", Like.builder().userId(userId).build());
  }

  public Feed getFeed(String userId, String feedId) throws FeedException {
     Optional<Feed> feedOptional = feedRepository.findById(new ObjectId(feedId));
     if (feedOptional.isEmpty())
       throw new FeedException(FeedException.ERR_NO_FEED);
     return feedOptional.get();
  }


  /**
   * TODO: 분리해야함 data layer
   */
  @Autowired
  protected MongoTemplate mongoTemplate;

  void push(ObjectId id, String property, Object value) {
    mongoTemplate.updateFirst(
        Query.query(Criteria.where("id").is(id)),
        new Update().push(property, value), Feed.class
    );
  }
  void pull(ObjectId id, String property, Object value) {
    mongoTemplate.updateFirst(
        Query.query(Criteria.where("id").is(id)),
        new Update().pull(property, value), Feed.class
    );
  }

  public void createDummyData() {
    feedRepository.save(mockupFeeds.get(0));
    feedRepository.save(mockupFeeds.get(1));
  }
}
