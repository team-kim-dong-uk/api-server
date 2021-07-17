package com.udhd.apiserver.exception.album;

import com.udhd.apiserver.exception.EntityNotFoundException;
import org.bson.types.ObjectId;

public class AlbumNotFoundException extends EntityNotFoundException {
    public AlbumNotFoundException(ObjectId albumId) {
        super("album not found with id = " + albumId);
    }
}