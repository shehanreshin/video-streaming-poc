package edu.shehanreshin.vidstreamingpoc.service.impl;

import edu.shehanreshin.vidstreamingpoc.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {
    public static final String RECORDING_PREFIX = "streaming/";
    private final S3Presigner s3Presigner;

    @Value("${config.aws.bucket-name}")
    private String bucketName;
    @Value("${const.video.presign-expiry-duration}")
    private Integer presignExpiryDuration;

    @Override
    public String getPresignedUrl(String fileName) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpiryDuration))
                .getObjectRequest(gr -> gr.bucket(bucketName).key(RECORDING_PREFIX + fileName))
                .build();

        return Optional.ofNullable(s3Presigner.presignGetObject(presignRequest).url())
                .map(URL::toString)
                .orElseThrow(() -> {
                    log.error("Failed to generate presigned URL");
                    return new RuntimeException("Failed to generate presigned URL");
                });
    }
}