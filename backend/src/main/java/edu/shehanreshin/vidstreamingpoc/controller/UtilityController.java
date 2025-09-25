package edu.shehanreshin.vidstreamingpoc.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
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

import java.util.Objects;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
@Slf4j
public class UtilityController {
    public static final String CONTENT_RANGE_FORMAT = "bytes %d-%d/%d";
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
        HeadObjectResponse metadata = s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucketName).key(fileName).build()
        );
        long fileSize = metadata.contentLength();

        long rangeStart = 0;
        long rangeEnd = fileSize - 1;

        boolean fullFileRequested = false;

        if (StringUtils.isNoneBlank(rangeHeader) && rangeHeader.startsWith(BYTES)) {
            String[] ranges = rangeHeader.replace(BYTES, "").split("-");
            rangeStart = Long.parseLong(ranges[0]);

            if (ranges.length > 1 && StringUtils.isNoneBlank(ranges[1])) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                fullFileRequested = true;
            }
        }

        if (fullFileRequested) {
            rangeEnd = Math.min(rangeStart + (maxChunkSizeInKb * 1024) - 1, fileSize - 1);
        }

        long contentLength = rangeEnd - rangeStart + 1;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .range(BYTES + rangeStart + "-" + rangeEnd)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

        return ResponseEntity
                .status(Objects.nonNull(rangeHeader) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
                .contentLength(contentLength)
                .contentType(MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(
                        HttpHeaders.CONTENT_RANGE,
                        String.format(CONTENT_RANGE_FORMAT, rangeStart, rangeEnd, fileSize)
                )
                .body(new InputStreamResource(s3Object));
    }
}