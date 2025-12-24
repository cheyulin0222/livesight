package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanUpdateRequest {

    @Schema(description = "方案 ID")
    @JsonProperty("plan_id")
    private String planId;

    @Schema(description = "方案名稱", example = "一般票")
    @Pattern(regexp = ".*\\S.*", message = "plan_name 若有提供則不可為空白")
    @JsonProperty("plan_name")
    private String planName;

    @Schema(description = "效期設定")
    private Expiry expiry;
}
