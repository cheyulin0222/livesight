package com.arplanets.corexrapi.livesight.model.dto.res;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
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
public class PlanCreateResponse {

    @Schema(description = "方案 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    @JsonProperty("plan_id")
    private String planId;

    @Schema(description = "方案名稱", example = "一般票")
    @JsonProperty("plan_name")
    private String planName;

    @Schema(description = "服務效期設定")
    private Expiry expiry;
}
