package edu.shehanreshin.vidstreamingpoc.config;

import edu.shehanreshin.vidstreamingpoc.dto.ApiCommonResponse;
import edu.shehanreshin.vidstreamingpoc.util.AppConstantCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiCommonResponse<Object>> handleException(Exception ex) {
        return ApiCommonResponse.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                AppConstantCollection.DEFAULT_ERROR_CODE,
                ex.getMessage(),
                null
        );
    }
}