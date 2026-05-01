package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeService.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SEND_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    private final UserSubscriptionRepository subscriptionRepository;
    private final WeChatSubscribeConfig config;
    private final RestTemplate restTemplate;

    public WeChatSubscribeService(UserSubscriptionRepository subscriptionRepository,
                                  WeChatSubscribeConfig config,
                                  RestTemplate restTemplate) {
        this.subscriptionRepository = subscriptionRepository;
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        String url = ACCESS_TOKEN_URL + "?grant_type=client_credential&appid=" + config.getAppId() + "&secret=" + config.getAppSecret();
        log.info("Fetching access token from: {}", url.replace(config.getAppSecret(), "***"));

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(Map.of("Content-Type", "application/json")), Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null || body.containsKey("errcode")) {
            int errcode = body != null ? (int) body.get("errcode") : -1;
            String errmsg = body != null ? (String) body.get("errmsg") : "unknown";
            throw new RuntimeException("Failed to get access token: errcode=" + errcode + ", errmsg=" + errmsg);
        }

        return (String) body.get("access_token");
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

        String token = getAccessToken();
        String url = SEND_TEMPLATE_URL + "?access_token=" + token;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("touser", openid);
        requestBody.put("template_id", templateId);
        requestBody.put("data", data);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(requestBody), Map.class);

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("errcode") && (int) body.get("errcode") != 0) {
            int errcode = (int) body.get("errcode");
            String errmsg = (String) body.get("errmsg");
            throw new RuntimeException("Failed to send template message: errcode=" + errcode + ", errmsg=" + errmsg);
        }

        log.info("Template message sent successfully to openid: {}", openid);
    }

    public record TemplateData(String value) {}
}