package com.udhd.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.user.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@SpringBootTest
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    UserService userService;

    protected MockMvc mockMvc;

    @Test
    @DisplayName("존재하지 않는 유저 삭제")
    void deleteUser_404() throws Exception {
        String userId = "60e2fea74c17cf5152fb5b78";

        // 이미 가지고 있는 앨범 데이터
        doThrow(new IllegalArgumentException()).when(userRepository).findById(new ObjectId(userId));

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(userId));
    }
}

