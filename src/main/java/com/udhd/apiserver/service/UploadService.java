package com.udhd.apiserver.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UploadService {
    private final AmazonS3Client amazonS3Client;
    private final PhotoRepository photoRepository;
    private final int PRESIGNED_URL_DURATION = 1000 * 60 * 5;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * s3 pre-signed url들을 반환한다.
     * 리턴값의 i번째 값은
     *   i번째 사진이 새 사진인 경우 presigned url이고,
     *   i번째 사진이 기존에 있던 사진인 경우 null 이다.
     *
     * @param checksums the checksums
     * @return the pre signed urls
     */
    public List<String> getPreSignedURLs(List<String> checksums) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += PRESIGNED_URL_DURATION;
        expiration.setTime(expTimeMillis);

        int N = checksums.size();

        List<Boolean> isNewPhoto = new ArrayList<>(Collections.nCopies(N, true));
        // TODO: Aggregation 을 이용한 쿼리로 변경
        for (int i = 0; i < N; i++) {
            isNewPhoto.set(i, !photoRepository.existsPhotoByChecksum(checksums.get(i)));
        }

        List<String> urls = new ArrayList<>(Collections.nCopies(N, null));
        for (int i = 0; i < N; i++) {
            if (!isNewPhoto.get(i)) {
                continue;
            }
            try {
                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                        new GeneratePresignedUrlRequest(bucket, checksums.get(i))
                                .withMethod(HttpMethod.PUT)
                                .withExpiration(expiration);
                URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
                urls.set(i, url.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
