package com.arplanets.LiveSight.authorization.model.dto.res;

import com.arplanets.LiveSight.authorization.model.eunms.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfoResponse {

    @Schema(description = "訂單 ID", example = "order_0052cc4a-8cdf-4d5c-9aeb-b155bdb10369")
    @JsonProperty("order_id")
    private String orderId;

    @Schema(description = "namespace")
    private String namespace;
    @Schema(description = "產品 ID")
    @JsonProperty("product_id")
    private String productId;
    @Schema(description = "服務類別")
    @JsonProperty("service_type")
    private String serviceType;
    @Schema(description = "服務 ID")
    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @Schema(description = "授權類別")
    @JsonProperty("auth_type")
    private String authType;
    @Schema(description = "使用者 ID", example = "cognito-user-abc123")
    @JsonProperty("auth_type_id")
    private String authTypeId;
    @Schema(description = "使用者瀏覽器")
    @JsonProperty("user_browser")
    private String userBrowser;
    @Schema(description = "使用者作業系統")
    @JsonProperty("user_os")
    private String userOs;
    @Schema(description = "使用者裝置")
    @JsonProperty("user_device_type")
    private String userDeviceType;

    @Schema(description = "訂單狀態", example = "ACTIVATED")
    @JsonProperty("order_status")
    private OrderStatus orderStatus;

    @Schema(description = "訂單建立時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @Schema(description = "訂單開通時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("activated_at")
    private ZonedDateTime activatedAt;
    @Schema(description = "訂單開通者")
    @JsonProperty("activated_by")
    private String activatedBy;

    @Schema(description = "訂單兌換時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("redeemed_at")
    private ZonedDateTime redeemedAt;

    @Schema(description = "訂單作廢時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("voided_at")
    private ZonedDateTime voidedAt;
    @Schema(description = "訂單作廢者")
    @JsonProperty("voided_by")
    private String voidedBy;

    @Schema(description = "訂單歸還時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("returned_at")
    private ZonedDateTime returnedAt;
    @Schema(description = "訂單歸還者")
    @JsonProperty("returned_by")
    private String returnedBy;

    @Schema(description = "訂單過期時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("expired_at")
    private ZonedDateTime expiredAt;

    @Schema(description = "訂單更新時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
}
