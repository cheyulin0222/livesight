package com.arplanets.corexrapi.livesight.model.dto.req;

import com.arplanets.corexrapi.livesight.model.validation.NamespaceStartsWithProductId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@NamespaceStartsWithProductId
public abstract class OrderRequestBase {

    @Schema(description = "產品 ID", example = "corexr")
    @NotBlank(message = "product_id 不可為空")
    @JsonProperty("product_id")
    private String productId;

    @Schema(description = "namespace", example = "corexr.livesight.500133fb-0c40-4158-9bf7-ca50c198b30a")
    @NotBlank(message = "namespace 不可為空")
    private String namespace;
}
