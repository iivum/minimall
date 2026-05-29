package com.minimall.exception;

public enum ErrorCode {
    // Common errors (1xxx)
    INVALID_REQUEST("1001", "Invalid request"),
    VALIDATION_ERROR("1002", "Validation failed"),
    NOT_FOUND("1003", "Resource not found"),
    METHOD_NOT_ALLOWED("1004", "HTTP method not supported"),
    INTERNAL_ERROR("1005", "Internal server error"),

    // Order errors (2xxx)
    ORDER_NOT_FOUND("2001", "Order not found"),
    ORDER_STATUS_ERROR("2002", "Order status error"),
    ORDER_CREATE_ERROR("2003", "Failed to create order"),

    // Payment errors (3xxx)
    PAYMENT_ERROR("3001", "Payment error"),
    PAYMENT_SIGNATURE_ERROR("3002", "Payment signature verification failed"),
    PAYMENT_REFUND_ERROR("3003", "Payment refund failed"),

    // Business errors (4xxx)
    BUSINESS_ERROR("4001", "Business error"),
    INSUFFICIENT_STOCK("4002", "Insufficient stock"),
    COUPON_EXPIRED("4003", "Coupon has expired"),
    COUPON_ALREADY_USED("4004", "Coupon has already been used"),

    // Auth errors (5xxx)
    UNAUTHORIZED("5001", "Unauthorized access"),
    FORBIDDEN("5002", "Access denied"),
    TOKEN_EXPIRED("5003", "Token has expired"),

    // Validation errors (6xxx)
    INVALID_PARAMETER("6001", "Invalid parameter"),
    MISSING_PARAMETER("6002", "Missing required parameter"),
    PARAMETER_TYPE_MISMATCH("6003", "Parameter type mismatch");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}