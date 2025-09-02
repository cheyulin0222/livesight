package com.arplanets.LiveSight.authorization.model;

import com.arplanets.LiveSight.authorization.model.eunms.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
        "order_id",
        "namespace",
        "product_id",
        "service_type",
        "service_type_id",
        "auth_type",
        "auth_type_id",
        "user_browser",
        "user_os",
        "user_device_type",
        "order_status",
        "created_at",
        "verification_code",
        "activated_at",
        "activated_by",
        "redeem_code",
        "redeemed_at",
        "access_token",
        "voided_at",
        "voided_by",
        "returned_at",
        "returned_by",
        "expired_at",
        "updated_at",
        "ttl"
})
public class OrderContext {
    @JsonProperty("order_id")
    private String orderId;

    private String namespace;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @JsonProperty("auth_type")
    private String authType;
    @JsonProperty("auth_type_id")
    private String authTypeId;
    @JsonProperty("user_browser")
    private String userBrowser;
    @JsonProperty("user_os")
    private String userOs;
    @JsonProperty("user_device_type")
    private String userDeviceType;

    @JsonProperty("order_status")
    private OrderStatus orderStatus;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonProperty("verification_code")
    private String verificationCode;

    @JsonProperty("activated_at")
    private ZonedDateTime activatedAt;
    @JsonProperty("activated_by")
    private String activatedBy;
    @JsonProperty("redeem_code")
    private String redeemCode;

    @JsonProperty("redeemed_at")
    private ZonedDateTime redeemedAt;
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("voided_at")
    private ZonedDateTime voidedAt;
    @JsonProperty("voided_by")
    private String voidedBy;

    @JsonProperty("returned_at")
    private ZonedDateTime returnedAt;
    @JsonProperty("returned_by")
    private String returnedBy;

    @JsonProperty("expired_at")
    private ZonedDateTime expiredAt;

    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;

    private ZonedDateTime ttl;
}
