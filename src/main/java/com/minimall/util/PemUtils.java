package com.minimall.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public final class PemUtils {

    private PemUtils() {}

    public static PrivateKey parsePrivateKey(String pemContent) throws Exception {
        String keyPem = pemContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        return KeyFactory.getInstance("RSA").generatePrivate(
            new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }

    public static X509Certificate parseCertificate(String pemContent) throws Exception {
        String certPem = pemContent
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replaceAll("\\s", "");
        byte[] certBytes = Base64.getDecoder().decode(certPem);
        return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(new java.io.ByteArrayInputStream(certBytes));
    }
}