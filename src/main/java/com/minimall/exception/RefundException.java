package com.minimall.exception;

public class RefundException extends BusinessException {
    public RefundException(String message) {
        super("REFUND_ERROR", message);
    }

    public RefundException(String message, Throwable cause) {
        super("REFUND_ERROR", message, cause);
    }
}
