package com.arplanets.corexrapi.livesight.model.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanBatchUpdateFailedResponse {

    @Schema(description = "方案 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    @JsonProperty("plan_id")
    private String planId;

    @Schema(description = "失敗原因")
    private String reason;
}
