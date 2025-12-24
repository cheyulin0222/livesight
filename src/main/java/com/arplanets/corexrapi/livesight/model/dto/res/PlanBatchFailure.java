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
public class PlanBatchFailure {

    @Schema(description = "方案名稱", example = "一般票")
    @JsonProperty("plan_name")
    private String planName;

    @Schema(description = "服務效期設定")
    private Expiry expiry;

    @Schema(description = "失敗原因")
    private String reason;
}
