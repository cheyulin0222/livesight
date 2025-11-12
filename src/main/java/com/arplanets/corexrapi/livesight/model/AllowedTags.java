package com.arplanets.corexrapi.livesight.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedTagsValidator.class)
public @interface AllowedTags {

    String message() default "One or more tags are invalid. Only 'pr' is allowed.";

    // 必填屬性：組別
    Class<?>[] groups() default {};

    // 必填屬性：負載 (Payload)
    Class<? extends Payload>[] payload() default {};
}
