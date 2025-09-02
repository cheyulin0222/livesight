package com.arplanets.LiveSight.authorization.exception.enums;

import lombok.Getter;

@Getter
public enum ErrorType {

    REQUEST("請求錯誤"),
    DATABASE("資料存取錯誤"),
    AUTHORITY("驗證失敗"),
    BUSINESS("商業邏輯檢核錯誤"),
    SYSTEM("系統錯誤");

    private final String label;

    ErrorType(String label) {
        this.label = label;
    }

}
