package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.model.dto.req.TokenVerifyRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.TokenVerifyResponse;
import com.arplanets.corexrapi.livesight.service.OrderService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "驗證", description = "驗證 API")
public class ValidationController {

    private final OrderService orderService;


    @PostMapping(value = "/token_verify", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "驗證 Access Token")
    public ResponseEntity<TokenVerifyResponse> validatedToken(@RequestBody @Valid TokenVerifyRequest request) {
        try {
            orderService.verifyToken(request.getAccessToken());

            return ResponseEntity.ok(new TokenVerifyResponse(true, "Token verified successfully."));

        } catch (JWTVerificationException e) {
            String errorMessage;
            if (e instanceof com.auth0.jwt.exceptions.TokenExpiredException) {
                errorMessage = "Token expired.";
            } else if (e instanceof com.auth0.jwt.exceptions.SignatureVerificationException) {
                errorMessage = "Invalid token signature.";
            } else {
                errorMessage = "Invalid token.";
            }
            return ResponseEntity.ok(new TokenVerifyResponse(false, errorMessage));
        } catch (OrderApiException e) {
            return ResponseEntity.ok(new TokenVerifyResponse(false, e.getCode().message()));
        }
    }

}
