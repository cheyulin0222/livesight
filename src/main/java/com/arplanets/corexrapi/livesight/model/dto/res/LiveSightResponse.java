package com.arplanets.corexrapi.livesight.model.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSightResponse {

    @JsonProperty("service_type_id")
    @Schema(description = "服務類別 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    private String serviceTypeId;
}
