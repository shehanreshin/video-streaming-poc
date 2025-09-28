package edu.shehanreshin.vidstreamingpoc.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

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
    public ResponseEntity<StreamingResponseBody> getVideo(
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

        if (StringUtils.isNotBlank(rangeHeader) && rangeHeader.startsWith(BYTES)) {
            String[] ranges = rangeHeader.replace(BYTES, "").split("-");
            rangeStart = Long.parseLong(ranges[0]);
            rangeEnd = ranges.length > 1 && StringUtils.isNotBlank(ranges[1]) ?
                    Long.parseLong(ranges[1]) : fileSize - 1;
        }

        log.info("range start : {}", rangeStart);
        log.info("range end : {}", rangeEnd);

        if (rangeStart >= fileSize) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        long contentLength = rangeEnd - rangeStart + 1;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(RECORDING_PREFIX + fileName)
                .range(BYTES + rangeStart + "-" + rangeEnd)
                .build();

        final StreamingResponseBody responseBody = outputStream -> {
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            int numberOfBytesToWrite = 0;
            byte[] data = new byte[1024];
            while ((numberOfBytesToWrite = s3Object.read(data, 0, data.length)) != -1) {
                outputStream.write(data);
            }
            s3Object.close();
        };

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseBody);
    }

    @GetMapping
    public String test() {
        return "test";
    }
}