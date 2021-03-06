package com.udhd.apiserver.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.domain.upload.UploadRepository;
import com.udhd.apiserver.service.search.PhotoDto;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.util.ImageUtils;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.upload.UploadWithGoogleDriveRequest;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;

@RequiredArgsConstructor
@Service
@Slf4j
public class UploadService {

  private final AmazonS3Client amazonS3Client;
  private final PhotoRepository photoRepository;
  private final UploadRepository uploadRepository;
  private final AlbumRepository albumRepository;
  private final RestTemplate restTemplate;
  private final FeedRepository feedRepository;
  private final SearchService searchService;
  @Value("${cloud.aws.s3.presigned-url-duration}")
  private final int PRESIGNED_URL_DURATION = 1000 * 60 * 5;
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String bucketRegion;

  public String generatePollingKey(String userId) {
    return userId + "." + System.currentTimeMillis();
  }

  public List<Upload> uploadWithGoogleDrive(
      UploadWithGoogleDriveRequest uploadWithGoogleDriveRequest,
      String pollingKey) {
    ObjectId uploaderId = new ObjectId(SecurityUtils.getLoginUserId());
    List<Upload> uploads = uploadWithGoogleDriveRequest.getFileIds()
        .stream()
        .map(fileId ->
            Upload.builder()
                .pollingKey(pollingKey)
                .uploaderId(uploaderId)
                .fileId(fileId)
                .uploaderId(uploaderId)
                .status(Upload.STATUS_UPLOADING)
                .build())
        .collect(Collectors.toList());
    uploadRepository.saveAll(uploads);
    return uploads;
  }

  @Async
  public void processUploadsFromGDrive(List<Upload> uploads, String googleDriveToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + googleDriveToken);
    HttpEntity request = new HttpEntity(headers);
    String s3UrlPrefix = "https://" + bucket + ".s3." + bucketRegion + ".amazonaws.com/";

    for (Upload upload : uploads) {
      processUploadFromGDrive(upload, request, s3UrlPrefix);
    }
  }

  @Async
  public void processUploadFromGDrive(Upload upload, HttpEntity request, String s3UrlPrefix) {
    ResponseEntity<byte[]> response = restTemplate.exchange(
        "https://www.googleapis.com/drive/v3/files/" + upload.getFileId() + "?alt=media",
        HttpMethod.GET,
        request,
        byte[].class
    );
    processUpload(upload, response.getBody(), s3UrlPrefix);
  }

  public void processUpload(Upload upload, byte[] data, String s3UrlPrefix) {
    String checksum = calculateChecksum(data);

    // checksum ??? s3 url ????????? update
    upload.setChecksum(checksum);
    upload.setS3Url(s3UrlPrefix + checksum);
    uploadRepository.save(upload);

    // TODO: bktree ?????? ????????? DB ??????????????? ?????????
    if (!photoRepository.existsPhotoByChecksum(checksum)) {
      InputStream imageInputStream = new ByteArrayInputStream(data);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType("image/jpeg");
      PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, checksum,
          imageInputStream, metadata)
          .withCannedAcl(CannedAccessControlList.PublicRead);

      amazonS3Client.putObject(putObjectRequest);

      //TODO: bktree??? ?????? message ??????
    }
    confirmUpload(upload);
  }

  public String confirmUpload(String pollingKey, String checksum) {
    Upload upload = uploadRepository.findByPollingKeyAndChecksum(pollingKey, checksum);
    // TODO: upload??? ????????? ??? ??????
    confirmUpload(upload);
    return upload.getId().toString();
  }

  @Async
  public void confirmUpload(Upload upload) {
    // Photo collection?????? checksum?????? ?????? ????????? ??????. ????????? ?????? ?????????.
    Photo photo = fetchOrCreatePhotoByUpload(upload);

    // Photo ????????? tag ????????? ????????? Album collection??? ??? ?????? ??????.
      if (upload.getTags() == null) {
          saveIntoFeed(photo);
      } else {
          saveIntoFeed(photo);
      }

    registerPhoto(photo);
    // Upload collection ??? ?????? ????????? ??????
    markCompleted(upload);
  }

  private void registerPhoto(Photo photo) {
    searchService.registerPhoto(toPhotoDto(photo));
  }

  private PhotoDto toPhotoDto(Photo photo) {
    return PhotoDto.builder()
        .photoId(photo.getId().toString())
        .url(photo.getOriginalLink())
        .build();
  }

  void saveIntoAlbum(ObjectId userId, Photo photo) {
    saveIntoAlbum(userId, photo, photo.getTags());
  }

  void saveIntoFeed(Photo photo) {
    Feed feed = Feed.builder()
        .order(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        .photo(photo)
        .likes(Collections.emptyList())
        .comments(Collections.emptyList())
        .build();
    feedRepository.save(feed);
  }

  void saveIntoAlbum(ObjectId userId, Photo photo, List<String> tags) {
    Album album = Album.builder()
        .tags(tags)
        .thumbnailLink(photo.getThumbnailLink())
        .lastViewed(new Date())
        .feedId(photo.getId())
        .userId(userId)
        .build();
    albumRepository.insert(album);
  }

  private String getUrlWithoutParameters(String url) throws URISyntaxException {
    URI uri = new URI(url);
    return new URI(uri.getScheme(),
        uri.getAuthority(),
        uri.getPath(),
        null, // Ignore the query part of the input url
        uri.getFragment()).toString();
  }

  Photo fetchOrCreatePhotoByUpload(Upload upload) {
    Photo photo = photoRepository.findByChecksum(upload.getChecksum())
        .orElseGet(() -> {
          String url = upload.getS3Url();
          try {
            url = getUrlWithoutParameters(upload.getS3Url());
          } catch (URISyntaxException e) {
            log.error("upload parse url error ", e);
          }
          List<String> tags = null;
          //tags = tagService.recommendTags(new URL(url));
          tags = Collections.emptyList();
          Photo newPhoto = Photo.builder()
              .id(upload.getId())
              .checksum(upload.getChecksum())
              .originalLink(url)
              .thumbnailLink(url)
              .uploaderId(upload.getUploaderId())
              .tags(tags)
              .build();
          photoRepository.insert(newPhoto);
          return newPhoto;
        });
    return photo;
  }

  public Long getProgress(String pollingKey) {
    Long total = uploadRepository.countByPollingKey(pollingKey);
    Long done = uploadRepository.countByPollingKeyAndStatus(pollingKey, Upload.STATUS_COMPLETED);
    return total == 0 ? 0 : 100 * done / total;
  }

  public void createThumbnailsAndScaledImages() {
    List<Photo> photos = photoRepository.findAll();
    String s3UrlPrefix = "https://" + bucket + ".s3." + bucketRegion + ".amazonaws.com/";
    for (int i=0; i < photos.size();  i++) {
	Photo photo = photos.get(i);
      try {
        BufferedImage originalImage = ImageUtils.load(photo.getOriginalLink());
        // create and upload thumbnail image
        if (photo.getThumbnailLink() == null || photo.getThumbnailLink().equals(photo.getOriginalLink())) {
          BufferedImage thumbnailImage = ImageUtils.createThumbnail(originalImage);
          String thumbnailKey = photo.getChecksum() + "-thumb";
          uploadBufferedImage(thumbnailImage, thumbnailKey);
          photo.setThumbnailLink(s3UrlPrefix + thumbnailKey);
        }
        // create and upload scaled image
        if (photo.getScaledLink() == null) {
          BufferedImage scaledImage = ImageUtils.createScaledImage(originalImage);
          String scaledKey = photo.getChecksum() + "-scaled";
          uploadBufferedImage(scaledImage, scaledKey);
          photo.setScaledLink(s3UrlPrefix + scaledKey);
        }
	photoRepository.save(photo);
      } catch(Exception e) {
        System.out.println(photo.getId());
        e.printStackTrace();
      }
      if (i % 1000 == 0) {
	      System.out.println(i);
      }
    }
    System.out.println("done!!!!!");
  }

  private void uploadBufferedImage(BufferedImage image, String objectKey) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(image, "jpeg", os);
    byte[] buffer = os.toByteArray();
    InputStream imageInputStream = new ByteArrayInputStream(buffer);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType("image/jpeg");
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, objectKey,
            imageInputStream, metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead);
    amazonS3Client.putObject(putObjectRequest);
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

  public List<Upload> createUpload(String userId, String pollingKey, List<String> checksums) {
    List<Upload> uploads = checksums.stream().map(
        checksum -> Upload.builder()
            .pollingKey(pollingKey)
            .checksum(checksum)
            .status(Upload.STATUS_UPLOADING)
            .uploaderId(new ObjectId(userId))
            .s3Url(null)
            .build()
    ).map(uploadRepository::save).collect(Collectors.toList());
    return uploads;
  }

  /**
   * s3 pre-signed url?????? ????????????. ???????????? i?????? ?????? i?????? ????????? ??? ????????? ?????? presigned url??????, i?????? ????????? ????????? ?????? ????????? ??????
   * null ??????.
   *
   * @return the pre signed urls
   */
  public void fillPresignedUrl(List<Upload> uploads) {
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += PRESIGNED_URL_DURATION;
    expiration.setTime(expTimeMillis);

    int N = uploads.size();

    List<Boolean> isNewPhoto = new ArrayList<>(Collections.nCopies(N, true));
    // TODO: Aggregation ??? ????????? ????????? ??????
    for (int i = 0; i < N; i++) {
      isNewPhoto.set(i, !photoRepository.existsPhotoByChecksum(uploads.get(i).getChecksum()));
    }

    for (int i = 0; i < N; i++) {
      if (!isNewPhoto.get(i)) {
        continue;
      }
      try {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(bucket, uploads.get(i).getChecksum())
                .withMethod(com.amazonaws.HttpMethod.PUT)
                .withExpiration(expiration);
        generatePresignedUrlRequest.addRequestParameter("x-amz-acl", "public-read");
        URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
        uploads.get(i).setS3Url(url.toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    uploadRepository.saveAll(uploads);
  }

  public boolean markCompleted(Upload upload) {
      if (upload == null) {
          return false;
      }
    upload.setStatus(Upload.STATUS_COMPLETED);
    uploadRepository.save(upload);
    return true;
  }

  public boolean markCompleted(String pollingKey, String checksum) {
    Upload upload = uploadRepository.findByPollingKeyAndChecksum(pollingKey, checksum);
    return markCompleted(upload);
  }

  public void setTagsByFeedId(String feedId, List<String> tags, boolean propagate)
      throws IllegalArgumentException {
      if (StringUtils.isEmpty(feedId) || !ObjectId.isValid(feedId)) {
          throw new IllegalArgumentException("feedId is not valid. (feedId :" + feedId + ")");
      }

    ObjectId feedObjectId = new ObjectId(feedId);
    Optional<Feed> optionalFeed = feedRepository.findById(feedObjectId);
      if (optionalFeed.isEmpty()) {
          throw new IllegalArgumentException("There is no proper feed. (feedId :" + feedId + ")");
      }

    Feed feed = optionalFeed.get();
    Photo photo = feed.getPhoto();
      if (photo == null) {
          throw new RuntimeException("Feed does not contains photo. (feedId :" + feedId + ")");
      }
    photo.setTags(tags);
    feedRepository.save(feed);

      if (propagate) {
          photoRepository.save(photo);
      }
  }

  public void setTagsByPhotoId(String photoId, List<String> tags) throws IllegalArgumentException {
      if (StringUtils.isEmpty(photoId) || !ObjectId.isValid(photoId)) {
          throw new IllegalArgumentException("feedId is not valid. (photoId :" + photoId + ")");
      }

    ObjectId photoObjectId = new ObjectId(photoId);
    Optional<Photo> optionalPhoto = photoRepository.findById(photoObjectId);
    if (optionalPhoto.isEmpty()) {
      throw new IllegalArgumentException("There is no proper feed. (photoId :" + photoId + ")");
    }
    Photo originalPhoto = optionalPhoto.get();
    originalPhoto.setTags(tags);
    photoRepository.save(originalPhoto);
    List<Feed> feeds = feedRepository.findAllByPhotoId(photoObjectId);
    if (feeds.isEmpty()) {
      throw new IllegalArgumentException("There is no proper feed. (photoId :" + photoId + ")");
    }
    for (Feed feed : feeds) {
      Photo photo = feed.getPhoto();
        if (photo == null) {
          throw new RuntimeException(
            "Feed does not contains photo. (photoId :" + photoId + ")");
        }
      photo.setTags(tags);
      feedRepository.save(feed);
    }
  }

  public List<String> getPhotoIdsByHash(String userId, List<String> checksums) {
    int N = checksums.size();
    List<Photo> photos = new ArrayList<>(Collections.nCopies(N, null));
    // TODO: Aggregation ??? ????????? ????????? ??????
    for (int i = 0; i < N; i++) {
      Optional<Photo> optionalPhoto = photoRepository.findByChecksum(checksums.get(i));
      if (optionalPhoto.isPresent())
        photos.set(i, optionalPhoto.get());
    }
    return photos.stream().map(photo -> {
      if (photo == null)
        return null;
      return photo.getId().toString();
    }).collect(Collectors.toList());
  }

  public void synchronizeFeed() {
    List<Photo> photos = photoRepository.findAll();
    photos.forEach(photo -> {
      Optional<Feed> optionalFeed = feedRepository.findByPhotoId(photo.getId());
      if (optionalFeed.isPresent()) {
        Feed feed = optionalFeed.get();
        feed.setPhoto(photo);
        feedRepository.save(feed);
      }
    });
  }
}
