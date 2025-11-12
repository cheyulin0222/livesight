package com.arplanets.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.service.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.service.public-key-path}")
    private Resource publicKeyResource;

    @Bean
    public PrivateKey privateKey() throws Exception {
        try (InputStream is = privateKeyResource.getInputStream()) {
            String keyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // 移除 PEM 標頭和尾部,以及所有空白字符
            String privateKeyPEM = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");  // 移除所有空白字符(包括換行)

            // Base64 解碼
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        try (InputStream is = publicKeyResource.getInputStream()) {
            String keyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // 移除 PEM 標頭和尾部,以及所有空白字符
            String publicKeyPEM = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64 解碼
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}