package com.udhd.apiserver.domain.user;

import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

  Optional<User> findByEmail(String email);

  Optional<User> findById(ObjectId id);

  boolean existsUserByNickname(String nickname);

  List<User> findUsersByNicknameStartingWith(String keyword);

  List<User> findUsersByNicknameContaining(String keyword);

  User insert(User user);
}
