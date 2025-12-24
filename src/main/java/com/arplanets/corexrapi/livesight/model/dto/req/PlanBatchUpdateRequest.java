package com.arplanets.corexrapi.livesight.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanBatchUpdateRequest {

    @JsonProperty("org_id")
    @NotBlank(message = "org_id 不可為空")
    @Schema(description = "組織 ID", example = "arplanet")
    private String orgId;

    @JsonProperty("live_sight_id")
    @NotBlank(message = "live_sight_id 不可為空")
    @Schema(description = "實境導覽 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    private String liveSightId;

    @NotBlank(message = "plans 不可為空")
    @Schema(description = "方案列表")
    private List<PlanUpdateRequest> plans;
}
