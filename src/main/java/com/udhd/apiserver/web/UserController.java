package com.udhd.apiserver.web;

import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;
    private final PhotoService photoService;

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
