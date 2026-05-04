package com.minimall.service;

import com.minimall.config.WeChatSubscribeConfig;
import com.minimall.model.Order;
import com.minimall.model.User;
import com.minimall.model.UserSubscription;
import com.minimall.repository.UserSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WeChatSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeService.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SUBSCRIBE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    private static final Duration TOKEN_EXPIRY_BUFFER = Duration.ofMinutes(5);

    private final UserSubscriptionRepository subscriptionRepository;
    private final WeChatSubscribeConfig config;
    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final ReentrantLock refreshLock = new ReentrantLock();

    public WeChatSubscribeService(UserSubscriptionRepository subscriptionRepository,
                                  WeChatSubscribeConfig config) {
        this.subscriptionRepository = subscriptionRepository;
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String getAccessToken() {
        String appId = config.getAppId();
        TokenInfo tokenInfo = tokenCache.get(appId);

        if (tokenInfo != null && !tokenInfo.isExpired()) {
            return tokenInfo.token;
        }

        refreshLock.lock();
        try {
            tokenInfo = tokenCache.get(appId);
            if (tokenInfo != null && !tokenInfo.isExpired()) {
                return tokenInfo.token;
            }
            return refreshAccessToken(appId);
        } finally {
            refreshLock.unlock();
        }
    }

    private String refreshAccessToken(String appId) {
        String url = ACCESS_TOKEN_URL + "?grant_type=client_credential" +
                     "&appid=" + appId +
                     "&secret=" + config.getAppSecret();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to get access token, HTTP status: {}", response.statusCode());
                throw new RuntimeException("Failed to get access token: HTTP " + response.statusCode());
            }

            Map<String, Object> result = parseJson(response.body());

            if (result.containsKey("errcode") && (Integer) result.get("errcode") != 0) {
                log.error("WeChat API error: {}", result.get("errmsg"));
                throw new RuntimeException("WeChat API error: " + result.get("errmsg"));
            }

            String token = (String) result.get("access_token");
            Integer expiresIn = (Integer) result.get("expires_in");

            if (token == null || expiresIn == null) {
                throw new RuntimeException("Invalid access token response");
            }

            Instant expiry = Instant.now().plus(Duration.ofSeconds(expiresIn)).minus(TOKEN_EXPIRY_BUFFER);
            tokenCache.put(appId, new TokenInfo(token, expiry));

            log.info("Access token refreshed successfully, expires in {} seconds", expiresIn);
            return token;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Access token request interrupted", e);
        } catch (Exception e) {
            log.error("Failed to refresh access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    private void sendTemplateMessage(String openid, String templateId, Map<String, Object> data) {
        String token = getAccessToken();
        String url = SUBSCRIBE_MESSAGE_URL + "?access_token=" + token;

        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("data", convertTemplateData(data));

        try {
            String jsonBody = toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to send template message, HTTP status: {}", response.statusCode());
                return;
            }

            Map<String, Object> result = parseJson(response.body());
            Integer errcode = (Integer) result.get("errcode");

            if (errcode == null || errcode == 0) {
                log.info("Template message sent successfully to openid: {}", openid);
            } else {
                log.error("WeChat subscribe message API error: {} - {}", errcode, result.get("errmsg"));
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Send template message interrupted", e);
        } catch (Exception e) {
            log.error("Failed to send template message", e);
        }
    }

    private Map<String, Object> convertTemplateData(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof TemplateData td) {
                result.put(entry.getKey(), Map.of("value", td.value()));
            }
        }
        return result;
    }

    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        int i = 0;
        json = json.trim();
        if (!json.startsWith("{")) {
            throw new RuntimeException("Invalid JSON: " + json);
        }
        i++;
        while (i < json.length() && json.charAt(i) != '}') {
            skipWhitespace(json, i);
            if (json.charAt(i) != '"') {
                throw new RuntimeException("Expected key at position " + i);
            }
            i++;
            int keyStart = i;
            while (i < json.length() && json.charAt(i) != '"') {
                i++;
            }
            String key = json.substring(keyStart, i);
            i++;
            skipWhitespace(json, i);
            if (i >= json.length() || json.charAt(i) != ':') {
                throw new RuntimeException("Expected ':' at position " + i);
            }
            i++;
            skipWhitespace(json, i);
            Object value = parseValue(json, i);
            result.put(key, value);
            skipWhitespace(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++;
            }
        }
        return result;
    }

    private Object parseValue(String json, int start) {
        skipWhitespace(json, start);
        char c = json.charAt(start);

        if (c == '"') {
            int end = start + 1;
            while (end < json.length() && json.charAt(end) != '"') {
                if (json.charAt(end) == '\\') end++;
                end++;
            }
            String value = json.substring(start + 1, end);
            return value.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
        } else if (c == '{') {
            int braceCount = 1;
            int startPos = start;
            int i = start + 1;
            while (i < json.length() && braceCount > 0) {
                if (json.charAt(i) == '{') braceCount++;
                else if (json.charAt(i) == '}') braceCount--;
                i++;
            }
            String inner = json.substring(startPos + 1, i - 1);
            return parseJson(inner);
        } else if (c == '[') {
            int bracketCount = 1;
            int startPos = start;
            int i = start + 1;
            while (i < json.length() && bracketCount > 0) {
                if (json.charAt(i) == '[') bracketCount++;
                else if (json.charAt(i) == ']') bracketCount--;
                i++;
            }
            String inner = json.substring(startPos + 1, i - 1);
            return parseJsonArray(inner);
        } else if (c == 't' || c == 'f') {
            if (json.substring(start).startsWith("true")) return true;
            if (json.substring(start).startsWith("false")) return false;
            throw new RuntimeException("Invalid boolean at position " + start);
        } else if (c == 'n') {
            if (json.substring(start).startsWith("null")) return null;
            throw new RuntimeException("Invalid null at position " + start);
        } else {
            int end = start;
            while (end < json.length() && "0123456789-.eE+".indexOf(json.charAt(end)) >= 0) {
                end++;
            }
            String num = json.substring(start, end);
            if (num.indexOf('.') >= 0 || num.indexOf('e') >= 0 || num.indexOf('E') >= 0) {
                return Double.parseDouble(num);
            }
            return Integer.parseInt(num);
        }
    }

    private java.util.List<Object> parseJsonArray(String json) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        int i = 0;
        while (i < json.length()) {
            skipWhitespace(json, i);
            if (json.charAt(i) == ']') break;
            Object value = parseValue(json, i);
            list.add(value);
            skipWhitespace(json, i);
            if (i < json.length() && json.charAt(i) == ',') i++;
        }
        return list;
    }

    private void skipWhitespace(String json, int pos) {
        while (pos < json.length() && " \t\n\r".indexOf(json.charAt(pos)) >= 0) {
            pos++;
        }
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            appendValue(sb, entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private void appendValue(StringBuilder sb, Object value) {
        if (value instanceof Map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
                appendValue(sb, entry.getValue());
            }
            sb.append("}");
        } else if (value instanceof String) {
            sb.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escapeJson(value.toString())).append("\"");
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
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

    public record TemplateData(String value) {}

    private record TokenInfo(String token, Instant expiry) {
        boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
}