package com.udhd.apiserver.service.feed;

import com.mongodb.client.result.UpdateResult;
import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Comment;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.feed.Like;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.exception.album.AlbumNotFoundException;
import com.udhd.apiserver.exception.photo.PhotoNotFoundException;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.web.dto.user.UserDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
public class FeedService {

  private static final Long FEED_HEAD_ID = 0L;
  private static final int DEFAULT_FEED_COUNT = 20;
  /**
   * TODO: 분리해야함 data layer
   */
  @Autowired
  protected MongoTemplate mongoTemplate;
  @Autowired
  FeedRepository feedRepository;
  @Autowired
  UserService userService;
  @Autowired
  AlbumService albumService;
  @Autowired
  SearchService searchService;

  public List<Feed> getFeeds(String userId) throws FeedException {
    return getFeeds(userId, FEED_HEAD_ID);
  }

  public List<Feed> getFeeds(String userId, Long lastOrder) throws FeedException {
    return getFeeds(userId, lastOrder, DEFAULT_FEED_COUNT);
  }

  public List<Feed> getFeeds(String userId, Long lastOrder, int feedCount) throws FeedException {
    // Unused userId
    long millis = Instant.now().toEpochMilli();
    return feedRepository.findAllByCreatedTimestampAfterOrderByOrder(millis);
  }

  public List<Feed> getRelatedFeeds(String userId, String photoId, int distance, int count)
      throws FeedException {
    List<String> similarPhotos = null;
    try {
      similarPhotos = searchService.searchSimilarPhoto(photoId, distance, count);
      System.out.println(similarPhotos);
    } catch (Exception e) {
      log.error(e.toString());
    }
    if (similarPhotos == null)
      similarPhotos = Collections.emptyList();
    // TODO: count 개수도 변화하도록 바꿔야함
    if (similarPhotos.size() < count) {
      similarPhotos.addAll(searchService.searchPhotoByTags(userId, photoId, count));
    }
    Collections.shuffle(similarPhotos);
    similarPhotos = similarPhotos.stream().limit(count).collect(Collectors.toList());

    List<Feed> retval = new ArrayList<>(Collections.emptyList());
    Optional<Feed> optionalFeed = feedRepository.findByPhotoId(new ObjectId(photoId));
    optionalFeed.ifPresent(retval::add);
    feedRepository.findAllByPhotoIdInOrderByOrder(similarPhotos
        .stream().map(p -> {
          if (StringUtils.isEmpty(p) || !ObjectId.isValid(p))
            return null;
          return new ObjectId(p);
        }).collect(Collectors.toList()), PageRequest.of(0, count))
        .forEach(feed -> {
          try {
            if (!feed.getPhoto().getId().toString().equals(photoId))
              retval.add(feed);
          } catch (Exception e) {
            log.error(e.toString());
          }
        });
    return retval;
  }

  public List<Feed> getSavedFeeds(String userId, int count, int page) throws FeedException {
    Pageable pageable = PageRequest.of(page, count);
    List<Album> savedAlbums = albumService.findAllByUserId(userId, pageable);
    List<ObjectId> feedIds = savedAlbums.stream().map(album -> album.getFeedId())
        .collect(Collectors.toList());
    return feedRepository.findAllByIdIn(feedIds);
  }

  public List<Feed> getLikedFeeds(String userId, int count, int page) throws FeedException {
    Pageable pageable = PageRequest.of(page, count);
    List<Feed> ret = feedRepository.findAllLikedFeedsByUserId(new ObjectId(userId), pageable);
    return ret;
  }

  public void registerComment(String userId, String feedId, String content)
      throws CommentException {
    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId)) {
      throw new CommentException("userId cannot be converted to ObjectId. feedId: " + userId);
    }

    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId)) {
      throw new CommentException("feedId cannot be converted to ObjectId. feedId: " + feedId);
    }

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

  public void deleteComment(String userId, String feedId, String commentId)
      throws CommentException {
    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId)) {
      throw new CommentException("userId cannot be converted to ObjectId. feedId: " + userId);
    }

    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId)) {
      throw new CommentException("feedId cannot be converted to ObjectId. feedId: " + feedId);
    }

    ObjectId userObjectId = new ObjectId(userId);
    ObjectId feedObjectId = new ObjectId(feedId);
    ObjectId commentObjectId = new ObjectId(commentId);

    deleteComment(feedObjectId, userObjectId, commentObjectId);
  }

  public void addLike(String userId, String feedId) throws FeedException, DuplicateKeyException {
    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId)) {
      throw new FeedException(
          "Invalid feedId : " + feedId + ". It must be non-empty and proper hexstring");
    }

    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId)) {
      throw new FeedException(
          "Invalid userId : " + userId + ". It must be non-empty and proper hexstring");
    }

    Optional<Feed> existingLikedFeed = feedRepository
        .existsFeedByUserId(new ObjectId(feedId), new ObjectId(userId));
    if (existingLikedFeed.isPresent()) {
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
    if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId)) {
      throw new FeedException(
          "Invalid feedId : " + feedId + ". It must be non-empty and proper hexstring");
    }

    if (StringUtils.isEmpty(userId) || !ObjectId.isValid(userId)) {
      throw new FeedException(
          "Invalid userId : " + userId + ". It must be non-empty and proper hexstring");
    }

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
    if (comment.getCreatedDate() == null) {
      comment.setCreatedDate(LocalDateTime.now());
    }
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
    if (like.getCreatedDate() == null) {
      like.setCreatedDate(LocalDateTime.now());
    }
    like.setModifiedDate(LocalDateTime.now());
    push(feedId, "likes", like);
  }

  protected void deleteLike(ObjectId feedId, ObjectId userId) {
    pull(feedId, "likes", Like.builder().userId(userId).build());
  }

  public Feed getFeed(String feedId) throws FeedException {
    Optional<Feed> feedOptional = feedRepository.findById(new ObjectId(feedId));
    if (feedOptional.isEmpty()) {
      throw new FeedException(FeedException.ERR_NO_FEED);
    }
    return feedOptional.get();
  }

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
