package edu.shehanreshin.vidstreamingpoc.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.OutputStreamWriter;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
@Slf4j
public class UtilityController {
    public static final String CONTENT_RANGE_FORMAT = "bytes %d-%d/%d";
    public static final String RECORDING_PREFIX = "streaming/";
    private final S3Client s3Client;

    @Value("${config.aws.bucket-name}")
    private String bucketName;
    @Value("${const.video.max-chunk-size-in-kb}")
    private Integer maxChunkSizeInKb;

    public static final String BYTES = "bytes=";

    @GetMapping("/video/{fileName}")
    public ResponseEntity<Resource> getVideo(
            @PathVariable String fileName,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) {
        log.info("fileName : {}  ::  range : {}", fileName, rangeHeader);

        HeadObjectResponse metadata;
        try {
            metadata = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(RECORDING_PREFIX + fileName)
                            .build()
            );
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            log.warn("File not found in S3: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long fileSize = metadata.contentLength();

        log.info("fileSize : {}", fileSize);

        long rangeStart = 0;
        long rangeEnd = 0;

        boolean isNotFullRangeRequest = false;

        if (StringUtils.isNotBlank(rangeHeader) && rangeHeader.startsWith(BYTES)) {
            String[] ranges = rangeHeader.replace(BYTES, "").split("-");
            rangeStart = Long.parseLong(ranges[0]);

            if (ranges.length > 1 && StringUtils.isNotBlank(ranges[1])) isNotFullRangeRequest = true;
            rangeEnd = isNotFullRangeRequest ?
                    Long.parseLong(ranges[1]) : Math.min(rangeStart + maxChunkSizeInKb * 1000 - 1, fileSize - 1);
        }

        long contentLength = rangeEnd - rangeStart + 1;
        log.info("range start : {}", rangeStart);
        log.info("range end : {}", rangeEnd);
        log.info("content length : {}", contentLength);

        if (rangeStart >= fileSize) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(RECORDING_PREFIX + fileName)
                .range(BYTES + rangeStart + "-" + rangeEnd)
                .build();

        byte[] data;

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            data = s3Object.readAllBytes();
        } catch (Exception e) {
            log.error("Error fetching video chunk from S3: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, "video/webm")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, String.format(CONTENT_RANGE_FORMAT, rangeStart, rangeEnd, fileSize))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(new ByteArrayResource(data));
    }

    @GetMapping
    public String test() {
        return "test";
    }
}