package edu.shehanreshin.vidstreamingpoc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record ApiCommonResponse<T>(
        Integer code,
        String message,
        T data
) {
    public static <T> ResponseEntity<ApiCommonResponse<T>> create(
            HttpStatusCode statusCode,
            Integer code,
            String message,
            T data
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(ApiCommonResponse.<T>builder()
                        .code(code)
                        .message(message)
                        .data(data)
                        .build()
                );
    }
}