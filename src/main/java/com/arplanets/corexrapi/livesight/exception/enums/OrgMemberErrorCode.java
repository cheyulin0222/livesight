package com.arplanets.corexrapi.livesight.exception.enums;

public enum OrgMemberErrorCode implements BusinessExceptionDisplay{
    _001(" uuid 不存在"),
    _002(" 無權操作此 org_id");

    private final String message;

    OrgMemberErrorCode(String message) {
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
