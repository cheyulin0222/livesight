package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderFilterRequest {

    @Schema(description = "namespace")
    private String namespace;

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

    @Schema(description = "訂單建立時間範圍（台北時間）")
    @JsonProperty("created_at")
    private DateRangeRequest createdAt;

    @Schema(description = "訂單開通時間範圍（台北時間）")
    @JsonProperty("activated_at")
    private DateRangeRequest activatedAt;

    @Schema(description = "訂單開通者")
    @JsonProperty("activated_by")
    private String activatedBy;

    @Schema(description = "標籤", example = "[\"pr\"]")
    private List<String> tags;

    @Schema(description = "訂單兌換時間範圍（台北時間）")
    @JsonProperty("redeemed_at")
    private DateRangeRequest redeemedAt;

    @Schema(description = "訂單作廢時間範圍（台北時間）")
    @JsonProperty("voided_at")
    private DateRangeRequest voidedAt;

    @Schema(description = "訂單作廢者")
    @JsonProperty("voided_by")
    private String voidedBy;

    @Schema(description = "訂單歸還時間範圍（台北時間）")
    @JsonProperty("returned_at")
    private DateRangeRequest returnedAt;

    @Schema(description = "訂單歸還者")
    @JsonProperty("returned_by")
    private String returnedBy;

    @Schema(description = "訂單過期時間範圍（台北時間）")
    @JsonProperty("expired_at")
    private DateRangeRequest expiredAt;

    @Schema(description = "訂單更新時間範圍（台北時間）")
    @JsonProperty("updated_at")
    private DateRangeRequest updatedAt;

}
