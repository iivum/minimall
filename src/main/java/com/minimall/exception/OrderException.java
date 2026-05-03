package com.minimall.exception;

public class OrderException extends BusinessException {
    public OrderException(String message) {
        super("ORDER_ERROR", message);
    }

    public OrderException(String message, Throwable cause) {
        super("ORDER_ERROR", message, cause);
    }
}