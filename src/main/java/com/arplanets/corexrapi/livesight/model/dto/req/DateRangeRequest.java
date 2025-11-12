package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.validation.ValidDateRange;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@ValidDateRange
public class DateRangeRequest {

    @Schema(description = "開始時間（台北時間）", example = "2025-08-11T00:00:00+08:00")
    @JsonProperty("start_date")
    private ZonedDateTime startDate;

    @Schema(description = "結束時間（台北時間）", example = "2025-08-12T00:00:00+08:00")
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
