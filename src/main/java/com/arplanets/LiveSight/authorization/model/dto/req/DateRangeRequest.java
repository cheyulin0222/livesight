package com.arplanets.LiveSight.authorization.model.dto.req;

import com.arplanets.LiveSight.authorization.model.validation.ValidDateRange;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@ValidDateRange
public class DateRangeRequest {

    @Schema(description = "開始時間（台北時間）", example = "2025-08-11T00:00:00+08:00")
    @NotNull(message = "startDate 必須提供")
    @JsonProperty("start_date")
    private ZonedDateTime startDate;

    @Schema(description = "結束時間（台北時間）", example = "2025-08-12T00:00:00+08:00")
    @NotNull(message = "endDate 必須提供")
    @JsonProperty("end_date")
    private ZonedDateTime endDate;

    public static class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRangeRequest> {
        @Override
        public boolean isValid(DateRangeRequest dateRange, ConstraintValidatorContext context) {
            if (dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
                return true;
            }
            return dateRange.getEndDate().isAfter(dateRange.getStartDate());
        }
    }
}
