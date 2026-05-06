package com.minimall.service;

import com.minimall.dto.RefundRequestDTO;
import com.minimall.exception.OrderException;
import com.minimall.exception.RefundException;
import com.minimall.model.Order;
import com.minimall.model.RefundRequest;
import com.minimall.model.User;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.RefundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RefundService {
    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final WeChatRefundService weChatRefundService;

    public RefundService(RefundRepository refundRepository,
                        OrderRepository orderRepository,
                        OrderService orderService,
                        UserService userService,
                        WeChatRefundService weChatRefundService) {
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.userService = userService;
        this.weChatRefundService = weChatRefundService;
    }

    public List<RefundRequestDTO> findByUserId(String userId) {
        return refundRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(RefundRequestDTO::from)
            .collect(Collectors.toList());
    }

    public RefundRequestDTO findById(String id) {
        return refundRepository.findById(id)
            .map(RefundRequestDTO::from)
            .orElseThrow(() -> new RefundException("Refund request not found: " + id));
    }

    public List<RefundRequestDTO> findAll() {
        return refundRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(RefundRequestDTO::from)
            .collect(Collectors.toList());
    }

    public List<RefundRequestDTO> findByStatus(RefundRequest.Status status) {
        return refundRepository.findByStatus(status)
            .stream()
            .map(RefundRequestDTO::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public RefundRequestDTO create(String orderId, String userId, BigDecimal amount, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new RefundException("Order does not belong to user");
        }

        if (order.getPayStatus() != Order.PayStatus.PAID) {
            throw new RefundException("Order is not paid, cannot refund");
        }

        if (order.getStatus() == Order.Status.REFUNDED || order.getStatus() == Order.Status.CANCELLED) {
            throw new RefundException("Order already refunded or cancelled");
        }

        BigDecimal refundAmount = amount;
        if (amount.compareTo(order.getTotalAmount()) > 0) {
            refundAmount = order.getTotalAmount();
        }

        RefundRequest refund = new RefundRequest();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setOrderNo(order.getOrderNo());
        refund.setAmount(refundAmount);
        refund.setReason(reason);
        refund.setStatus(RefundRequest.Status.PENDING);

        RefundRequest saved = refundRepository.save(refund);
        log.info("Created refund request: {} for order: {}", saved.getId(), order.getOrderNo());

        return RefundRequestDTO.from(saved);
    }

    @Transactional
    public RefundRequestDTO approve(String id, String adminNote) {
        RefundRequest refund = refundRepository.findById(id)
            .orElseThrow(() -> new RefundException("Refund request not found: " + id));

        if (refund.getStatus() != RefundRequest.Status.PENDING) {
            throw new RefundException("Refund request is not pending, current status: " + refund.getStatus());
        }

        refund.setStatus(RefundRequest.Status.APPROVED);
        refund.setAdminNote(adminNote);

        RefundRequest saved = refundRepository.save(refund);
        log.info("Approved refund request: {}", id);

        weChatRefundService.executeRefund(refund);

        return RefundRequestDTO.from(saved);
    }

    @Transactional
    public RefundRequestDTO reject(String id, String rejectReason) {
        RefundRequest refund = refundRepository.findById(id)
            .orElseThrow(() -> new RefundException("Refund request not found: " + id));

        if (refund.getStatus() != RefundRequest.Status.PENDING) {
            throw new RefundException("Refund request is not pending, current status: " + refund.getStatus());
        }

        refund.setStatus(RefundRequest.Status.REJECTED);
        refund.setRejectReason(rejectReason);

        RefundRequest saved = refundRepository.save(refund);
        log.info("Rejected refund request: {} with reason: {}", id, rejectReason);

        return RefundRequestDTO.from(saved);
    }

    @Transactional
    public void markAsCompleted(String refundId, String wechatRefundNo) {
        RefundRequest refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RefundException("Refund request not found: " + refundId));

        refund.setStatus(RefundRequest.Status.COMPLETED);
        refund.setWechatRefundNo(wechatRefundNo);
        refund.setWechatRefundTime(Instant.now());
        refundRepository.save(refund);

        Order order = orderRepository.findById(refund.getOrderId())
            .orElseThrow(() -> new OrderException("Order not found: " + refund.getOrderId()));
        order.setPayStatus(Order.PayStatus.REFUNDED);
        order.setStatus(Order.Status.REFUNDED);
        orderRepository.save(order);

        log.info("Refund completed: {} for order: {}", refundId, refund.getOrderNo());
    }

    @Transactional
    public void markAsFailed(String refundId, String reason) {
        RefundRequest refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RefundException("Refund request not found: " + refundId));

        refund.setStatus(RefundRequest.Status.FAILED);
        refund.setRejectReason(reason);
        refundRepository.save(refund);

        log.error("Refund failed: {} reason: {}", refundId, reason);
    }
}
