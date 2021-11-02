package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.feed.CommentException;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.SuccessResponse;
import com.udhd.apiserver.web.dto.feed.CommentDto;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedDtoMapper;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import com.udhd.apiserver.web.dto.feed.LikeDto;
import com.udhd.apiserver.web.dto.feed.PhotoDto;
import com.udhd.apiserver.web.dto.feed.RegisterCommentRequestDto;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/feeds")
@RestController
@Slf4j
public class FeedController {

  final String SUCCESS_MESSAGE = "success";
  @Autowired
  FeedService feedService;
  @Autowired
  AlbumService albumService;
  FeedDtoMapper feedDtoMapper;

  public static List<FeedDto> toFeedDtoList(List<Feed> feeds, List<Album> savedFeeds) {
    return feeds.stream()
        .map(feed -> {
          boolean saved = false;
          for (Album album : savedFeeds) {
            if (album.getFeedId().equals(feed.getId())) {
              saved = true;
            }
          }
          return toFeedDto(feed, saved);
        })
        .collect(Collectors.toList());
  }

  public static FeedDto toFeedDto(Feed feed, boolean saved) {
    Photo photo = feed.getPhoto();
    PhotoDto photoDto = PhotoDto.builder()
        .id(photo.getId().toString())
        .uploaderId(photo.getUploaderId().toString())
        .checksum(photo.getChecksum())
        .originalLink(photo.getOriginalLink())
        .thumbnailLink(photo.getThumbnailLink())
        .createdDate(
            photo.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .modifiedDate(
            photo.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .tags(photo.getTags())
        .build();
    List<CommentDto> commentDtos = feed.getComments().stream().map(comment -> CommentDto.builder()
        .id(comment.getId().toString())
        .userId(comment.getUserId().toString())
        .userName(comment.getUserName())
        .content(comment.getContent())
        .deleted(comment.isDeleted())
        .createdDate(
            comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .modifiedDate(
            comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .build()).collect(Collectors.toList());
    List<LikeDto> likeDtos = feed.getLikes().stream().map(
        like -> LikeDto.builder().id(like.getId().toString()).userId(like.getUserId().toString())
            .userName(like.getUserName()).build()).collect(Collectors.toList());
    return FeedDto.builder()
        .id(feed.getId().toString()) // TODO: 이거 나중에 service layer에서도 dto 만들어줘서 string 추상화 해줘야함
        .photo(photoDto)
        .comments(commentDtos)
        .likes(likeDtos)
        .saved(saved)
        .build();
  }

  public static List<FeedDto> toFeedDtoList(List<Feed> feeds, List<Album> savedFeeds,
      String userId) {
    return feeds.stream()
        .map(feed -> {
          boolean saved = false;
          for (Album album : savedFeeds) {
            if (album.getFeedId().equals(feed.getId())) {
              saved = true;
            }
          }
          return toFeedDto(feed, saved, userId);
        })
        .collect(Collectors.toList());
  }

  public static FeedDto toFeedDto(Feed feed, boolean saved, String userId) {
    Photo photo = feed.getPhoto();
    PhotoDto photoDto = PhotoDto.builder()
        .id(photo.getId().toString())
        .uploaderId(photo.getUploaderId().toString())
        .checksum(photo.getChecksum())
        .originalLink(photo.getOriginalLink())
        .thumbnailLink(photo.getThumbnailLink())
        .createdDate(
            photo.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .modifiedDate(
            photo.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .tags(photo.getTags())
        .build();
    List<CommentDto> commentDtos = feed.getComments().stream().map(comment -> CommentDto.builder()
        .id(comment.getId().toString())
        .userId(comment.getUserId().toString())
        .userName(comment.getUserName())
        .content(comment.getContent())
        .deleted(comment.isDeleted())
        .createdDate(
            comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .modifiedDate(
            comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .build()).collect(Collectors.toList());
    List<LikeDto> likeDtos = feed.getLikes().stream().map(
        like -> LikeDto.builder().id(like.getId().toString()).userId(like.getUserId().toString())
            .userName(like.getUserName()).build()).collect(Collectors.toList());
    return FeedDto.builder()
        .id(feed.getId().toString()) // TODO: 이거 나중에 service layer에서도 dto 만들어줘서 string 추상화 해줘야함
        .photo(photoDto)
        .comments(commentDtos)
        .likes(likeDtos)
        .liked(likeDtos.stream()
            .filter(like -> userId.equals(like.getUserId())).count() > 0)
        .saved(saved)
        .build();
  }

  @GetMapping("")
  @ResponseBody
  public GeneralResponse getFeedsForBackCompatibility(
      @RequestParam(defaultValue = "0") Long lastOrder,
      HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getFeeds(userId, lastOrder);
      List<Album> savedFeeds = userId.length() > 0
          ? albumService.findAllByUserIdAndFeedIdIn(userId,
          feeds.stream().map(feed -> feed.getId()).collect(Collectors.toList()))
          : Collections.emptyList();
      List<FeedDto> feedDtos = toFeedDtoList(feeds, savedFeeds, userId);
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @GetMapping("/list")
  @ResponseBody
  public GeneralResponse getFeeds(HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getFeeds(userId);
      List<Album> savedFeeds = userId.length() > 0
          ? albumService.findAllByUserIdAndFeedIdIn(userId,
          feeds.stream().map(feed -> feed.getId()).collect(Collectors.toList()))
          : Collections.emptyList();
      List<FeedDto> feedDtos = toFeedDtoList(feeds, savedFeeds, userId);
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @GetMapping("/related")
  @ResponseBody
  GeneralResponse getRelatedFeeds(
      @RequestParam(defaultValue = "") String photoId,
      @RequestParam(defaultValue = "20") Integer distance,
      @RequestParam(defaultValue = "21") Integer count,
      HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getRelatedFeeds(userId, photoId, distance, count);
      List<Album> savedFeeds = albumService.findAllByUserIdAndFeedIdIn(userId,
          feeds.stream().map(feed -> feed.getId()).collect(Collectors.toList()));
      List<FeedDto> feedDtos = toFeedDtoList(feeds, savedFeeds, userId);
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @PutMapping("/{feedId}/comment")
  @ResponseBody
  Object registerComment(@PathVariable String feedId,
      @RequestBody RegisterCommentRequestDto registerCommentRequestDto,
      HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    try {
      feedService.registerComment(userId, feedId, registerCommentRequestDto.getContent());
      return toFeedDto(feedService.getFeed(feedId), albumService.isSavedFeed(userId, feedId),
          userId);
    } catch (CommentException | FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/{feedId}/comment/{commentId}")
  @ResponseBody
  Object deleteComment(@PathVariable String feedId, @PathVariable String commentId,
      HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    try {
      feedService.deleteComment(userId, feedId, commentId);
      return toFeedDto(feedService.getFeed(feedId), albumService.isSavedFeed(userId, feedId),
          userId);
    } catch (CommentException | FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/{feedId}/like")
  GeneralResponse addFavorite(@PathVariable String feedId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.addLike(userId, feedId);
      retval.setMessage(SUCCESS_MESSAGE);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }

  @DeleteMapping("/{feedId}/like")
  GeneralResponse deleteFavorite(@PathVariable String feedId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.deleteLike(userId, feedId);
      retval.setMessage(SUCCESS_MESSAGE);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }

  @PutMapping("/{feedId}/save")
  GeneralResponse saveFeed(@PathVariable String feedId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.saveFeed(userId, feedId);
      retval.setMessage(SUCCESS_MESSAGE);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }

  @DeleteMapping("/{feedId}/save")
  GeneralResponse deleteSavedFeed(@PathVariable String feedId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.deleteSavedFeed(userId, feedId);
      retval.setMessage(SUCCESS_MESSAGE);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }
}
