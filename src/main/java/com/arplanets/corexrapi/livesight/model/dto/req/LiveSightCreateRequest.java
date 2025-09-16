package com.arplanets.corexrapi.livesight.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSightCreateRequest {

    @Schema(description = "組織 ID", example = "arplanet")
    @NotBlank(message = "org_id 不可為空")
    @JsonProperty("org_id")
    private String orgId;
}
