package com.minimall.service;

import com.minimall.dto.PaymentResponse;
import com.minimall.exception.PaymentException;
import com.minimall.model.Order;
import com.minimall.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final PayService payService;
    private final Map<String, PaymentResponse> paymentStore = new ConcurrentHashMap<>();

    public PaymentService(OrderRepository orderRepository, PayService payService) {
        this.orderRepository = orderRepository;
        this.payService = payService;
    }

    @Transactional
    public PaymentResponse initiatePayment(String orderId, BigDecimal amount) {
        log.info("Initiating payment for order: {}, amount: {}", orderId, amount);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new PaymentException("Order not found: " + orderId));

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("Payment initiated: {} for order: {}", paymentId, orderId);
        return new PaymentResponse(
            paymentId,
            orderId,
            amount,
            "PENDING",
            null,
            Instant.now(),
            null
        );
    }

    public PaymentResponse getPaymentStatus(String paymentId) {
        log.info("Getting payment status for: {}", paymentId);

        PaymentResponse stored = paymentStore.get(paymentId);
        if (stored != null) {
            return stored;
        }

        if (paymentId.startsWith("PAY-")) {
            return new PaymentResponse(
                paymentId,
                null,
                null,
                "PENDING",
                null,
                Instant.now(),
                null
            );
        }

        throw new PaymentException("Payment not found: " + paymentId);
    }

    @Transactional
    public void processCallback(String transactionId, String status) {
        log.info("Processing callback: transactionId={}, status={}", transactionId, status);

        if ("SUCCESS".equals(status)) {
            orderRepository.findAll().stream()
                .filter(o -> o.getTradeNo() != null && o.getTradeNo().equals(transactionId))
                .findFirst()
                .ifPresent(order -> {
                    order.setPayStatus(Order.PayStatus.PAID);
                    order.setStatus(Order.Status.PAID);
                    order.setPayTime(Instant.now());
                    orderRepository.save(order);
                    log.info("Order updated to PAID: {}", order.getId());
                });
        }

        paymentStore.values().stream()
            .filter(p -> transactionId.equals(p.transactionId()))
            .findFirst()
            .ifPresent(payment -> {
                PaymentResponse updated = new PaymentResponse(
                    payment.paymentId(),
                    payment.orderId(),
                    payment.amount(),
                    status,
                    transactionId,
                    payment.createdAt(),
                    "SUCCESS".equals(status) ? Instant.now() : null
                );
                paymentStore.put(payment.paymentId(), updated);
            });

        log.info("Callback processed successfully");
    }
}