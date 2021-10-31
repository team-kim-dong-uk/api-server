package com.udhd.apiserver.web;

import static com.udhd.apiserver.web.FeedController.toFeedDto;
import static com.udhd.apiserver.web.FeedController.toFeedDtoList;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedDtoMapper;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;
    private final PhotoService photoService;
    private final FeedService feedService;
    private final AlbumService albumService;
    private final FeedDtoMapper feedDtoMapper;

    @GetMapping("/{userId}/like")
    @ResponseStatus(HttpStatus.OK)
    public GeneralResponse listLiked(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "0") int page,
            @PathVariable String userId,
            HttpServletResponse response
    ) {
        FeedResponse retval = new FeedResponse();
        try {
            List<Feed> feeds = feedService.getLikedFeeds(userId, count, page);
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


    @PutMapping("/{userId}/nickname")
    public UserDto setNickname(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest updateUserRequest) throws DuplicateNicknameException {
        SecurityUtils.checkUser(userId);

        return userService.setNickname(userId, updateUserRequest.getNickname());
      }
  /**
   * 유저 상세정보 조회.
   *
   * @param userId the user id
   * @return the user response
   */
  @GetMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto detailUser(
      @PathVariable String userId) {
    return userService.getUserDetail(userId);
  }


  @PutMapping("/{userId}/group")
  public UserDto setGroup(
      @PathVariable String userId,
      @RequestBody UpdateUserRequest updateUserRequest) throws DuplicateNicknameException {
    SecurityUtils.checkUser(userId);

    return userService.setGroup(userId, updateUserRequest.getGroup());
  }

  /**
   * 유저 정보 업데이트.
   *
   * @param userId the user id
   * @param updateUserRequest the update user request
   * @return the user response
   */
  @PatchMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto updateUser(
      @PathVariable String userId,
      @RequestBody UpdateUserRequest updateUserRequest) {
    SecurityUtils.checkUser(userId);

    return userService.updateUser(userId, updateUserRequest);
  }

  /**
   * 유저가 업로드한 사진 목록을 가져온다.
   */
  @GetMapping("/{userId}/uploaded")
  @ResponseStatus(HttpStatus.OK)
  public List<PhotoOutlineDto> uploadedPhotos(
      @PathVariable String userId,
      @RequestParam(defaultValue = "photoId") String sortBy,
      @RequestParam(required = false) String findAfter,
      @RequestParam(defaultValue = "21") Integer fetchSize) {
    SecurityUtils.checkUser(userId);

    return photoService.findPhotosUploadedBy(userId, findAfter, fetchSize);
  }
}
