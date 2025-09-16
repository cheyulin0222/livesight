package com.arplanets.corexrapi.livesight.model.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyResponse {

    @JsonProperty("is_valid")
    @Schema(description = "驗證結果")
    private boolean isValid;

    @Schema(description = "驗證結果訊息")
    private String message;

}
