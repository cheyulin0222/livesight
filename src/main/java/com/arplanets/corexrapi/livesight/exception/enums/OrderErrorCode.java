package com.arplanets.corexrapi.livesight.exception.enums;

public enum OrderErrorCode implements BusinessExceptionDisplay {
    _001("沒有提供 Live Sight ID"),
    _002("Live Sight ID 不存在"),
    _003("Org ID 無權操作此 Live Sight"),
    _004("訂單不存在"),
    _005("Salt 驗證失敗"),
    _006("Product ID 無權操作此訂單"),
    _015("訂單已失效"),
    _016("1.訂單不存在 或 2.Product ID 無權操作此訂單 或 3.已作廢 或 4.namespace 無權操作此訂單"),
    _017("1.訂單不存在 或 2.Product ID 無權操作此訂單 或 3.已過期 或 4.訂單狀態不為 PENDING 或 5.namespace 無權操作此訂單"),
    _018("1.訂單不存在 或 2.Product ID 無權操作此訂單 或 3.已過期 或 4.訂單狀態不為 ACTIVATED 或 5.redeem_code 驗證失敗"),
    _019("1.訂單不存在 或 2.Product ID 無權操作此訂單 符 3.或訂單狀態不為 REDEEMED 或 4.namespace 無權操作此訂單"),
    _020("訂單驗證失敗"),
    _021("namespace 無權操作此訂單");

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
