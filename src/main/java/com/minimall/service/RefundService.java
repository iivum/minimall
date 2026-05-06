package com.minimall.service;

import com.minimall.dto.RefundApplicationRequest;
import com.minimall.dto.RefundApprovalRequest;
import com.minimall.dto.RefundResponse;
import com.minimall.model.Order;
import com.minimall.model.RefundRequest;
import com.minimall.model.User;
import com.minimall.repository.RefundRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class RefundService {
    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRequestRepository refundRequestRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final PayService payService;
    private final WeChatSubscribeService subscribeService;

    public RefundService(RefundRequestRepository refundRequestRepository,
                         OrderService orderService,
                         UserService userService,
                         PayService payService,
                         WeChatSubscribeService subscribeService) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderService = orderService;
        this.userService = userService;
        this.payService = payService;
        this.subscribeService = subscribeService;
    }

    @Transactional
    public RefundResponse applyForRefund(String orderId, String userId, RefundApplicationRequest request) {
        Order order = orderService.findById(orderId);

        if (order.getPayStatus() != Order.PayStatus.PAID) {
            throw new RuntimeException("Only paid orders can be refunded");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only request refund for your own orders");
        }

        if (request.amount().compareTo(order.getTotalAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed order total amount");
        }

        if (refundRequestRepository.findByOrderIdAndStatus(orderId, RefundRequest.Status.PENDING).isPresent()) {
            throw new RuntimeException("There is already a pending refund request for this order");
        }

        RefundRequest refund = new RefundRequest();
        refund.setOrder(order);
        refund.setAmount(request.amount());
        refund.setReason(request.reason());
        refund.setStatus(RefundRequest.Status.PENDING);

        RefundRequest saved = refundRequestRepository.save(refund);
        log.info("Refund request created for order: {}, amount: {}", orderId, request.amount());

        return RefundResponse.from(saved);
    }

    @Transactional
    public RefundResponse approveRefund(String refundId, String adminId, RefundApprovalRequest request) {
        RefundRequest refund = refundRequestRepository.findById(refundId)
            .orElseThrow(() -> new RuntimeException("Refund request not found: " + refundId));

        if (refund.getStatus() != RefundRequest.Status.PENDING) {
            throw new RuntimeException("Only pending refund requests can be processed");
        }

        User admin = userService.findById(adminId);
        if (admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only administrators can approve refunds");
        }

        refund.setAdmin(admin);
        refund.setAdminComment(request.adminComment());
        refund.setProcessedAt(Instant.now());

        if (request.approved()) {
            refund.setStatus(RefundRequest.Status.APPROVED);
            executeRefund(refund);
        } else {
            refund.setStatus(RefundRequest.Status.REJECTED);
        }

        RefundRequest saved = refundRequestRepository.save(refund);
        log.info("Refund request {} processed: {}", refundId, saved.getStatus());

        return RefundResponse.from(saved);
    }

    private void executeRefund(RefundRequest refund) {
        Order order = refund.getOrder();

        try {
            boolean success = payService.processRefund(order, refund.getAmount());

            if (success) {
                refund.setStatus(RefundRequest.Status.COMPLETED);
                order.setPayStatus(Order.PayStatus.REFUNDED);
                order.setStatus(Order.Status.CANCELLED);
                orderService.save(order);

                subscribeService.sendRefundCompletedMessage(refund, order.getUser());
                log.info("Refund executed successfully for order: {}", order.getOrderNo());
            } else {
                log.error("Refund execution failed for order: {}", order.getOrderNo());
            }
        } catch (Exception e) {
            log.error("Error executing refund for order: {}", order.getOrderNo(), e);
            throw new RuntimeException("Failed to execute refund: " + e.getMessage());
        }
    }

    public List<RefundResponse> getPendingRefunds() {
        return refundRequestRepository.findByStatus(RefundRequest.Status.PENDING)
            .stream()
            .map(RefundResponse::from)
            .toList();
    }

    public List<RefundResponse> getRefundsByOrder(String orderId) {
        return refundRequestRepository.findByOrderId(orderId)
            .stream()
            .map(RefundResponse::from)
            .toList();
    }

    public RefundResponse getRefundById(String refundId) {
        return refundRequestRepository.findById(refundId)
            .map(RefundResponse::from)
            .orElseThrow(() -> new RuntimeException("Refund request not found: " + refundId));
    }
}