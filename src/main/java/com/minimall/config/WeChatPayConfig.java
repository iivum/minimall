package com.minimall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechatpay")
public class WeChatPayConfig {
    private String appid;
    private String mchid;
    private String serialNo;
    private String privateKeyPath;
    private String apiV3Key;
    private String callbackUrl;
    private boolean sandbox;
    private String privateKeyContent;

    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }
    public String getMchid() { return mchid; }
    public void setMchid(String mchid) { this.mchid = mchid; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
    public String getApiV3Key() { return apiV3Key; }
    public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
    public boolean isSandbox() { return sandbox; }
    public void setSandbox(boolean sandbox) { this.sandbox = sandbox; }
    public String getPrivateKeyContent() { return privateKeyContent; }
    public void setPrivateKeyContent(String privateKeyContent) { this.privateKeyContent = privateKeyContent; }
}
