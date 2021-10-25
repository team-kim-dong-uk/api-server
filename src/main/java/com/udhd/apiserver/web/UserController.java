package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.feed.FeedDto;
import com.udhd.apiserver.web.dto.feed.FeedResponse;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

import static com.udhd.apiserver.web.FeedController.toFeedDto;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;
    private final PhotoService photoService;
    private final FeedService feedService;

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

    @GetMapping("/{userId}/save")
    @ResponseStatus(HttpStatus.OK)
    public GeneralResponse listSaved(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "1") int page,
            @PathVariable String userId,
            HttpServletResponse response
    ) {
        FeedResponse retval = new FeedResponse();
        try {
            List<Feed> feeds = feedService.getSavedFeeds(userId, count, page);
            List<FeedDto> feedDtos = feeds.stream()
                    .map(feed -> toFeedDto(feed))
                    .collect(Collectors.toList());
            retval.setFeeds(feedDtos);
        } catch (FeedException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        return retval;
    }

    @GetMapping("/{userId}/like")
    @ResponseStatus(HttpStatus.OK)
    public GeneralResponse listLiked(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "1") int page,
            @PathVariable String userId,
            HttpServletResponse response
    ) {
        FeedResponse retval = new FeedResponse();
        try {
            List<Feed> feeds = feedService.getLikedFeeds(userId, count, page);
            List<FeedDto> feedDtos = feeds.stream()
                    .map(feed -> toFeedDto(feed))
                    .collect(Collectors.toList());
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
     * @param userId            the user id
     * @param updateUserRequest the update user request
     * @return the user response
     */
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(
            @PathVariable String userId,
            UpdateUserRequest updateUserRequest) {
        SecurityUtils.checkUser(userId);

        return userService.updateUser(userId, updateUserRequest);
    }

    /**
     * 유저가 업로드한 사진 목록을 가져온다.
     * @param userId
     * @param sortBy
     * @param findAfter
     * @param fetchSize
     * @return
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
