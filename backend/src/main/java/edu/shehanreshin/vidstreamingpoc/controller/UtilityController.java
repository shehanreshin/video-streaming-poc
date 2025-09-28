package edu.shehanreshin.vidstreamingpoc.controller;


import edu.shehanreshin.vidstreamingpoc.dto.ApiCommonResponse;
import edu.shehanreshin.vidstreamingpoc.service.StreamService;
import edu.shehanreshin.vidstreamingpoc.util.AppConstantCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
@Slf4j
public class UtilityController {
    private final StreamService streamService;

    private static final String VIDEO_URI_FETCH_SUCCESS_MESSAGE = "Video uri fetched successfully";

    @GetMapping("/video/{fileName}")
    public ResponseEntity<ApiCommonResponse<String>> getVideoUri(
            @PathVariable(name = "fileName") String fileName
    ) {
        log.info("Request received for video uri  ::  file name : {}", fileName);
        return ResponseEntity.ok(
                ApiCommonResponse.<String>builder()
                        .code(AppConstantCollection.DEFAULT_SUCCESS_CODE)
                        .message(VIDEO_URI_FETCH_SUCCESS_MESSAGE)
                        .data(streamService.getVideoUri(fileName))
                        .build()
        );
    }
}