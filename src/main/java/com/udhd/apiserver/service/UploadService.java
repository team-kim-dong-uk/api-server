package com.udhd.apiserver.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.domain.upload.UploadRepository;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.upload.UploadWithGoogleDriveRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.ServerRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UploadService {
    private final AmazonS3Client amazonS3Client;
    private final PhotoRepository photoRepository;
    private final UploadRepository uploadRepository;
    private final AlbumRepository albumRepository;
    private final RestTemplate restTemplate;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String bucketRegion;

    public String generatePollingKey(String userId) {
        return userId + "." + System.currentTimeMillis();
    }

    public List<Upload> uploadWithGoogleDrive(UploadWithGoogleDriveRequest uploadWithGoogleDriveRequest,
                                          String pollingKey) {
        ObjectId uploaderId = new ObjectId(SecurityUtils.getLoginUserId());
        List<Upload> uploads = uploadWithGoogleDriveRequest.getFileIds()
                .stream()
                .map(fileId ->
                        Upload.builder()
                                .pollingKey(pollingKey)
                                .uploaderId(uploaderId)
                                .fileId(fileId)
                                .status("uploading")
                                .build())
                .collect(Collectors.toList());
        uploadRepository.saveAll(uploads);
        return uploads;
    }

    @Async
    public void processUploads(List<Upload> uploads, String googleDriveToken) {
        System.out.println("process");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + googleDriveToken);
        HttpEntity request = new HttpEntity(headers);
        String s3UrlPrefix = "https://" + bucket + ".s3." + bucketRegion + ".amazonaws.com/";

        for (Upload upload : uploads) {
            processUpload(upload, request, s3UrlPrefix);
        }
    }

    @Async
    public void processUpload(Upload upload, HttpEntity request, String s3UrlPrefix) {
        System.out.println("upload");
        ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://www.googleapis.com/drive/v3/files/" + upload.getFileId() + "?alt=media",
                HttpMethod.GET,
                request,
                byte[].class
        );

        String checksum = calculateChecksum(response.getBody());

        // checksum 및 s3 url 디비에 update
        upload.setChecksum(checksum);
        upload.setS3Url(s3UrlPrefix + checksum);
        uploadRepository.save(upload);

        if (photoRepository.existsPhotoByChecksum(checksum)) {
            confirmUpload(upload);
        } else {
            InputStream imageInputStream = new ByteArrayInputStream(response.getBody());

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, checksum, imageInputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3Client.putObject(putObjectRequest);

            //TODO: bktree를 위한 message 발행
            confirmUpload(upload); // TODO: bktree 저장 완료후 DB 저장하기로 바꾸기
        }
    }

    @Async
    public void confirmUpload(Upload upload) {
        // Photo collection에서 checksum으로 해당 사진을 찾음. 없으면 새로 저장함.
        Photo photo = photoRepository.findByChecksum(upload.getChecksum())
                .orElseGet(() -> {
                    // TODO: ML 서버에서 추천태그 바다오기
                    List<String> tags = Arrays.asList("오마이걸", "1집");
                    Photo newPhoto = Photo.builder()
                            .id(upload.getId())
                            .checksum(upload.getChecksum())
                            .originalLink(upload.getS3Url())
                            .thumbnailLink(upload.getS3Url())
                            .uploaderId(upload.getUploaderId())
                            .tags(tags)
                            .build();
                    photoRepository.insert(newPhoto);
                    return newPhoto;
                });

        // Photo 정보와 tag 정보를 조합해 Album collection에 새 사진 저장.
        List<String> tags = upload.getTags() == null ?
                photo.getTags() : upload.getTags();
        Album album = Album.builder()
                .tags(tags)
                .favourite(false)
                .thumbnailLink(photo.getThumbnailLink())
                .lastViewed(new Date())
                .photoId(photo.getId())
                .userId(upload.getUploaderId())
                .build();
        albumRepository.insert(album);

        // Upload collection 에 저장 완료로 표시
        upload.setStatus("uploaded");
        uploadRepository.save(upload);
    }

    public Long checkProgress(String pollingKey) {
        Long total = uploadRepository.countByPollingKey(pollingKey);
        Long done = uploadRepository.countByPollingKeyAndStatus(pollingKey, "uploaded");
        return total == 0 ? 0 : 100 * done / total;
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] checksumInBytes = messageDigest.digest(data);
            return Hex.encodeHexString(checksumInBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
