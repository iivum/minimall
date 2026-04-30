package com.minimall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.PaymentsService;
import com.wechat.pay.java.service.payments.model.*;
import com.wechat.pay.java.core.http.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PayService {
    private static final Logger log = LoggerFactory.getLogger(PayService.class);

    private final RSAAutoCertificateConfig config;
    private final WeChatPayConfig weChatPayConfig;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private PaymentsService paymentsService;

    public PayService(RSAAutoCertificateConfig config, WeChatPayConfig weChatPayConfig, OrderService orderService) {
        this.config = config;
        this.weChatPayConfig = weChatPayConfig;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
        initPaymentsService();
    }

    private void initPaymentsService() {
        if (weChatPayConfig.isSandbox()) {
            this.paymentsService = new PaymentsService.Builder()
                .config(config)
                .environment(Environment.SANDBOX)
                .build();
        } else {
            this.paymentsService = new PaymentsService.Builder()
                .config(config)
                .environment(Environment.PRODUCTION)
                .build();
        }
    }

    public String createUnifiedOrder(Order order, String openid) {
        com.wechat.pay.java.service.payments.model.PrepayRequest request = new PrepayRequest();

        Amount amount = new Amount();
        amount.setTotal(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        request.setAppid(config.getMchId());
        request.setMchid(config.getMchId());
        request.setDescription("MiniMall Order: " + order.getOrderNo());
        request.setNotifyUrl(weChatPayConfig.getCallbackUrl());
        request.setOutTradeNo(order.getOrderNo());

        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        try {
            PrepayResponse response = paymentsService.prepay(request);
            return response.getPrepayId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unified order: " + e.getMessage(), e);
        }
    }

    public String getJsApiSign(String prepayId, long timestamp, String nonceStr) {
        try {
            String message = config.getMchId() + "\n" + timestamp + "\n" + nonceStr + "\n" + prepayId + "\n";
            return com.wechat.pay.java.core.util.PemUtil.encodeBase64(
                com.wechat.pay.java.core.util.SignatureUtil.sign(
                    config.getPrivateKey(),
                    "RSA256",
                    message.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JSAPI payload: " + e.getMessage(), e);
        }
    }

    public boolean verifyCallback(String body, String signature, String serialNo) {
        try {
            return paymentsService.callback(body, signature, serialNo);
        } catch (Exception e) {
            log.error("Callback verification failed: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void processCallback(String body) {
        try {
            Map<String, Object> notification = objectMapper.readValue(body, Map.class);
            Map<String, Object> payload = (Map<String, Object>) notification.get("resource");
            String outTradeNo = (String) payload.get("out_trade_no");
            String tradeNo = (String) payload.get("transaction_id");
            String tradeStatus = (String) ((Map<String, Object>) payload.get("amount")).get("state");

            Order order = orderService.findByOrderNo(outTradeNo);
            if ("SUCCESS".equals(tradeStatus)) {
                orderService.pay(order.getId(), tradeNo);
                log.info("Order paid successfully: {}", outTradeNo);
            } else {
                log.warn("Payment failed for order: {}, status: {}", outTradeNo, tradeStatus);
            }
        } catch (Exception e) {
            log.error("Failed to process callback: {}", e.getMessage());
            throw new RuntimeException("Failed to process callback", e);
        }
    }
}
