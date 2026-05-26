package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class WeChatSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeService.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SUBSCRIBE_MESSAGE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/template/subscribe";
    private static final Duration TOKEN_EXPIRY_BUFFER = Duration.ofMinutes(5);

    private final UserSubscriptionRepository subscriptionRepository;
    private final WeChatSubscribeConfig config;
    private final WebClient webClient;

    private String cachedAccessToken;
    private Instant tokenExpiry;
    private final Map<String, Instant> tokenCache = new HashMap<>();

    public WeChatSubscribeService(UserSubscriptionRepository subscriptionRepository,
                                   WeChatSubscribeConfig config) {
        this.subscriptionRepository = subscriptionRepository;
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.weixin.qq.com")
            .build();
    }

    public void sendOrderCreatedMessage(Order order, User user) {
        sendMessageIfSubscribed(user, UserSubscription::isOrderCreatedEnabled,
            () -> buildOrderCreatedData(order), config.getOrderCreatedTemplateId());
    }

    public void sendOrderPaidMessage(Order order, User user) {
        sendMessageIfSubscribed(user, UserSubscription::isOrderPaidEnabled,
            () -> buildOrderPaidData(order), config.getOrderPaidTemplateId());
    }

    public void sendOrderShippedMessage(Order order, User user, String expressNo) {
        sendMessageIfSubscribed(user, UserSubscription::isOrderShippedEnabled,
            () -> buildOrderShippedData(order, expressNo), config.getOrderShippedTemplateId());
    }

    public void sendOrderCompletedMessage(Order order, User user) {
        sendMessageIfSubscribed(user, UserSubscription::isOrderCompletedEnabled,
            () -> buildOrderCompletedData(order), config.getOrderCompletedTemplateId());
    }

    private void sendMessageIfSubscribed(User user,
                                        java.util.function.Predicate<UserSubscription> isEnabled,
                                        java.util.function.Supplier<Map<String, Object>> dataBuilder,
                                        String templateId) {
        UserSubscription sub = subscriptionRepository.findByOpenid(user.getOpenid()).orElse(null);
        if (sub == null || !isEnabled.test(sub)) {
            log.info("User {} has not subscribed to this message type", user.getOpenid());
            return;
        }

        sendTemplateMessageAsync(user.getOpenid(), templateId, dataBuilder.get())
            .exceptionally(ex -> {
                log.error("Failed to send template message to openid={}: {}", user.getOpenid(), ex.getMessage());
                return null;
            });
    }

    private Map<String, Object> buildOrderCreatedData(Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));
        data.put("amount", new TemplateData(order.getTotalAmount().toString() + "元"));
        return data;
    }

    private Map<String, Object> buildOrderPaidData(Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));
        data.put("amount", new TemplateData(order.getTotalAmount().toString() + "元"));
        return data;
    }

    private Map<String, Object> buildOrderShippedData(Order order, String expressNo) {
        Map<String, Object> data = new HashMap<>();
        data.put("express_no", new TemplateData(expressNo));
        data.put("order_no", new TemplateData(order.getOrderNo()));
        return data;
    }

    private Map<String, Object> buildOrderCompletedData(Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("order_no", new TemplateData(order.getOrderNo()));
        return data;
    }

    @CircuitBreaker(name = "WeChatApi", fallbackMethod = "getAccessTokenFallback")
    @Retry(name = "WeChatApi")
    public CompletableFuture<String> getAccessTokenAsync() {
        if (cachedAccessToken != null && tokenExpiry != null) {
            if (Instant.now().plus(TOKEN_EXPIRY_BUFFER).isBefore(tokenExpiry)) {
                log.debug("Returning cached access token");
                return CompletableFuture.completedFuture(cachedAccessToken);
            }
            log.info("Access token expired or about to expire, refreshing");
        }

        log.info("Fetching new access token from WeChat API");

        return webClient.get()
            .uri(ACCESS_TOKEN_URL + "?grant_type=client_credential&appid={appid}&secret={secret}",
                 config.getAppId(), config.getAppSecret())
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
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
                tokenCache.put(cachedAccessToken, tokenExpiry);
                log.info("Successfully obtained new access token, expires in {} seconds", expiresIn);
                return cachedAccessToken;
            })
            .toFuture();
    }

    @SuppressWarnings("unused")
    private CompletableFuture<String> getAccessTokenFallback(Exception ex) {
        log.warn("Circuit breaker fallback triggered for getAccessToken: {}", ex.getMessage());
        String cached = cachedAccessToken;
        if (cached != null) {
            log.info("Returning stale cached access token due to circuit breaker open");
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.failedFuture(ex);
    }

    @CircuitBreaker(name = "WeChatApi", fallbackMethod = "sendTemplateMessageFallback")
    @Retry(name = "WeChatApi")
    @TimeLimiter(name = "WeChatApi")
    public CompletableFuture<Void> sendTemplateMessageAsync(String openid, String templateId, Map<String, Object> data) {
        return getAccessTokenAsync().thenCompose(accessToken -> {
            Map<String, Object> payload = new HashMap<>();
            payload.put("touser", openid);
            payload.put("template_id", templateId);
            payload.put("data", data);

            log.info("Sending template message to openid: {}, template: {}", openid, templateId);

            return webClient.post()
                .uri(SUBSCRIBE_MESSAGE_SEND_URL + "?access_token={token}", accessToken)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response == null) {
                        log.warn("Template message response is null, openid={}", openid);
                        return null;
                    }
                    Integer errcode = (Integer) response.get("errcode");
                    String errmsg = (String) response.get("errmsg");
                    if (errcode != null && errcode == 0) {
                        log.info("Successfully sent template message to openid: {}", openid);
                    } else {
                        log.warn("Failed to send template message, errcode={}, errmsg={}, openid={}", errcode, errmsg, openid);
                    }
                    return null;
                })
                .toFuture()
                .thenApply(r -> null);
        });
    }

    @SuppressWarnings("unused")
    private CompletableFuture<Void> sendTemplateMessageFallback(String openid, String templateId,
                                                                 Map<String, Object> data, Exception ex) {
        log.warn("Circuit breaker fallback triggered for sendTemplateMessage to openid={}: {}",
                 openid, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    public record TemplateData(String value) {}
}