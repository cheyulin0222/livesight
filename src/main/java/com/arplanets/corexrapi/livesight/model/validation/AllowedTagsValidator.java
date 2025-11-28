package com.arplanets.corexrapi.livesight.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Set;

public class AllowedTagsValidator implements ConstraintValidator<AllowedTags, List<String>> {

    private static final Set<String> ALLOWED_TAGS = Set.of("pr");

    @Override
    public boolean isValid(List<String> tags, ConstraintValidatorContext context) {

        if (tags == null || tags.isEmpty()) {
            return true;
        }

        for (String tag : tags) {
            if (!ALLOWED_TAGS.contains(tag)) {
                return false;
            }
        }

        return true;
    }
}
