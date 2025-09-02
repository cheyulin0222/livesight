package com.arplanets.LiveSight.authorization.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderVoidRequest {

    @Schema(description = "產品 ID", example = "corexr")
    @NotBlank(message = "product_id 不可為空")
    @JsonProperty("product_id")
    private String productId;

    @Schema(description = "組織 ID", example = "arplanet")
    @NotBlank(message = "org_id 不可為空")
    @JsonProperty("org_id")
    private String orgId;

    @Schema(description = "namespace", example = "corexr.livesight.500133fb-0c40-4158-9bf7-ca50c198b30a")
    @NotBlank(message = "namespace 不可為空")
    private String namespace;

    @Schema(description = "訂單 ID", example = "order_0052cc4a-8cdf-4d5c-9aeb-b155bdb10369")
    @NotBlank(message = "order_id 不可為空")
    @JsonProperty("order_id")
    private String orderId;
}
