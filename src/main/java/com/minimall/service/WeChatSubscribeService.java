package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeService.class);

    private final UserSubscriptionRepository subscriptionRepository;
    private final WeChatSubscribeConfig config;

    public WeChatSubscribeService(UserSubscriptionRepository subscriptionRepository,
                                  WeChatSubscribeConfig config) {
        this.subscriptionRepository = subscriptionRepository;
        this.config = config;
    }

    public void sendOrderCreatedMessage(Order order, User user) {
        UserSubscription sub = subscriptionRepository.findByOpenid(user.getOpenid()).orElse(null);
        if (sub == null || !sub.isOrderCreatedEnabled()) {
            log.info("User {} has not subscribed to order created messages", user.getOpenid());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));
        data.put("amount", new TemplateData(order.getTotalAmount().toString() + "元"));

        sendTemplateMessage(user.getOpenid(), config.getOrderCreatedTemplateId(), data);
    }

    public void sendOrderPaidMessage(Order order, User user) {
        UserSubscription sub = subscriptionRepository.findByOpenid(user.getOpenid()).orElse(null);
        if (sub == null || !sub.isOrderPaidEnabled()) {
            log.info("User {} has not subscribed to order paid messages", user.getOpenid());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));
        data.put("amount", new TemplateData(order.getTotalAmount().toString() + "元"));

        sendTemplateMessage(user.getOpenid(), config.getOrderPaidTemplateId(), data);
    }

    public void sendOrderShippedMessage(Order order, User user, String expressNo) {
        UserSubscription sub = subscriptionRepository.findByOpenid(user.getOpenid()).orElse(null);
        if (sub == null || !sub.isOrderShippedEnabled()) {
            log.info("User {} has not subscribed to order shipped messages", user.getOpenid());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("express_no", new TemplateData(expressNo));
        data.put("order_no", new TemplateData(order.getOrderNo()));

        sendTemplateMessage(user.getOpenid(), config.getOrderShippedTemplateId(), data);
    }

    public void sendOrderCompletedMessage(Order order, User user) {
        UserSubscription sub = subscriptionRepository.findByOpenid(user.getOpenid()).orElse(null);
        if (sub == null || !sub.isOrderCompletedEnabled()) {
            log.info("User {} has not subscribed to order completed messages", user.getOpenid());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));

        sendTemplateMessage(user.getOpenid(), config.getOrderCompletedTemplateId(), data);
    }

    private void sendTemplateMessage(String openid, String templateId, Map<String, Object> data) {
        log.info("Sending template message to openid: {}, template: {}", openid, templateId);
        // TODO: Implement actual WeChat API call
        // WeChat subscription message API requires:
        // 1. Get access token using appid and appsecret
        // 2. Call subscribeMessage.send API
    }

    public record TemplateData(String value) {}
}