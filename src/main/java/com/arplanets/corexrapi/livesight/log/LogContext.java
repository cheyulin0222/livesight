package com.arplanets.corexrapi.livesight.log;

import com.arplanets.corexrapi.livesight.model.RequestContext;
import com.arplanets.corexrapi.livesight.model.ResponseContext;
import com.arplanets.commons.utils.ClientInfoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class LogContext {

    public static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final String LOG_SERVICE = "live_sight";
    public static final String AUDIT_LOG_GROUP = "audit.order";
    public static final String API_ACTION_LOG_GROUP = "api.action";

    public static final Integer LOG_VER = 1;

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ObjectMapper objectMapper;

    public String getLogSn() {
        return generateId();
    }

    public String getRequestId() {
        if (request != null && request.getAttribute("requestContext") != null) {
            RequestContext requestContext = (RequestContext) request.getAttribute("requestContext");
            return requestContext.getRequestId();
        }
        return null;
    }

    public String getActionType() {
        if (request != null && StringUtils.hasText(request.getRequestURI())) {
            if ("/.well-known/jwks.json".equals(request.getRequestURI())) {
                return "jwks.fetch";
            }

            String[] parts = request.getRequestURI().split("/");

            if (parts.length >= 3) {
                String resource = parts[parts.length - 2];
                String action = parts[parts.length - 1];

                if (!resource.isEmpty() && !action.isEmpty()) {
                    return resource + "." + action;
                }
            }

            return "unclassified";
        }
        return null;
    }

    public String getUserAgent() {
        if (request != null) {
            return request.getHeader("User-Agent");
        }
        return null;
    }

    public String getSourceIp() {
        if (request != null) {
            return ClientInfoUtil.getClientIp(request);
        }
        return null;
    }

    public ResponseContext getResponseContext() {
        ResponseContext responseContext = null;

        if (request != null) {
            Object respContextObj = request.getAttribute("responseContext");
            if (respContextObj != null) {
                if (respContextObj instanceof ResponseContext) {
                    responseContext = (ResponseContext) respContextObj;
                }
            }
        }

        return responseContext;
    }

    public LogMessage buildApiMessage(
            String logLevel,
            Map<String, Object> context,
            ErrorContext errorResponse) {
        Instant now = Instant.now();

        return LogMessage.builder()
                .logService(LOG_SERVICE)
                .logGroup(API_ACTION_LOG_GROUP)
                .logLevel(logLevel)
                .logSn(getLogSn())
                .logVer(LOG_VER)
                .sessionId(null)
                .requestId(getRequestId())
                .userIdp(null)
                .userUid(null)
                .userRole(null)
                .source("aws.lambda")
                .sourceIp(getSourceIp())
                .userAgent(getUserAgent())
                .sourceId(null)
                .actionType(getActionType())
                .actionResult(null)
                .targetType(null)
                .targetVal(null)
                .context(context)
                .errorCode(errorResponse != null && errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : 0)
                .errorMessage(errorResponse != null ? errorResponse.getErrorMessage() : null)
                .details(errorResponse != null ? errorResponse.getDetails() : null)
                .eventTimestamp(now.getEpochSecond())
                .time8601(ZonedDateTime.ofInstant(now, ZoneOffset.ofHours(8)).format(ISO_8601_FORMATTER))
                .build();
    }


    public LogMessage buildAuditMessage() {
        Instant now = Instant.now();

        return LogMessage.builder()
                .logService(LOG_SERVICE)
                .logGroup(AUDIT_LOG_GROUP)
                .logLevel(getAuditLogLevel(getStatus()))
                .logSn(getLogSn())
                .logVer(LOG_VER)
                .sessionId(null)
                .requestId(getRequestId())
                .userIdp(null)
                .userUid(null)
                .userRole(null)
                .source("aws.lambda")
                .sourceIp(getSourceIp())
                .userAgent(getUserAgent())
                .sourceId(null)
                .actionType(getActionType())
                .actionResult(getAuditActionResult(getStatus()))
                .targetType("order")
                .targetVal(getAuditOrderId(getResponseContext()))
                .context(getAuditContext(getResponseContext()))
                .errorCode(getStatus())
                .errorMessage(getAuditErrorMessage(getResponseContext()))
                .details(getAuditDetails(getResponseContext()))
                .eventTimestamp(now.getEpochSecond())
                .time8601(ZonedDateTime.ofInstant(now, ZoneOffset.ofHours(8)).format(ISO_8601_FORMATTER))
                .build();
    }

    public LogMessage initApiMessage(String targetVal) {

        return LogMessage.builder()
                .logService(LOG_SERVICE)
                .logGroup(AUDIT_LOG_GROUP)
                .logLevel(null)
                .logSn(getLogSn())
                .logVer(LOG_VER)
                .sessionId(null)
                .requestId(getRequestId())
                .userIdp(null)
                .userUid(null)
                .userRole(null)
                .source("aws.lambda")
                .sourceIp(getSourceIp())
                .userAgent(getUserAgent())
                .sourceId(null)
                .actionType(getActionType())
                .actionResult(null)
                .targetType("order")
                .targetVal(targetVal)
                .context(null)
                .errorCode(0)
                .errorMessage("")
                .details("")
                .eventTimestamp(null)
                .time8601(null)
                .build();
    }

    private int getStatus() {
        if (response != null) {
            return response.getStatus();
        }
        return 0;
    }

    private String getAuditLogLevel(int httpStatus) {
        if (httpStatus >= 400) {
            return "ERROR";
        }
        return "INFO";
    }

    private String getAuditActionResult(int status) {
        if (status >= 200 && status < 300) {
            return "success";
        } else if (status >= 400 && status < 600) {
            return "failure";
        }
        return "unknown";
    }

    private String getAuditOrderId(ResponseContext context) {
        if (context != null && context.getOrder() != null) {
            return context.getOrder().getOrderId();
        }
        return null;
    }

    private Map<String, Object> getAuditContext(ResponseContext responseContext) {
        LinkedHashMap<String, Object> context = new LinkedHashMap<>();

        try {
            if (responseContext != null) {
                context = objectMapper.convertValue(
                        responseContext.getOrder(),
                        new TypeReference<>() {
                        }
                );
            }
        } catch (IllegalArgumentException e) {
            log.error("Error converting OrderContext to Map for audit log: {}", e.getMessage());
            context.put("conversion_error", e.getMessage());
        } catch (Exception e) { // 捕獲其他潛在的例外
            log.error("An unexpected error occurred during OrderContext to Map conversion: {}", e.getMessage());
            context.put("conversion_error", "Unexpected error: " + e.getMessage());
        }

        return context;
    }

    private String getAuditErrorMessage(ResponseContext responseContext) {
        if (responseContext != null && responseContext.getErrorContext() != null) {
            return responseContext.getErrorContext().getErrorMessage();
        }
        return "";
    }

    private String getAuditDetails(ResponseContext responseContext) {
        if (responseContext != null && responseContext.getErrorContext() != null) {
            return responseContext.getErrorContext().getDetails();
        }
        return "";
    }

    private String generateId() {
        ZoneId taipeiZone = ZoneId.of("Asia/Taipei");
        String timestamp = LocalDateTime.now(taipeiZone)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return "%s-%s-%s".formatted("log", timestamp, UUID.randomUUID().toString());
    }

}
