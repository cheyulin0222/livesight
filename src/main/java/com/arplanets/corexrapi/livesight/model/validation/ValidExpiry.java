package com.arplanets.corexrapi.livesight.model.validation;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = Expiry.ExpiryValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface ValidExpiry {
    String message() default "效期欄位設定不完整";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
