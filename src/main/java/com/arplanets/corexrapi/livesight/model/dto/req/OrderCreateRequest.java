package com.arplanets.corexrapi.livesight.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest extends OrderRequestBase{

    @Schema(description = "授權類別", example = "aws.cognito")
    @NotBlank(message = "auth_type 不可為空")
    @JsonProperty("auth_type")
    private String authType;

    @Schema(description = "授權類別 ID", example = "cognito-user-abc123")
    @NotBlank(message = "auth_type_id 不可為空")
    @JsonProperty("auth_type_id")
    private String authTypeId;

    @Schema(description = "鹽值")
    @NotBlank(message = "salt 不可為空")
    private String salt;
}
