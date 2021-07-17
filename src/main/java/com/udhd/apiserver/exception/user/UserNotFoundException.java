package com.udhd.apiserver.exception.user;

import com.udhd.apiserver.exception.EntityNotFoundException;
import org.bson.types.ObjectId;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(ObjectId userId) {
        super("user not found with id = " + userId);
    }
}
