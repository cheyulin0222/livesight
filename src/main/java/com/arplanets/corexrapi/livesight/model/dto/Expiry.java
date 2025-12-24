package com.arplanets.corexrapi.livesight.model.dto;

import com.arplanets.corexrapi.livesight.model.eunms.ExpireMode;
import com.arplanets.corexrapi.livesight.model.eunms.PeriodUnit;
import com.arplanets.corexrapi.livesight.model.validation.ValidExpiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidExpiry
@Builder
public class Expiry {

    @Schema(description = "效期模式: RELATIVE(相對), PERIOD_ALIGNED(週期對齊), ABSOLUTE(絕對)", example = "RELATIVE")
    @NotNull(message = "expire_mode 不可為空")
    @JsonProperty("expire_mode")
    private ExpireMode expireMode;

    @Schema(description = "效期-相對持續時長 (配合 RELATIVE 模式使用，單位：分鐘)", example = "30")
    @JsonProperty("duration")
    private Long duration;

    @Schema(description = "效期-週期對齊單位 (配合 PERIOD_ALIGNED 模式使用): DAY, WEEK, MONTH, YEAR", example = "DAY")
    @JsonProperty("period_unit")
    private PeriodUnit periodUnit;

    @Schema(description = "效期-固定截止時間 (配合 ABSOLUTE 模式使用)", example = "2025-08-11T00:00:00+08:00")
    @JsonProperty("fixed_at")
    private ZonedDateTime fixedAt;

    public static class ExpiryValidator implements ConstraintValidator<ValidExpiry, Expiry> {

        @Override
        public boolean isValid(Expiry expiry, ConstraintValidatorContext context) {
            if (expiry == null || expiry.getExpireMode() == null) {
                return true;
            }

            boolean isValid = true;
            String message = "";
            String propertyName = "";

            switch (expiry.getExpireMode()) {
                case RELATIVE:
                    if (expiry.getDuration() == null) {
                        isValid = false;
                        message = "模式為 RELATIVE 時，duration 不可為空";
                        propertyName = "duration";
                    }
                    break;
                case PERIOD_ALIGNED:
                    if (expiry.getPeriodUnit() == null) {
                        isValid = false;
                        message = "模式為 PERIOD_ALIGNED 時，period_unit 不可為空";
                        propertyName = "period_unit";
                    }
                    break;
                case ABSOLUTE:
                    if (expiry.getFixedAt() == null) {
                        isValid = false;
                        message = "模式為 ABSOLUTE 時，fixed_at 不可為空";
                        propertyName = "fixed_at";
                    }
                    break;
            }

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(propertyName)
                        .addConstraintViolation();
            }

            return isValid;
        }
    }
}
