package com.arplanets.corexrapi.livesight.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "log_service",
        "log_group",
        "log_level",
        "log_sn",
        "log_ver",
        "session_id",
        "request_id",
        "user_idp",
        "user_uid",
        "user_role",
        "source",
        "source_ip",
        "user_agent",
        "source_id",
        "action_type",
        "action_result",
        "target_type",
        "target_val",
        "context",
        "error_code",
        "error_message",
        "details",
        "event_timestamp",
        "time_8601"
})
public class LogMessage {

    @JsonProperty("log_service")
    private String logService;

    @JsonProperty("log_group")
    private String logGroup;

    @JsonProperty("log_level")
    private String logLevel;

    @JsonProperty("log_sn")
    private String logSn;

    @JsonProperty("log_ver")
    private Integer logVer;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_idp")
    private String userIdp;

    @JsonProperty("user_uid")
    private String userUid;

    @JsonProperty("user_role")
    private String userRole;

    private String source;

    @JsonProperty("source_ip")
    private String sourceIp;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("action_type")
    private String actionType;

    @JsonProperty("action_result")
    private String actionResult;

    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("target_val")
    private String targetVal;

    private Map<String, Object> context;

    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    private String details;

    @JsonProperty("event_timestamp")
    private Long eventTimestamp;

    @JsonProperty("time_8601")
    private String time8601;

}
