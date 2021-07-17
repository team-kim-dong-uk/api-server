package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUserDetail(String userId) throws UserNotFoundException {
        ObjectId userObjectId = new ObjectId(userId);

        User user = userRepository.findById(userObjectId)
                .orElseThrow(() -> new UserNotFoundException(userObjectId));

        return toUserDto(user);
    }

    public UserDto updateUser(String userId, UpdateUserRequest updateUserRequest)
            throws UserNotFoundException {
        ObjectId userObjectId = new ObjectId(userId);

        User user = userRepository.findById(userObjectId)
                        .orElseThrow(() -> new UserNotFoundException(userObjectId));

        if(updateUserRequest.getNickname() != null) {
            user.setNickname(updateUserRequest.getNickname());
        }
        userRepository.save(user);

        return toUserDto(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User insert(User user) {
        return userRepository.insert(user);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .userId(user.getId().toString())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .numAlbumPhotos(2)      // TODO
                .numUploadedPhotos(2)
                .build();
    }
}
