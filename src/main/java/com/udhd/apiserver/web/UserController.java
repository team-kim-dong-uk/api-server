package com.udhd.apiserver.web;

import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;

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
}
