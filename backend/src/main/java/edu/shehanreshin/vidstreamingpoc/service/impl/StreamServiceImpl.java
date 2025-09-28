package edu.shehanreshin.vidstreamingpoc.service.impl;

import edu.shehanreshin.vidstreamingpoc.service.S3Service;
import edu.shehanreshin.vidstreamingpoc.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamServiceImpl implements StreamService {
    private final S3Service s3Service;
    private final Random random;

    @Override
    public String getVideoUri(String fileName) {
        if (!checkIfUserAuthorizedToAccessVideo()) {
            log.error("User is not authorized to access video");
            throw new RuntimeException("User is not authorized to access video");
        }

        return s3Service.getPresignedUrl(fileName);
    }

    private boolean checkIfUserAuthorizedToAccessVideo() {
        boolean isUserAuthorized = random.nextBoolean();
        log.info("Is user authorized : {}", isUserAuthorized);
        return isUserAuthorized;
    }
}