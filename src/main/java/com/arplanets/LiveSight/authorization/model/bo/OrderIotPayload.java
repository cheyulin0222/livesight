package com.arplanets.LiveSight.authorization.model.bo;

import com.arplanets.LiveSight.authorization.model.eunms.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderIotPayload {

    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("order_status")
    private OrderStatus orderStatus;
    @JsonProperty("auth_type_id")
    private String authTypeId;
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime createdAt;
    @JsonProperty("expired_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime expiredAt;
    @JsonProperty("redeem_code")
    private String redeemCode;
}
