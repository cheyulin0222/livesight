package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.security.jwt.OrderJwtManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "JWT 公鑰 API", description = "提供 JWT 驗證所需的公開金鑰（JWK Set）")
@RequiredArgsConstructor
public class JwkSetController {

    private final OrderJwtManager orderJwtManager;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "獲取公開金鑰集（JWK Set）")
    public String jwkSet() {
        return orderJwtManager.getJwkSetJson();
    }
}
