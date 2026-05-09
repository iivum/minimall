package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WeChatSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeService.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SUBSCRIBE_MESSAGE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/template/subscribe";
    private static final Duration TOKEN_EXPIRY_BUFFER = Duration.ofMinutes(5);
    private static final Duration TOKEN_FETCH_TIMEOUT = Duration.ofSeconds(10);

    private final UserSubscriptionRepository subscriptionRepository;
    private final WeChatSubscribeConfig config;
    private final WebClient webClient;

    private String cachedAccessToken;
    private Instant tokenExpiry;
    private final ReentrantLock tokenLock = new ReentrantLock();

    public WeChatSubscribeService(UserSubscriptionRepository subscriptionRepository,
                                  WeChatSubscribeConfig config) {
        this.subscriptionRepository = subscriptionRepository;
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.weixin.qq.com")
            .build();
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

    private String getAccessToken() {
        tokenLock.lock();
        try {
            if (cachedAccessToken != null && tokenExpiry != null) {
                if (Instant.now().plus(TOKEN_EXPIRY_BUFFER).isBefore(tokenExpiry)) {
                    log.debug("Returning cached access token");
                    return cachedAccessToken;
                }
                log.info("Access token expired or about to expire, refreshing");
            }

            log.info("Fetching new access token from WeChat API");
            Map<String, Object> response = webClient.get()
                .uri(ACCESS_TOKEN_URL + "?grant_type=client_credential&appid={appid}&secret={secret}",
                     config.getAppId(), config.getAppSecret())
                .retrieve()
                .bodyToMono(Map.class)
                .block(TOKEN_FETCH_TIMEOUT);

            if (response == null) {
                throw new RuntimeException("Failed to fetch access token: null response");
            }

            if (response.containsKey("errcode")) {
                Integer errcode = (Integer) response.get("errcode");
                String errmsg = (String) response.get("errmsg");
                throw new RuntimeException("WeChat API error: errcode=" + errcode + ", errmsg=" + errmsg);
            }

            cachedAccessToken = (String) response.get("access_token");
            Integer expiresIn = (Integer) response.get("expires_in");
            tokenExpiry = Instant.now().plusSeconds(expiresIn);

            log.info("Successfully obtained new access token, expires in {} seconds", expiresIn);
            return cachedAccessToken;

        } finally {
            tokenLock.unlock();
        }
    }

    private void sendTemplateMessage(String openid, String templateId, Map<String, Object> data) {
        String accessToken = getAccessToken();

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("data", data);

        log.info("Sending template message to openid: {}, template: {}", openid, templateId);

        Map<String, Object> response = webClient.post()
            .uri(SUBSCRIBE_MESSAGE_SEND_URL + "?access_token={token}", accessToken)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Map.class)
            .block(Duration.ofSeconds(10));

        if (response == null) {
            log.warn("Template message response is null, openid={}", openid);
            return;
        }

        Integer errcode = (Integer) response.get("errcode");
        String errmsg = (String) response.get("errmsg");

        if (errcode != null && errcode == 0) {
            log.info("Successfully sent template message to openid: {}", openid);
        } else {
            log.warn("Failed to send template message, errcode={}, errmsg={}, openid={}", errcode, errmsg, openid);
        }
    }

    public record TemplateData(String value) {}
}