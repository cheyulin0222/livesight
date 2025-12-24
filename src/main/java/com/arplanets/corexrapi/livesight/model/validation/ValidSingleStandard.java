package com.arplanets.corexrapi.livesight.model.validation;

import com.arplanets.corexrapi.livesight.model.dto.req.PlanBatchCreateRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PlanBatchCreateRequest.SingleStandardValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidSingleStandard {
    String message() default "只能有一組 plan 為 standard";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
