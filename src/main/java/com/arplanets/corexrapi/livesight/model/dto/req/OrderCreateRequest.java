package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Schema(description = "方案 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    @Pattern(regexp = ".*\\S.*", message = "plan_name 若有提供則不可為空白")
    @JsonProperty("plan_id")
    private String planId;


}
