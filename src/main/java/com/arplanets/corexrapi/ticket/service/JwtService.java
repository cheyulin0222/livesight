package com.arplanets.corexrapi.ticket.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final PrivateKey privateKey;

    @Value("${jwt.service.issuer}")
    private String issuer;

    @Value("${jwt.service.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.service.key-id}")
    private String keyId;

    public JwtService(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        // 您可以在 claims 中放入任何需要的額外資訊
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        // ... (例如 claims.put("aud", "target-system-audience"))

        return Jwts.builder()
                // (重要) 在 Header 中設置 Key ID (kid)
                .setHeaderParam("kid", keyId)
                .setClaims(claims)
                // (重要) 將 userId 設為 Token 的 "Subject"
                .setSubject(userId)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 使用 RS256 和私鑰簽名
                .compact();
    }
}