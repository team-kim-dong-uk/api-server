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
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import com.udhd.apiserver.web.dto.feed.PhotoDto;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/feeds")
@RestController
public class FeedController {
  @Autowired
  FeedService feedService;


  @GetMapping("")
  @ResponseBody
  GeneralResponse getFeeds(HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getFeeds(userId);
      List<FeedDto> feedDtos = feeds.stream().map(feed -> {
        Photo photo = feed.getPhoto();
        PhotoDto photoDto = PhotoDto.builder()
            .id(photo.getId().toString())
            .checksum(photo.getChecksum())
            .originalLink(photo.getOriginalLink())
            .thumbnailLink(photo.getThumbnailLink())
            .createdDate(photo.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .modifiedDate(photo.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build();
        List<CommentDto> commentDtos = feed.getComments().stream().map(comment -> CommentDto.builder()
            .id(comment.getId().toString())
            .userId(comment.getUserId().toString())
            .userName(comment.getUserName())
            .content(comment.getContent())
            .deleted(comment.isDeleted())
            .createdDate(comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .modifiedDate(comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()).collect(Collectors.toList());
        return FeedDto.builder()
            .id(feed.getId().toString()) // TODO: 이거 나중에 service layer에서도 dto 만들어줘서 string 추상화 해줘야함
            .photo(photoDto)
            .comments(commentDtos)
            .build();
      }).collect(Collectors.toList());
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @GetMapping("/related")
  @ResponseBody
  GeneralResponse getRelatedFeeds(@PathVariable String feedId,
      @RequestParam(defaultValue = "") String photoId,
      HttpServletResponse response) {
    FeedResponse retval = new FeedResponse();
    String userId = SecurityUtils.getLoginUserId();
    try {
      List<Feed> feeds = feedService.getRelatedFeeds(userId, photoId);
      List<FeedDto> feedDtos = feeds.stream().map(feed -> {
        Photo photo = feed.getPhoto();
        PhotoDto photoDto = PhotoDto.builder()
            .id(photo.getId().toString())
            .checksum(photo.getChecksum())
            .originalLink(photo.getOriginalLink())
            .thumbnailLink(photo.getThumbnailLink())
            .createdDate(photo.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .modifiedDate(photo.getModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build();
        List<CommentDto> commentDtos = feed.getComments().stream().map(comment -> CommentDto.builder()
            .id(comment.getId().toString())
            .userId(comment.getUserId().toString())
            .userName(comment.getUserName())
            .content(comment.getContent())
            .deleted(comment.isDeleted())
            .createdDate(comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .modifiedDate(comment.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()).collect(Collectors.toList());
        return FeedDto.builder()
            .id(feed.getId().toString()) // TODO: 이거 나중에 service layer에서도 dto 만들어줘서 string 추상화 해줘야함
            .photo(photoDto)
            .comments(commentDtos)
            .build();
      }).collect(Collectors.toList());
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @PutMapping("/{feedId}/comment")
  @ResponseBody
  GeneralResponse registerComment(@PathVariable String feedId, String content, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.registerComment(userId, feedId, content);
      retval.setMessage("success");
    } catch (CommentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }

  @DeleteMapping("/{feedId}/comment/{commentId}")
  @ResponseBody
  GeneralResponse deleteComment(@PathVariable String feedId, @PathVariable String commentId, HttpServletResponse response) {
    String userId = SecurityUtils.getLoginUserId();
    SuccessResponse retval = new SuccessResponse();
    try {
      feedService.deleteComment(userId, feedId, commentId);
      retval.setMessage("success");
    } catch (CommentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }

    return retval;
  }
}
