package com.arplanets.LiveSight.authorization.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Slf4j
public class MultiIssuerJwtDecoder implements JwtDecoder {

    private final ConcurrentMap<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

    private final List<String> trustedIssuers;

    @Override
    public Jwt decode(String token) throws JwtException {
        // 快速解析 JWT 以獲取 issuer，不進行完整的簽名驗證
        String issuer = getIssuerFromToken(token);

        if (!isTrustedIssuer(issuer)) {
            throw new JwtException("Untrusted issuer: " + issuer);
        }

        JwtDecoder decoder = decoders.computeIfAbsent(issuer, this::createDecoderForIssuer);

        return decoder.decode(token);

    }

    private String getIssuerFromToken(String token) {
        try {
            // (1) 拆分 JWT 的三個部分
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Invalid JWT format: token must have 3 parts.");
            }

            // (2) Base64 解碼 payload 部分 (第二部分)
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            // (3) 使用 Jackson 函式庫解析 JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decodedPayload);

            // (4) 取得 'iss' 聲明
            String issuer = jsonNode.path("iss").asText();
            if (issuer.isEmpty()) {
                throw new JwtException("JWT has no issuer (iss) claim.");
            }

            return issuer;
        } catch (Exception e) {
            throw new JwtException("Failed to get issuer from token.", e);
        }
    }


    private boolean isTrustedIssuer(String issuer) {
        if (issuer == null) {
            return false;
        }
        for (String trusted : trustedIssuers) {
            if (trusted.equals(issuer)) {
                return true;
            }
        }
        return false;
    }

    private JwtDecoder createDecoderForIssuer(String issuer) {
        try {
            String jwkSetUri;

            // 根據 issuer 判斷 JWK Set 的 URL 格式
            if (issuer.equals("https://test-auth.platform.arplanets.com:9000/pool1")) {
                jwkSetUri = issuer + "/oauth2/jwks";
            } else {
                jwkSetUri = issuer + "/.well-known/jwks.json";
            }

            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } catch (Exception e) {
            throw new JwtException("Failed to create JwtDecoder for issuer: " + issuer, e);
        }
    }
}
