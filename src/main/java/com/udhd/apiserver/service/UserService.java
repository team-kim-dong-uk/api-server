package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.auth.InvalidAccessTokenException;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * 닉네임을 변경한다.
     * 현재는 중복된 닉네임이 있는지만 체크
     * @param nickname
     * @throws DuplicateNicknameException
     */
    public UserDto setNickname(String userId, String nickname)
            throws DuplicateNicknameException, InvalidAccessTokenException {
        ObjectId userObjectId = new ObjectId(userId);

        // 이미 중복 닉이 있으면 에러
        if (userRepository.existsUserByNickname(nickname)) {
            throw new DuplicateNicknameException("Duplicate nickname " + nickname + " exists");
        }
        User user = userRepository.findById(userObjectId)
                .orElseThrow(() -> new InvalidAccessTokenException("Invalid access token"));
        user.setNickname(nickname);
        userRepository.save(user);
        return toUserDto(user);
    }

    /**
     * 최애 group을 변경한다.
     * @param userId
     * @param group
     * @return
     * @throws InvalidAccessTokenException
     */
    public UserDto setGroup(String userId, String group) throws InvalidAccessTokenException {
        ObjectId userObjectId = new ObjectId(userId);

        User user = userRepository.findById(userObjectId)
                .orElseThrow(() -> new InvalidAccessTokenException("Invalid access token"));
        user.setGroup(group);
        userRepository.save(user);
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

    public User findById(String userId) throws IllegalArgumentException {
        if (StringUtils.isEmpty(userId) || ObjectId.isValid(userId))
            throw new IllegalArgumentException("userId is invalid(userId : " + userId + ")");
        Optional<User> userOptional = userRepository.findById(new ObjectId(userId));

        if (userOptional.isEmpty())
            throw new IllegalArgumentException("there is no user (userId : " + userId + ")");

        return userOptional.get();
    }

    public User insert(User user) {
        return userRepository.insert(user);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .userId(user.getId().toString())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .group(user.getGroup())
                .numAlbumPhotos(2)      // TODO
                .numUploadedPhotos(user.getUploadCount())
                .build();
    }
}
