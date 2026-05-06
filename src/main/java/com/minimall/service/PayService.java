package com.minimall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.config.WeChatPayConfig;
import com.minimall.model.Order;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

@Service
public class PayService {
    private static final Logger log = LoggerFactory.getLogger(PayService.class);
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private final WeChatPayConfig weChatPayConfig;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final JsapiServiceExtension jsapiServiceExtension;

    public PayService(WeChatPayConfig weChatPayConfig, OrderService orderService,
                     RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        this.weChatPayConfig = weChatPayConfig;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
        this.jsapiServiceExtension = new JsapiServiceExtension.Builder()
            .config(rsaAutoCertificateConfig)
            .build();
    }

    /**
     * Creates a real WeChat Pay unified order via JSAPI SDK.
     *
     * @param order  the order to be paid
     * @param openid the user's WeChat openid
     * @return PrepayWithRequestPaymentResponse containing all JSAPI payment parameters
     */
    public PrepayWithRequestPaymentResponse createUnifiedOrder(Order order, String openid) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(weChatPayConfig.getAppid());
        request.setMchid(weChatPayConfig.getMchid());
        request.setDescription("Order payment: " + order.getOrderNo());
        request.setOutTradeNo(order.getOrderNo());
        request.setNotifyUrl(weChatPayConfig.getCallbackUrl());

        // Set amount - total must be in cents (Integer), currency is CNY
        Amount amount = new Amount();
        amount.setTotal(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
        amount.setCurrency("CNY");
        request.setAmount(amount);

        // Set payer info for JSAPI
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        log.info("Creating WeChat Pay unified order for orderNo: {}, amount: {} CNY",
            order.getOrderNo(), order.getTotalAmount());

        PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(request);

        log.info("WeChat Pay unified order created successfully, prepay_id: {}",
            response.getPackageVal());
        return response;
    }

    /**
     * Extracts JSAPI payment parameters from SDK response.
     *
     * @param response the SDK response containing payment parameters
     * @return map with timeStamp, nonceStr, package, signType, paySign
     */
    public Map<String, String> getJsApiSign(PrepayWithRequestPaymentResponse response) {
        return Map.of(
            "timeStamp", response.getTimeStamp(),
            "nonceStr", response.getNonceStr(),
            "package", response.getPackageVal(),
            "signType", response.getSignType(),
            "paySign", response.getPaySign()
        );
    }

    /**
     * Legacy signing method kept for compatibility.
     * In the new flow, the SDK handles signing automatically.
     */
    public String getJsApiSign(String prepayId, long timestamp, String nonceStr) {
        // Legacy method - not used with new SDK flow
        String message = weChatPayConfig.getMchid() + "\n" + timestamp + "\n" + nonceStr + "\n" + prepayId + "\n";
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(getPrivateKey());
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            log.error("Failed to sign JSAPI request: {}", e.getMessage());
            throw new RuntimeException("Failed to sign JSAPI request", e);
        }
    }

    public boolean verifyCallback(String body, String signature, String serialNo) {
        try {
            if (serialNo == null || !serialNo.equals(weChatPayConfig.getSerialNo())) {
                log.warn("Callback serial number mismatch: expected={}, actual={}",
                    weChatPayConfig.getSerialNo(), serialNo);
                return false;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));

            Signature verifier = Signature.getInstance(SIGN_ALGORITHM);
            verifier.initVerify(getWeChatPayCertificate());
            verifier.update(hash);
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("Callback verification failed: {}", e.getMessage());
            return false;
        }
    }

    private java.security.PrivateKey getPrivateKey() throws Exception {
        String privateKeyContent = weChatPayConfig.getPrivateKeyContent();
        if (privateKeyContent == null || privateKeyContent.isEmpty()) {
            throw new IllegalStateException("WeChat Pay private key not configured");
        }
        String keyPem = privateKeyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        return java.security.KeyFactory.getInstance("RSA").generatePrivate(
            new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }

    private java.security.cert.X509Certificate getWeChatPayCertificate() throws Exception {
        String certContent = weChatPayConfig.getPlatformCertificateContent();
        if (certContent == null || certContent.isEmpty()) {
            throw new IllegalStateException("WeChat Pay platform certificate not configured");
        }
        String certPem = certContent
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replaceAll("\\s", "");
        byte[] certBytes = Base64.getDecoder().decode(certPem);
        return (java.security.cert.X509Certificate) java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(certBytes));
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
