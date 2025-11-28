package com.arplanets.corexrapi.livesight.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NamespaceStartsWithProductIdValidator.class)
public @interface NamespaceStartsWithProductId {

    String message() default "namespace 必須以 product_id 開頭";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
