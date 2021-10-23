package com.udhd.apiserver.service.feed;

import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.photo.Photo;
import java.util.Collections;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  private static String FEED_HEAD_ID = "000000000000000000000000";
  private static int DEFAULT_FEED_COUNT = 20;
  private static List<Feed> mockupFeeds = Collections.singletonList(Feed.builder()
      .id(new ObjectId())
      .photo(Photo.builder()
          .id(new ObjectId())
          .checksum("a2e5ee9ec01226be8f12009a7d7663bf") // random md5 dummy hash
          .originalLink("https://udhd.pics")
          .thumbnailLink("https://udhd.pics")
          .build())
      .comments(Collections.singletonList(
          Comment.builder()
              .id(new ObjectId())
              .content("dummy comment")
              .deleted(false)
              .build()
      )).build());

  public List<Feed> getFeeds(String userId) throws FeedException {
    return getFeeds(userId, FEED_HEAD_ID);
  }
  public List<Feed> getFeeds(String userId, String lastFeedId) throws FeedException {
    return getFeeds(userId, lastFeedId, DEFAULT_FEED_COUNT);
  }
  public List<Feed> getFeeds(String userId, String lastFeedId, int feedCount) throws FeedException {
    return mockupFeeds;
  }

  public void registerComment(String userId, String feedId, String content) throws CommentException {
  }

  public void deleteComment(String userId, String feedId, String commentId) throws CommentException {
  }
}
