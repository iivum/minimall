package com.minimall.controller;

import com.minimall.model.Order;
import com.minimall.service.OrderService;
import com.minimall.service.PayService;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> createPayRequest(
            @PathVariable String orderId,
            @RequestParam String openid) {

        Order order = orderService.findById(orderId);
        PrepayWithRequestPaymentResponse response = payService.createUnifiedOrder(order, openid);
        Map<String, String> paymentParams = payService.getJsApiSign(response);

        return ResponseEntity.ok(paymentParams);
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
