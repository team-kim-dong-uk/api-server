package com.udhd.apiserver.service.feed;

import com.mongodb.client.result.UpdateResult;
import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.feed.Like;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.exception.album.AlbumNotFoundException;
import com.udhd.apiserver.exception.photo.PhotoNotFoundException;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedService {
  @Autowired
  FeedRepository feedRepository;

  @Autowired
  UserService userService;

  @Autowired
  AlbumService albumService;

  private static Integer FEED_HEAD_ID = 0;
  private static int DEFAULT_FEED_COUNT = 20;

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
          .order(11)
          .likes(Collections.emptyList())
          .build(),
          Feed.builder()
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
          ))
          .likes(Collections.emptyList())
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

  public List<Feed> getSavedFeeds(String userId, int count, int page) throws FeedException {
    Pageable pageable = PageRequest.of(page, count);
    List<Album> savedAlbums = albumService.findAllByUserId(userId, pageable);
    List<ObjectId> feedIds = savedAlbums.stream().map(album -> album.getFeedId()).collect(Collectors.toList());
    return feedRepository.findAllById(feedIds);
  }

  public List<Feed> getLikedFeeds(String userId, int count, int page) throws FeedException {
    Pageable pageable = PageRequest.of(page, count);
    List<Feed> ret = feedRepository.findAllLikedFeedsByUserId(new ObjectId(userId), pageable);
    return ret;
  }

  public void registerComment(String userId, String feedId, String content) throws CommentException {
    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
      throw new CommentException("userId cannot be converted to ObjectId. feedId: " + userId);

    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
      throw new CommentException("feedId cannot be converted to ObjectId. feedId: " + feedId);

    ObjectId userObjectId = new ObjectId(userId);
    ObjectId feedObjectId = new ObjectId(feedId);

    try {
      UserDto user = userService.getUserDetail(userId);
      pushComment(feedObjectId, Comment.builder()
          .id(new ObjectId())
          .userId(userObjectId)
          .userName(user.getNickname())
          .content(content)
          .deleted(false)
          .build()
      );
    } catch (UserNotFoundException e) {
      throw new CommentException(e.getMessage());
    }
  }

  public void deleteComment(String userId, String feedId, String commentId) throws CommentException {
    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
      throw new CommentException("userId cannot be converted to ObjectId. feedId: " + userId);

    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
      throw new CommentException("feedId cannot be converted to ObjectId. feedId: " + feedId);

    ObjectId userObjectId = new ObjectId(userId);
    ObjectId feedObjectId = new ObjectId(feedId);
    ObjectId commentObjectId = new ObjectId(commentId);

    deleteComment(feedObjectId, userObjectId, commentObjectId);
  }

  public void addLike(String userId, String feedId) throws FeedException, DuplicateKeyException {
    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
      throw new FeedException("Invalid feedId : " + feedId + ". It must be non-empty and proper hexstring");

    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
      throw new FeedException("Invalid userId : " + userId + ". It must be non-empty and proper hexstring");

    Optional<Feed> existingLikedFeed = feedRepository.existsFeedByUserId(new ObjectId(feedId), new ObjectId(userId));
    if (existingLikedFeed.isPresent()){
      throw new FeedException("이미 좋아요한 사진입니다.");
    }

    User user = userService.findById(userId);
    userService.updateCount(userId, "addLike");
    pushLike(new ObjectId(feedId),
        Like.builder()
        .userId(new ObjectId(userId))
        .userName(user.getNickname())
        .build());
  }

  public void deleteLike(String userId, String feedId) throws FeedException {
    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
      throw new FeedException("Invalid feedId : " + feedId + ". It must be non-empty and proper hexstring");

    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
      throw new FeedException("Invalid userId : " + userId + ". It must be non-empty and proper hexstring");

    deleteLike(new ObjectId(feedId), new ObjectId(userId));
    userService.updateCount(userId, "deleteLike");
  }

  public void saveFeed(String userId, String feedId) throws FeedException {
    try {
      albumService.saveAlbum(userId, feedId);
    } catch (PhotoNotFoundException e) {
      throw new FeedException(e.getMessage());
    }
    userService.updateCount(userId, "addSave");
  }

  public void deleteSavedFeed(String userId, String feedId) throws FeedException {
    try {
      albumService.deleteAlbum(userId, feedId);
    } catch (AlbumNotFoundException e) {
      throw new FeedException(e.getMessage());
    }
    userService.updateCount(userId, "deleteSave");
  }

  protected void pushComment(ObjectId feedId, Comment comment) {
    if (comment.getCreatedDate() == null)
      comment.setCreatedDate(LocalDateTime.now());
    comment.setModifiedDate(LocalDateTime.now());
    push(feedId, "comments", comment);
  }

  protected void deleteComment(ObjectId feedId, ObjectId userId, ObjectId commentId) {
    pull(feedId, "comments", Comment.builder().id(commentId).build());
    mongoTemplate.updateFirst(
        Query.query(Criteria.where("id").is(feedId)),
        new Update().pull("comments",
            Comment.builder().id(commentId).userId(userId).build()), Feed.class);
  }

  protected void pushLike(ObjectId feedId, Like like) {
    if (like.getCreatedDate() == null)
      like.setCreatedDate(LocalDateTime.now());
    like.setModifiedDate(LocalDateTime.now());
    push(feedId, "likes", like);
  }

  protected void deleteLike(ObjectId feedId, ObjectId userId) {
    pull(feedId, "likes", Like.builder().userId(userId).build());
  }

  public Feed getFeed(String feedId) throws FeedException {
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
    mongoTemplate.updateMulti(
        Query.query(Criteria.where("id").is(id)),
        new Update().push(property, value), Feed.class
    );
  }
  void pull(ObjectId id, String property, Object value) {
    UpdateResult result = mongoTemplate.updateMulti(
        Query.query(Criteria.where("id").is(id)),
        new Update().pull(property, value), Feed.class
    );
  }

  public void createDummyData() {
    feedRepository.save(mockupFeeds.get(0));
    feedRepository.save(mockupFeeds.get(1));
  }


}
