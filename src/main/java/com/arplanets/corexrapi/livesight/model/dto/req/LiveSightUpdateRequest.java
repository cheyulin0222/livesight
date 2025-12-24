package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import com.arplanets.corexrapi.livesight.model.validation.ValidLiveSightUpdateRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidLiveSightUpdateRequest
public class LiveSightUpdateRequest {

    @Schema(description = "組織 ID", example = "arplanet")
    @NotBlank(message = "org_id 不可為空")
    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("service_type_id")
    @NotBlank(message = "service_type_id 不可為空")
    @Schema(description = "服務類別 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    private String serviceTypeId;

    @Schema(description = "開通效期設定")
    @JsonProperty("activation_expiry")
    private Expiry activationExpiry;

    @Schema(description = "服務效期設定")
    @JsonProperty("service_expiry")
    private Expiry serviceExpiry;

    public static class LiveSightUpdateRequestValidator implements ConstraintValidator<ValidLiveSightUpdateRequest, LiveSightUpdateRequest> {

        @Override
        public boolean isValid(LiveSightUpdateRequest request, ConstraintValidatorContext context) {
            return request.getActivationExpiry() != null || request.getServiceExpiry() != null;
        }
    }
}
