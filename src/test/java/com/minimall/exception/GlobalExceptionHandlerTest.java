package com.minimall.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void apiResponse_ok_createsSuccessResponse() {
        var response = com.minimall.dto.ApiResponse.ok("data");
        assertTrue(response.success());
        assertEquals("data", response.data());
        assertNull(response.error());
    }

    @Test
    void apiResponse_error_createsErrorResponse() {
        var response = com.minimall.dto.ApiResponse.error("error message");
        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("error message", response.error());
    }

    @Test
    void apiResponse_errorWithCode_createsErrorResponseWithCode() {
        var response = com.minimall.dto.ApiResponse.error("ERROR_CODE", "error message");
        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("error message", response.error());
        assertEquals("ERROR_CODE", response.errorCode());
    }
}