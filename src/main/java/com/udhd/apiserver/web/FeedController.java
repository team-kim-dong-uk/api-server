package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.service.feed.CommentException;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.SuccessResponse;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
      List<FeedDto> feedDtos = feeds.stream().map(feed -> FeedDto.builder()
          .id(feed.getId().toString()) // TODO: 이거 나중에 service layer에서도 dto 만들어줘서 string 추상화 해줘야함
          .photo(feed.getPhoto())
          .comments(feed.getComments())
          .build()).collect(Collectors.toList());
      retval.setFeeds(feedDtos);
    } catch (FeedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
    return retval;
  }

  @PostMapping("/{feedId}/comment")
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
