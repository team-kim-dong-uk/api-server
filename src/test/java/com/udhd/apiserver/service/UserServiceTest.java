package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.exception.auth.InvalidAccessTokenException;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    UserService userService;

    private static String userId = "6110066323a94f7c27f9cf4c";
    private static ObjectId userObjectId = new ObjectId(userId);
    User user = User.builder()
            .id(new ObjectId(userId))
            .nickname("tester")
            .group("omygirl")
            .likeCount(3)
            .saveCount(2)
            .build();
    @Test
    @DisplayName("유저 상세정보")
    void getUserDetail(){
        when(userRepository.findById(userObjectId))
                .thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserDetail(userId);

        assertThat(userDto.getUserId()).isEqualTo(userId);
    }
    @Test
    @DisplayName("유저 상세정보 - 존재하지 않는 유저")
    void getUserDetail_404(){
        when(userRepository.findById(userObjectId))
                .thenThrow(new UserNotFoundException(userObjectId));

        assertThrows(UserNotFoundException.class,
                () -> userRepository.findById(userObjectId));
    }

    @Test
    @DisplayName("닉네임 변경")
    void setNickname(){
        String nicknameForChange = "fromis";
        User userChangeNickname = User.builder()
                .id(userObjectId)
                .nickname(nicknameForChange)
                .build();

        when(userRepository.existsUserByNickname(nicknameForChange))
                .thenReturn(false);
        when(userRepository.findById(userObjectId))
                .thenReturn(Optional.of(userChangeNickname));

        UserDto userDto = userService.setNickname(userId, nicknameForChange);

        assertThat(userDto.getUserId()).isEqualTo(userId);
        assertThat(userDto.getNickname()).isEqualTo(nicknameForChange);
    }
    @Test
    @DisplayName("닉네임 변경 - 중복")
    void setNickname_duplicate(){
        String nicknameForChange = "fromis";

        when(userRepository.existsUserByNickname(nicknameForChange))
                .thenReturn(true);

        assertThrows(DuplicateNicknameException.class,
                () -> userService.setNickname(userId, nicknameForChange));
    }
    @Test
    @DisplayName("닉네임 변경 - Invalid")
    void setNickname_invalid(){
        String nicknameForChange = "fromis";

        when(userRepository.findById(userObjectId))
                .thenThrow(new InvalidAccessTokenException("Invalid access token"));

        assertThrows(InvalidAccessTokenException.class,
                () -> userService.setNickname(userId, nicknameForChange));
    }

    @Test
    @DisplayName("유저 정보 업데이트")
    void updateUser(){
        String nicknameForChange = "fromis";
        UpdateUserRequest request = UpdateUserRequest.builder()
                .nickname(nicknameForChange)
                .build();

        when(userRepository.findById(userObjectId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.updateUser(userId, request);
        assertThat(result.getNickname()).isEqualTo(nicknameForChange);
    }
    @Test
    @DisplayName("존재하지 않는 유저 정보 업데이트")
    void updateUser_404(){
        String nicknameForChange = "fromis";
        UpdateUserRequest request = UpdateUserRequest.builder()
                .nickname(nicknameForChange)
                .build();
        when(userRepository.findById(userObjectId))
                .thenThrow(new UserNotFoundException(userObjectId));

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    @DisplayName("존재하지 않는 유저 삭제")
    void deleteUser_404() {
        String userId = "60e2fea74c17cf5152fb5b78";

        // 이미 가지고 있는 앨범 데이터
        doThrow(new IllegalArgumentException()).when(userRepository).findById(new ObjectId(userId));

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));
    }
}

