package com.arplanets.LiveSight.authorization.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenVerifyRequest {

    @JsonProperty("access_token")
    @Schema(description = "Access Token")
    @NotBlank(message = "access_token 不可為空")
    private String accessToken;
}
