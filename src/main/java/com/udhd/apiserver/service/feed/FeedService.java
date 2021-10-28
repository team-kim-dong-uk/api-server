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
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.web.dto.user.UserDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import com.udhd.apiserver.web.dto.feed.CommentDto;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.PhotoDto;
import org.apache.commons.lang3.StringUtils;
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

  @Autowired
  UserService userService;

  @Autowired
  AlbumService albumService;

  @Autowired
  SearchService searchService;

  private static Integer FEED_HEAD_ID = 0;
  private static int DEFAULT_FEED_COUNT = 20;

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
    int defaultCount = 20;
    List<String> similarPhotos = searchService.searchSimilarPhoto(photoId, defaultCount);
    // TODO: count 개수도 변화하도록 바꿔야함
    return feedRepository.findAllByPhotoIdInOrderByOrder(similarPhotos
        .stream().map(ObjectId::new).collect(
        Collectors.toList()), PageRequest.of(0, defaultCount));
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

  public void addLike(String userId, String feedId) throws FeedException {
    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId))
      throw new FeedException("Invalid feedId : " + feedId + ". It must be non-empty and proper hexstring");

    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId))
      throw new FeedException("Invalid userId : " + userId + ". It must be non-empty and proper hexstring");

    User user = userService.findById(userId);
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
  }

  public void saveFeed(String userId, String feedId) throws FeedException {
    try {
      albumService.saveAlbum(userId, feedId);
    } catch (PhotoNotFoundException e) {
      throw new FeedException(e.getMessage());
    }
  }

  public void deleteSavedFeed(String userId, String feedId) throws FeedException {
    try {
      albumService.deleteAlbum(userId, feedId);
    } catch (AlbumNotFoundException e) {
      throw new FeedException(e.getMessage());
    }
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
}
