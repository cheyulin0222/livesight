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
public class OrderFetchStatusRequest {

    @Schema(description = "產品 ID", example = "corexr")
    @NotBlank(message = "product_id 不可為空")
    @JsonProperty("product_id")
    private String productId;

    @Schema(description = "訂單 ID", example = "order_0052cc4a-8cdf-4d5c-9aeb-b155bdb10369")
    @NotBlank(message = "order_id 不可為空")
    @JsonProperty("order_id")
    private String orderId;

    @Schema(description = "鹽值")
    @NotBlank(message = "salt 不可為空")
    private String salt;
}
