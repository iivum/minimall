package com.minimall.service;

import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import com.minimall.model.RefundRequest;
import com.minimall.repository.OrderRepository;
import com.minimall.repository.RefundRepository;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class WeChatRefundService {
    private static final Logger log = LoggerFactory.getLogger(WeChatRefundService.class);

    private final WeChatPayConfig weChatPayConfig;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final RefundService refundService;

    public WeChatRefundService(WeChatPayConfig weChatPayConfig,
                               RefundRepository refundRepository,
                               OrderRepository orderRepository) {
        this.weChatPayConfig = weChatPayConfig;
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.refundService = createRefundService();
    }

    private RefundService createRefundService() {
        try {
            Config config = weChatPayConfig.rsaAutoCertificateConfig();
            return new RefundService.Builder().config(config).build();
        } catch (Exception e) {
            log.error("Failed to create WeChat Pay RefundService: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize WeChat Pay refund service", e);
        }
    }

    @Transactional
    public void executeRefund(RefundRequest refund) {
        try {
            Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + refund.getOrderId()));

            CreateRequest request = new CreateRequest();
            request.setTransactionId(order.getTradeNo());
            request.setOutRefundNo("REFUND_" + refund.getOrderNo());
            request.setReason(refund.getReason());
            request.setNotifyUrl(weChatPayConfig.getCallbackUrl() + "/api/refunds/callback");

            AmountReq amount = new AmountReq();
            amount.setRefund(refund.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
            amount.setTotal(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue());
            amount.setCurrency("CNY");
            request.setAmount(amount);

            Refund response = refundService.create(request);

            refund.setWechatRefundNo(response.getRefundId());
            refund.setWechatRefundTime(Instant.now());
            refund.setStatus(RefundRequest.Status.COMPLETED);
            refundRepository.save(refund);

            order.setPayStatus(Order.PayStatus.REFUNDED);
            order.setStatus(Order.Status.REFUNDED);
            orderRepository.save(order);

            log.info("WeChat refund created successfully: refundId={}, outRefundNo={}",
                response.getRefundId(), "REFUND_" + refund.getOrderNo());

        } catch (Exception e) {
            log.error("Refund execution failed: {}", e.getMessage());
            refund.setStatus(RefundRequest.Status.FAILED);
            refund.setRejectReason("Refund execution failed: " + e.getMessage());
            refundRepository.save(refund);
            throw new RuntimeException("Refund execution failed", e);
        }
    }
}
