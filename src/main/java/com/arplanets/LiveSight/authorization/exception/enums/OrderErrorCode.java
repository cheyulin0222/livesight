package com.arplanets.LiveSight.authorization.exception.enums;

public enum OrderErrorCode implements BusinessExceptionDisplay {
    _001("沒有提供 Live Sight ID"),
    _002("Live Sight ID 不存在"),
    _003("Org ID 無權操作此 Live Sight"),
    _004("訂單不存在"),
    _005("Salt 驗證失敗"),
    _006("Product ID 無權操作此訂單"),
    _007("訂單狀態不存在"),
    _008("目前的訂單狀態不可開通"),
    _009("訂單沒有逾期時間"),
    _010("訂單已過期"),
    _011("無效的 Redeem Code"),
    _012("目前的訂單狀態不可兌換"),
    _013("訂單已作廢"),
    _014("目前的訂單狀態不可歸還");

    private final String message;

    OrderErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String description() {
        return this.getClass().getSimpleName() + this.name();
    }
}
