package com.arplanets.LiveSight.authorization.model.eunms;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderStatus {

    PENDING,    // 待處理
    ACTIVATED,  // 已開通
    REDEEMED,   // 已兌換
    VOIDED,     // 已作廢
    COMPLETED;     // 已歸還


    public static OrderStatus toOrderStatus(String statusString) {
        try {
            return OrderStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid OrderStatus string: {}", statusString);
            return null;
        }
    }
}
