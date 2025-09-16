package com.arplanets.corexrapi.livesight.exception.enums;

public enum OrderErrorCode implements BusinessExceptionDisplay {
    _001("沒有提供 Live Sight ID"),
    _002("Live Sight ID 不存在"),
    _003("Org ID 無權操作此 Live Sight"),
    _004("訂單不存在"),
    _005("Salt 驗證失敗"),
    _006("Product ID 無權操作此訂單"),
    _015("訂單已失效"),
    _016("訂單不存在或 ProductID 不符或已作廢"),
    _017("訂單不存在或 ProductID 不符或已過期或訂單狀態不為 PENDING"),
    _018("訂單不存在或 ProductID 不符或已過期或訂單狀態不為 ACTIVATED 或 redeem_code 驗證失敗"),
    _019("訂單不存在或 ProductID 不符或訂單狀態不為 REDEEMED"),
    _020("訂單驗證失敗");

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
