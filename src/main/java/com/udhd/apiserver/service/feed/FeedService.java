package com.udhd.apiserver.service.feed;

import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.photo.Photo;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  @Autowired
  FeedRepository feedRepository;

  private static String FEED_HEAD_ID = "000000000000000000000000";
  private static int DEFAULT_FEED_COUNT = 20;
  /*
  {"_id":{"$oid":"616d24f1f9a88621add4fa18"},
  "uploaderId":{"$oid":"615425cbf83812399805ea84"},
  "thumbnailLink":"https://udhdbucket.s3.ap-northeast-2.amazonaws.com/dcf4af04c49cd976eb1d1862d528d365",
  "originalLink":"https://udhdbucket.s3.ap-northeast-2.amazonaws.com/dcf4af04c49cd976eb1d1862d528d365",
  "checksum":"dcf4af04c49cd976eb1d1862d528d365",
  "tags":["오마이걸","1집"],
  "modifiedDate":{"$date":"2021-10-18T07:45:34.292Z"},
  "_class":"com.udhd.apiserver.domain.photo.Photo"}
   */
  private static List<Feed> mockupFeeds = Arrays.asList(Feed.builder()
      .id(new ObjectId())
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
          .build(), Feed.builder()
          .id(new ObjectId())
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
          )).build()
      );

  public List<Feed> getFeeds(String userId) throws FeedException {
    return getFeeds(userId, FEED_HEAD_ID);
  }

  public List<Feed> getFeeds(String userId, String lastFeedId) throws FeedException {
    return getFeeds(userId, lastFeedId, DEFAULT_FEED_COUNT);
  }

  public List<Feed> getFeeds(String userId, String lastFeedId, int feedCount) throws FeedException {
    return mockupFeeds;
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
}
