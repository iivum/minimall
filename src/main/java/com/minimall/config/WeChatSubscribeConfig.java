package com.minimall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat.subscribe")
public class WeChatSubscribeConfig {
    private String appId;
    private String appSecret;
    private String orderCreatedTemplateId;
    private String orderPaidTemplateId;
    private String orderShippedTemplateId;
    private String orderCompletedTemplateId;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getOrderCreatedTemplateId() { return orderCreatedTemplateId; }
    public void setOrderCreatedTemplateId(String id) { this.orderCreatedTemplateId = id; }
    public String getOrderPaidTemplateId() { return orderPaidTemplateId; }
    public void setOrderPaidTemplateId(String id) { this.orderPaidTemplateId = id; }
    public String getOrderShippedTemplateId() { return orderShippedTemplateId; }
    public void setOrderShippedTemplateId(String id) { this.orderShippedTemplateId = id; }
    public String getOrderCompletedTemplateId() { return orderCompletedTemplateId; }
    public void setOrderCompletedTemplateId(String id) { this.orderCompletedTemplateId = id; }
}