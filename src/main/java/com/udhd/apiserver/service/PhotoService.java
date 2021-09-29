package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.EntityNotFoundException;
import com.udhd.apiserver.exception.photo.PhotoNotFoundException;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Photo service.
 */
@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository photoRepository;

    /**
     * Gets photo detail.
     *
     * @param photoId the photo id
     * @return the photo detail
     * @throws PhotoNotFoundException the photo not found exception
     */
    public PhotoDetailDto getPhotoDetail(String photoId) throws PhotoNotFoundException {
        ObjectId photoObjectId = new ObjectId(photoId);

        Photo photo = photoRepository.findById(photoObjectId)
                        .orElseThrow(() -> new PhotoNotFoundException(photoObjectId));

        return toPhotoDetailDto(photo);
    }

    public List<PhotoOutlineDto> findPhotos(List<String> tags, String uploaderId, String findAfterId, int fetchSize) {
        List<Photo> photos;
        ObjectId findAfterObjectId = findAfterId == null
                ? new ObjectId("000000000000000000000000") : new ObjectId(findAfterId);
        if (uploaderId == null || uploaderId.equals("")) {
            if (tags.size() == 0) {
                photos = photoRepository.findAllByIdAfter(findAfterObjectId);
            } else {
                photos = photoRepository.findAllByTagsInAndIdAfter(tags, findAfterObjectId);
            }
        } else {
            ObjectId uploaderObjectId = new ObjectId(uploaderId);
            if (tags.size() == 0) {
                photos = photoRepository.findAllByUploaderIdAndIdAfter(uploaderObjectId, findAfterObjectId);
            } else {
                photos = photoRepository.findAllByUploaderIdAndTagsInAndIdAfter(uploaderObjectId, tags,
                        findAfterObjectId);
            }
        }

        return photos.stream().limit(fetchSize)
                .map(photo -> toPhotoOutlineDto(photo))
                .collect(Collectors.toList());
    }

    public List<PhotoOutlineDto> findPhotosUploadedBy(String userId, String findAfterId, int fetchSize) {
        ObjectId userObjectId = new ObjectId(userId);

        List<Photo> photos;
        if (findAfterId == null) {
            photos = photoRepository.findAllByUploaderId(userObjectId);
        } else {
            ObjectId findAfterObjectId = new ObjectId(findAfterId);
            photos = photoRepository.findAllByUploaderIdAndIdAfter(userObjectId, findAfterObjectId);
        }

        return photos.stream().limit(fetchSize)
                .map(photo -> toPhotoOutlineDto(photo))
                .collect(Collectors.toList());
    }

    private PhotoDetailDto toPhotoDetailDto(Photo photo) {
        return PhotoDetailDto.builder()
                .photoId(photo.getId().toString())
                .uploaderId(photo.getUploaderId().toString())
                .uploaderNickname("가짜닉")    // TODO
                .originalLink(photo.getOriginalLink())
                .uploadedAt(photo.getId().getDate())
                .tags(photo.getTags())
                .build();
    }

    private PhotoOutlineDto toPhotoOutlineDto(Photo photo) {
        return PhotoOutlineDto.builder()
                .photoId(photo.getId().toString())
                .thumbnailLink(photo.getThumbnailLink())
                .build();
    }
}
