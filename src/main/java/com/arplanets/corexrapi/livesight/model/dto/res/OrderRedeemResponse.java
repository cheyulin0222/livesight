package com.arplanets.corexrapi.livesight.model.dto.res;

import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRedeemResponse{

    @Schema(description = "訂單 ID", example = "order_0052cc4a-8cdf-4d5c-9aeb-b155bdb10369")
    @JsonProperty("order_id")
    private String orderId;
    @Schema(description = "訂單狀態", example = "REDEEMED")
    @JsonProperty("order_status")
    private OrderStatus orderStatus;
    @Schema(description = "使用者 ID", example = "cognito-user-abc123")
    @JsonProperty("auth_type_id")
    private String authTypeId;
    @Schema(description = "訂單建立時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @Schema(description = "訂單過期時間")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @JsonProperty("expired_at")
    private ZonedDateTime expiredAt;
    @Schema(description = "ACCESS TOKEN")
    @JsonProperty("access_token")
    private String accessToken;
    @Schema(description = "標籤", example = "[\"pr\"]")
    private List<String> tags;

}
