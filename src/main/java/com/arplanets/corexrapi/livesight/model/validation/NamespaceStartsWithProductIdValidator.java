package com.arplanets.corexrapi.livesight.model.validation;

import com.arplanets.corexrapi.livesight.model.dto.req.OrderRequestBase;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NamespaceStartsWithProductIdValidator implements ConstraintValidator<NamespaceStartsWithProductId, OrderRequestBase> {

    @Override
    public boolean isValid(OrderRequestBase request, ConstraintValidatorContext context) {

        if (request == null || request.getProductId() == null || request.getNamespace() == null || request.getProductId().isBlank() || request.getNamespace().isBlank()) {
            return true;
        }

        String[] split = request.getNamespace().split("\\.");

        boolean isValid = request.getProductId().equals(split[0]);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("namespace")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
