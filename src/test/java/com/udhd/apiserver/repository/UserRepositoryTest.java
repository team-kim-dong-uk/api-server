package com.udhd.apiserver.repository;

import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private String userId;

    @BeforeEach
    void inputUser(){
        User user = User.builder()
                .email("test@email.com")
                .nickname("Tester")
                .build();
        this.userId = userRepository.save(user).getId().toString();
    }
    @AfterEach
    void deleteUser(){
        userRepository.deleteById(new ObjectId(userId));
    }

    @Test
    void findUserTest() throws ClassNotFoundException {
        System.out.println("userId is : " + this.userId);
        Optional<User> user = userRepository.findById(new ObjectId(userId));
        assertThat(user.isPresent()).isTrue();

        Class c = Class.forName("com.udhd.apiserver.domain.user.User");
        assertThat(c).isNotNull();
    }



}
