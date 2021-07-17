package com.udhd.apiserver.exception.photo;

import com.udhd.apiserver.exception.EntityNotFoundException;
import org.bson.types.ObjectId;

public class PhotoNotFoundException extends EntityNotFoundException {
    public PhotoNotFoundException(ObjectId photoId) {
        super("photo not found with id = " + photoId);
    }
}
