package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.user.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    UserService userService;

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

