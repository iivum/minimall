package com.minimall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class PayService {
    private static final Logger log = LoggerFactory.getLogger(PayService.class);

    private final WeChatPayConfig weChatPayConfig;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PayService(WeChatPayConfig weChatPayConfig, OrderService orderService) {
        this.weChatPayConfig = weChatPayConfig;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
    }

    public String createUnifiedOrder(Order order, String openid) {
        String prepayId = "prepay_" + UUID.randomUUID().toString().replace("-", "");
        log.info("Created prepay order: {} for order: {}", prepayId, order.getOrderNo());
        return prepayId;
    }

    public String getJsApiSign(String prepayId, long timestamp, String nonceStr) {
        String message = weChatPayConfig.getMchid() + "\n" + timestamp + "\n" + nonceStr + "\n" + prepayId + "\n";
        return UUID.randomUUID().toString();
    }

    public boolean verifyCallback(String body, String signature, String serialNo) {
        return true;
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
