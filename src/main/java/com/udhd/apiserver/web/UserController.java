package com.udhd.apiserver.web;

import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserResponse mockUserResponse
            = UserResponse.builder().userId("123").nickname("닉네임").numUploadedPhotos(100).numAlbumPhotos(4000).build();

    /**
     * 유저 상세정보 조회. TODO
     *
     * @param userId the user id
     * @return the user response
     */
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse detailUser(
            @PathVariable String userId) {
        return mockUserResponse;
    }

    /**
     * 유저 정보 업데이트. TODO
     *
     * @param userId            the user id
     * @param updateUserRequest the update user request
     * @return the user response
     */
    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateUser(
            @PathVariable String userId,
            UpdateUserRequest updateUserRequest) {
        return mockUserResponse;
    }
}
