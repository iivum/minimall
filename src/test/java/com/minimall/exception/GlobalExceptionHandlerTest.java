package com.minimall.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("NoHandlerFoundException returns 404 with NOT_FOUND error code")
    void handleNoHandlerFound_returns404() throws NoHandlerFoundException {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/unknown", null);
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleNoHandlerFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("1003");
        assertThat(response.getBody().message()).contains("/unknown");
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException returns 400 with PARAMETER_TYPE_MISMATCH error code")
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "not-a-number", Integer.class, "id", null, null);
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("6003");
    }

    @Test
    @DisplayName("MissingServletRequestParameterException returns 400 with MISSING_PARAMETER error code")
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("page", "int");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("6002");
        assertThat(response.getBody().message()).contains("page");
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException returns 405 with METHOD_NOT_ALLOWED error code")
    void handleMethodNotSupported_returns405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("1004");
        assertThat(response.getBody().message()).contains("DELETE");
    }

    @Test
    @DisplayName("BusinessException returns 400 with custom error code")
    void handleBusinessException_returns400() {
        BusinessException ex = new BusinessException("CUSTOM_ERROR", "Custom error message");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CUSTOM_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Custom error message");
    }

    @Test
    @DisplayName("IllegalArgumentException returns 400 with INVALID_PARAMETER error code")
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid id provided");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("6001");
        assertThat(response.getBody().message()).isEqualTo("Invalid id provided");
    }

    @Test
    @DisplayName("RuntimeException with 'not found' message returns 404")
    void handleRuntime_withNotFound_returns404() {
        RuntimeException ex = new RuntimeException("Order not found");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleRuntime(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("1003");
    }

    @Test
    @DisplayName("Generic RuntimeException returns 500 with INTERNAL_ERROR")
    void handleRuntime_generic_returns500() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleRuntime(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("1005");
    }

    @Test
    @DisplayName("UnauthorizedException returns 403 with FORBIDDEN error code")
    void handleUnauthorized_returns403() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        ResponseEntity<GlobalExceptionHandler.ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("5002");
    }

    @Test
    @DisplayName("ApiResponse.success returns correct structure")
    void apiResponse_success() {
        var response = GlobalExceptionHandler.ApiResponse.success("test data");

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("test data");
    }

    @Test
    @DisplayName("ApiResponse.error with ErrorCode returns correct structure")
    void apiResponse_errorWithErrorCode() {
        var response = GlobalExceptionHandler.ApiResponse.error(ErrorCode.NOT_FOUND);

        assertThat(response.code()).isEqualTo("1003");
        assertThat(response.message()).isEqualTo("Resource not found");
        assertThat(response.data()).isNull();
    }
}