package com.minimall.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String error, String errorCode, Instant timestamp) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(false, null, errorMessage, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String errorMessage, String errorCode) {
        return new ApiResponse<>(false, null, errorMessage, errorCode, Instant.now());
    }
}