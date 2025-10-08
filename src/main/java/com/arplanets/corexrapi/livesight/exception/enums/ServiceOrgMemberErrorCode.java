package com.arplanets.corexrapi.livesight.exception.enums;

public enum ServiceOrgMemberErrorCode implements BusinessExceptionDisplay{

    _001("ServiceOrgMember 不存在");

    private final String message;

    ServiceOrgMemberErrorCode(String message) {
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
