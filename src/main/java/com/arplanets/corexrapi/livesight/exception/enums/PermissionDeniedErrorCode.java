package com.arplanets.corexrapi.livesight.exception.enums;

public enum PermissionDeniedErrorCode implements BusinessExceptionDisplay{
    _001("組織驗證失敗"),
    _002("無法辨識使用者"),
    _003("使用者非組織成員"),
    _004("未提供 Live Sight ID"),
    _005("組織無權操作此 Live Sight ID"),
    _006("訂單不存在"),
    _007("Live Sight 無權操作此訂單"),
    _008("Live Sight 不存在"),
    _009("Product ID 無緣操作此訂單"),
    _010("Product ID 與 namespace 不符");

    private final String message;

    PermissionDeniedErrorCode(String message) {
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
