package com.arplanets.LiveSight.authorization.exception.enums;

public enum LiveSightErrorCode implements BusinessExceptionDisplay{

    _001("Token 缺少 username claim"),
    _002("Member 不存在"),
    _003("LiveSight 不存在");

    private final String message;

    LiveSightErrorCode(String message) {
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
