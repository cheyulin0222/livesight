package com.arplanets.corexrapi.livesight.exception.enums;

public enum PlanErrorCode implements BusinessExceptionDisplay{

    _001("一般方案已存在");


    private final String message;

    PlanErrorCode(String message) {
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
