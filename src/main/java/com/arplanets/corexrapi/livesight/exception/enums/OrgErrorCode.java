package com.arplanets.corexrapi.livesight.exception.enums;

public enum OrgErrorCode implements BusinessExceptionDisplay{
    _001("Org 不存在或未啟用");


    private final String message;

    OrgErrorCode(String message) {
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
