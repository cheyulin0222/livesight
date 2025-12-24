package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.validation.ValidSingleStandard;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidSingleStandard
public class PlanBatchCreateRequest {

    @JsonProperty("org_id")
    @NotBlank(message = "org_id 不可為空")
    @Schema(description = "組織 ID", example = "arplanet")
    private String orgId;

    @JsonProperty("live_sight_id")
    @NotBlank(message = "live_sight_id 不可為空")
    @Schema(description = "實境導覽 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    private String liveSightId;

    @NotEmpty(message = "plans 列表不可為空")
    @Schema(description = "方案列表")
    private List<PlanCreateRequest> plans;

    public static class SingleStandardValidator implements ConstraintValidator<ValidSingleStandard, PlanBatchCreateRequest> {
        @Override
        public boolean isValid(PlanBatchCreateRequest request, ConstraintValidatorContext context) {
            if (request == null || request.getPlans() == null) {
                return true;
            }

            return request.getPlans().stream()
                    .filter(PlanCreateRequest::getStandard)
                    .count() <= 1;
        }
    }
}
