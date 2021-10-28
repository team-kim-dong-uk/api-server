package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.service.feed.CommentException;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.SuccessResponse;
import com.udhd.apiserver.web.dto.feed.CommentDto;
import com.udhd.apiserver.web.dto.feed.CommentRequest;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedDtoMapper;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import com.udhd.apiserver.web.dto.feed.LikeDto;
import com.udhd.apiserver.web.dto.feed.PhotoDto;
import java.time.ZoneId;
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
  @Autowired
  FeedService feedService;
  @Autowired
  FeedDtoMapper feedDtoMapper;

  final String SUCCESS_MESSAGE = "success";

  @GetMapping("")
  @ResponseBody
  GeneralResponse getFeeds(HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getFeeds(userId);
      log.info("feed", feeds);
      List<FeedDto> feedDtos = feeds.stream()
              .map(feedDtoMapper::toDto)
              .collect(Collectors.toList());
      log.info("feedDto", feedDtos);
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @GetMapping("/related")
  @ResponseBody
  GeneralResponse getRelatedFeeds(@RequestParam(defaultValue = "") String photoId,
      HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getRelatedFeeds(userId, photoId);
      List<FeedDto> feedDtos = feeds.stream()
              .map(feedDtoMapper::toDto)
              .collect(Collectors.toList());
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @PutMapping("/{feedId}/comment")
  @ResponseBody
  Object registerComment(@PathVariable String feedId, @RequestBody CommentRequest commentRequest, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    try {
      String content = commentRequest.getContent();
      feedService.registerComment(userId, feedId, content);
      return feedDtoMapper.toDto(feedService.getFeed(feedId));
    } catch (CommentException | FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/{feedId}/comment/{commentId}")
  @ResponseBody
  Object deleteComment(@PathVariable String feedId, @PathVariable String commentId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    try {
      feedService.deleteComment(userId, feedId, commentId);
      return feedDtoMapper.toDto(feedService.getFeed(feedId));
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
