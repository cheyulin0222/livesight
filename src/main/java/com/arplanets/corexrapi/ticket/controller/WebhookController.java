package com.arplanets.corexrapi.ticket.controller;

import com.arplanets.corexrapi.ticket.dto.IncomingDataDto;
import com.arplanets.corexrapi.ticket.dto.PayloadDto;
import com.arplanets.corexrapi.ticket.dto.ValidationResult;
import com.arplanets.corexrapi.ticket.service.JwtService;
import com.arplanets.corexrapi.ticket.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Controller
@Tag(name = "Webhook", description = "Webhook API")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final ValidationService validationService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.service.error-url}")
    private String errorUrl;

    public WebhookController(ValidationService validationService,
                             JwtService jwtService,
                             ObjectMapper objectMapper) {
        this.validationService = validationService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/ticket/webhook")
    @Operation(summary = "Webhook")
    public String handleWebhook(@RequestParam("data") String encodedData) {

        try {
            // -----------------------------------------------------
            // 步驟 1: 解碼 & 解析
            // -----------------------------------------------------
            String base64Data = URLDecoder.decode(encodedData, StandardCharsets.UTF_8);
            byte[] jsonBytes = Base64.getUrlDecoder().decode(base64Data);
            String jsonData = new String(jsonBytes, StandardCharsets.UTF_8);

            IncomingDataDto data = objectMapper.readValue(jsonData, IncomingDataDto.class);
            PayloadDto payload = data.getPayload();

            if (payload == null) {
                logger.warn("無效請求：缺少 'payload' 物件。");
                return buildErrorRedirect("invalid_request");
            }

            logger.info("開始驗證請求：AuthID = {}, TicketID = {}", payload.getAuthTypeId(), payload.getTicketObjid());

            Optional<ValidationResult> resultOpt = validationService.validateAndProcess(payload);

            if (resultOpt.isEmpty()) {
                logger.warn("驗證失敗。");
                return buildErrorRedirect("validation_failed");
            }

            // -----------------------------------------------------
            // 步驟 3: 生成 JWT Token (*** 已修改 ***)
            // -----------------------------------------------------

            // 從驗證結果中取出 finalUrl 和 userId
            ValidationResult result = resultOpt.get();
            String finalUrl = result.getFinalUrl();
            String userId = result.getUserId(); // 這就是 auth_type_id

            // 呼叫更新後的 JwtService，傳入 userId
            String accessToken = jwtService.generateToken(userId);

            // -----------------------------------------------------
            // 步驟 4: 重定向 (*** 已修改 ***)
            // -----------------------------------------------------

            // 使用從 DB 2 查到的 finalUrl
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(finalUrl)
                    .queryParam("access_token", accessToken)
                    .toUriString();

            logger.info("驗證成功。使用者 {} 正在重定向到 {}", userId, finalUrl);

            // 返回 "redirect:" 會觸發 Spring MVC 執行 302 Found 重定向
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            // (捕捉所有錯誤，包括 JSON 解析錯誤、DB 2 找不到配置的嚴重錯誤等)
            logger.error("處理 webhook 時發生未預期的錯誤: " + e.getMessage(), e);
            return buildErrorRedirect("processing_error");
        }
    }

    // (輔助方法，保持不變)
    private String buildErrorRedirect(String errorCode) {
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(errorUrl)
                .queryParam("error", errorCode)
                .toUriString();
        return "redirect:" + redirectUrl;
    }
}