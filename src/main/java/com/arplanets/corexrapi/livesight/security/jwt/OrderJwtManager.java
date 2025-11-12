package com.arplanets.corexrapi.livesight.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static com.arplanets.corexrapi.livesight.service.impl.DynamoDbOrderServiceImpl.ZONE_ID;

@Component
@Slf4j
@Data
public class OrderJwtManager {

    private KeyPair keyPair;

    @Value("${order.access-token.private-key.path}")
    private Resource privateKeyResource;
    @Value("${order.access-token.issuer}")
    private String jwtIssuer;
    @Value("${order.access-token.expiration-minutes}")
    private long jwtExpirationMinutes;
    @Value("${order.access-token.audience:}")
    private String jwtAudience;

    @PostConstruct
    private void initKeys() {
        try {
            log.info("正在從 Classpath 尋找私鑰...");
            if (privateKeyResource.exists()) {
                byte[] keyBytes;
                try (InputStream inputStream = privateKeyResource.getInputStream()) {
                    keyBytes = inputStream.readAllBytes();
                }

                keyPair = loadKeyPairFromBytes(keyBytes);
                log.info("成功從 Classpath 資源載入金鑰對。");
            } else {
                log.error("【致命錯誤】: 應用程式無法在 Classpath 中找到必要的私鑰檔案 '{}'。", privateKeyResource.getDescription());
                throw new RuntimeException("金鑰設定錯誤，應用程式無法啟動。請先執行 KeyGeneratorUtility 產生金鑰。");
            }
        } catch (Exception e) {
            throw new RuntimeException("金鑰管理員初始化失敗。", e);
        }
    }

    @Bean
    public JWTVerifier jwtVerifier() {
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
        Algorithm algorithm = Algorithm.RSA256(publicKey, null);

        Verification verification = JWT.require(algorithm)
                .withIssuer(this.jwtIssuer);

        if (this.jwtAudience != null && !this.jwtAudience.isEmpty()) {
            verification.withAudience(this.jwtAudience);
        }

        return verification.build();
    }

    public DecodedJWT verify(String accessToken) {
        return jwtVerifier().verify(accessToken);
    }

    private KeyPair loadKeyPairFromBytes(byte[] keyBytes) throws Exception  {
        log.info("loadKeyPairFromBytes");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        if (privateKey instanceof RSAPrivateCrtKey rsaPrivateCrtKey) {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    rsaPrivateCrtKey.getModulus(),
                    rsaPrivateCrtKey.getPublicExponent()
            );
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return new KeyPair(publicKey, privateKey);
        } else {
            throw new IllegalStateException("無法從提供的私鑰推導公鑰。");
        }
    }

    public String getJwkSetJson() {
        PublicKey publicKey = keyPair.getPublic();

        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        return new JWKSet(jwk).toString();
    }

    public String genAccessToken(String orderId, String productId, List<String> tags, ZonedDateTime now, ZonedDateTime expire) {
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
                .withIssuer(jwtIssuer)
                .withSubject(orderId)
                .withClaim("product_id", productId)
                .withClaim("tags", tags)
                .withIssuedAt(now.toInstant())
                .withExpiresAt(expire.toInstant())
                .sign(algorithm);
    }


}
