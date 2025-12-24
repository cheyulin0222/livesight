package com.arplanets.corexrapi.livesight.model.validation;

import com.arplanets.corexrapi.livesight.model.dto.req.LiveSightUpdateRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = LiveSightUpdateRequest.LiveSightUpdateRequestValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidLiveSightUpdateRequest {
    String message() default "請求中未包含任何需更新的欄位";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
