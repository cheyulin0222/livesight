package com.arplanets.corexrapi.livesight.model.validation;

import com.arplanets.corexrapi.livesight.model.dto.req.DateRangeRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
// @Constraint: 核心註解，告訴 Spring 這個註解是一個驗證註解，並指定了它的驗證器是 DateRangeValidator
@Constraint(validatedBy = DateRangeRequest.DateRangeValidator.class)
// @Target({TYPE}): 註解可以應用在類別（Class）、介面（Interface）或 Enum 上
@Target({TYPE})
// @Retention(RUNTIME): 註解會在運行時保留，以便 Spring 容器可以讀取它
@Retention(RUNTIME)
public @interface ValidDateRange {
    // 驗證失敗時的預設錯誤訊息
    String message() default "結束日期必須晚於開始日期";

    // 固定寫法，用於分組驗證
    Class<?>[] groups() default {};

    // 固定寫法，用於傳遞元數據
    Class<? extends Payload>[] payload() default {};
}
