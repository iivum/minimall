package com.minimall.exception;

import com.minimall.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleUnauthorized returns FORBIDDEN with message")
    void handleUnauthorized_returnsForbidden() {
        var ex = new UnauthorizedException("Access denied");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Access denied");
        assertThat(response.getBody().errorCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("handleBusiness returns BAD_REQUEST with error code")
    void handleBusiness_returnsBadRequest() {
        var ex = new BusinessException("INVALID_INPUT", "Invalid product data");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Invalid product data");
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_INPUT");
    }

    @Test
    @DisplayName("handleOrder returns BAD_REQUEST with ORDER_ERROR code")
    void handleOrder_returnsBadRequest() {
        var ex = new OrderException("Order not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleOrder(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Order not found");
        assertThat(response.getBody().errorCode()).isEqualTo("ORDER_ERROR");
    }

    @Test
    @DisplayName("handlePayment returns BAD_REQUEST with PAYMENT_ERROR code")
    void handlePayment_returnsBadRequest() {
        var ex = new PaymentException("Payment failed");

        ResponseEntity<ApiResponse<Void>> response = handler.handlePayment(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Payment failed");
        assertThat(response.getBody().errorCode()).isEqualTo("PAYMENT_ERROR");
    }

    @Test
    @DisplayName("handleValidation returns BAD_REQUEST with VALIDATION_ERROR code")
    void handleValidation_returnsBadRequest() {
        var ex = new ValidationException("Field cannot be blank");

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Field cannot be blank");
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handleIllegalArgument returns BAD_REQUEST with BAD_REQUEST code")
    void handleIllegalArgument_returnsBadRequest() {
        var ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Invalid argument");
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("handleGeneric returns INTERNAL_SERVER_ERROR without leaking details")
    void handleGeneric_returnsInternalServerError() {
        var ex = new RuntimeException("Database connection failed");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isEqualTo("Internal server error");
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_ERROR");
    }
}