package com.arplanets.jwt.controller;

import com.arplanets.jwt.dto.JwkDto;
import com.arplanets.jwt.dto.JwksResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collections;

@RestController
@Tag(name = "Redirect 驗證", description = "Redirect 驗證 API")
public class JwksController {

    private final JwksResponse jwksResponse;

    public JwksController(PublicKey publicKey, @Value("${jwt.service.key-id}") String keyId) {
        this.jwksResponse = buildJwks(publicKey, keyId);
    }

    @GetMapping(value = "/live-sight/api/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Redirect 驗證")
    public JwksResponse getJwks() {
        return this.jwksResponse;
    }

    private JwksResponse buildJwks(PublicKey publicKey, String keyId) {

        if (!(publicKey instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("PublicKey is not an RSAPublicKey");
        }
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

        // 從 RSAPublicKey 獲取模數 (n) 和指數 (e)
        BigInteger modulus = rsaPublicKey.getModulus();
        BigInteger exponent = rsaPublicKey.getPublicExponent();

        // 轉換為 Base64URL (URL-safe, no padding)
        String n = encodeBase64Url(modulus);
        String e = encodeBase64Url(exponent);

        JwkDto jwk = JwkDto.builder()
                .kty("RSA")       // Key Type
                .use("sig")       // Key Use (signature)
                .alg("RS256")     // Algorithm
                .kid(keyId)       // Key ID
                .n(n)             // Modulus
                .e(e)             // Exponent
                .build();

        return new JwksResponse(Collections.singletonList(jwk));
    }

    /**
     * 將 BigInteger 轉換為 JWK 標準的 Base64URL (unsigned, minimal bytes)
     */
    private String encodeBase64Url(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();

        // 處理 BigInteger 可能產生的前導 0 (sign bit)
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] minimalBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, minimalBytes, 0, minimalBytes.length);
            bytes = minimalBytes;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
