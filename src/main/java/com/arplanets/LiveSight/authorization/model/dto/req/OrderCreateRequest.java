package com.arplanets.LiveSight.authorization.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {

    @Schema(description = "產品 ID", example = "corexr")
    @NotBlank(message = "product_id 不可為空")
    @JsonProperty("product_id")
    private String productId;

    @Schema(description = "namespace", example = "corexr.livesight.500133fb-0c40-4158-9bf7-ca50c198b30a")
    @NotBlank(message = "namespace 不可為空")
    private String namespace;

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
