package com.arplanets.corexrapi.livesight.model.dto;

import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private String orderId;

    private String namespace;
    private String productId;
    private String serviceType;
    private String serviceTypeId;
    private String planId;

    private String authType;
    private String authTypeId;
    private String userBrowser;
    private String userOs;
    private String userDeviceType;

    private OrderStatus orderStatus;

    private ZonedDateTime createdAt;

    private ZonedDateTime activatedAt;
    private String activatedBy;
    private String redeemCode;
    private List<String> tags;

    private ZonedDateTime redeemedAt;
    private String accessToken;

    private ZonedDateTime voidedAt;
    private String voidedBy;

    private ZonedDateTime returnedAt;
    private String returnedBy;

    private ZonedDateTime expiredAt;

    private ZonedDateTime updatedAt;

}
