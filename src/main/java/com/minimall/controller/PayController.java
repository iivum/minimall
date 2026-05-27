package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import com.minimall.service.PayService;
import com.wechat.pay.java.service.refund.model.Refund;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pay")
@Tag(name = "Payment", description = "WeChat Pay APIs")
public class PayController {
    private final PayService payService;
    private final OrderService orderService;

    public PayController(PayService payService, OrderService orderService) {
        this.payService = payService;
        this.orderService = orderService;
    }

    @PostMapping("/create/{orderId}")
    @Operation(summary = "Create unified order and get prepay_id")
    public ResponseEntity<Map<String, Object>> createPayRequest(@PathVariable String orderId, @RequestParam String openid) {
        Order order = orderService.findById(orderId);
        String prepayId = payService.createUnifiedOrder(order, openid);

        long timestamp = System.currentTimeMillis() / 1000;
        String nonceStr = UUID.randomUUID().toString();
        String sign = payService.getJsApiSign(prepayId, timestamp, nonceStr);

        Map<String, Object> result = new HashMap<>();
        result.put("timeStamp", timestamp);
        result.put("nonceStr", nonceStr);
        result.put("package", "prepay_id=" + prepayId);
        result.put("signType", "RSA");
        result.put("paySign", sign);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/refund")
    @Operation(summary = "Process refund for a paid order")
    public ResponseEntity<Map<String, Object>> refund(@RequestParam String orderId,
                                                        @RequestParam BigDecimal refundAmount) {
        Order order = orderService.findById(orderId);

        if (order.getPayStatus() != Order.PayStatus.PAID) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "ORDER_NOT_PAID");
            error.put("message", "Only paid orders can be refunded");
            return ResponseEntity.badRequest().body(error);
        }

        if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "REFUND_AMOUNT_EXCEEDED");
            error.put("message", "Refund amount cannot exceed order total amount");
            return ResponseEntity.badRequest().body(error);
        }

        Refund result = payService.refund(order, refundAmount);
        orderService.refund(orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("refundAmount", refundAmount);
        response.put("refundId", result.getRefundId());
        response.put("status", result.getStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    @Operation(summary = "WeChat Pay callback notification")
    public ResponseEntity<String> handleCallback(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Serial") String serialNo) {

        if (!payService.verifyCallback(body, signature, serialNo)) {
            return ResponseEntity.badRequest().body("VERIFY_FAILED");
        }

        try {
            payService.processCallback(body);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.ok("FAIL");
        }
    }
}
