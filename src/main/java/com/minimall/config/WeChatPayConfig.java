package com.minimall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.util.PemUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
    private String platformCertificateContent;

    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }

    @Bean
    public RSAAutoCertificateConfig rsaAutoCertificateConfig() throws IOException {
        ClassPathResource resource = new ClassPathResource(privateKeyPath.replace("classpath:", "cert/"));
        String privateKeyContent;
        try (InputStream is = resource.getInputStream()) {
            privateKeyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        RSAAutoCertificateConfig config = new RSAAutoCertificateConfig.Builder()
            .mchId(mchid)
            .privateKey(privateKeyContent)
            .merchantSerialNumber(serialNo)
            .apiV3Key(apiV3Key)
            .build();
        return config;
    }

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
    public String getPlatformCertificateContent() { return platformCertificateContent; }
    public void setPlatformCertificateContent(String platformCertificateContent) { this.platformCertificateContent = platformCertificateContent; }
}