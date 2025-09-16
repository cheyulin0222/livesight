package com.arplanets.corexrapi.livesight.model.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PageRequest {

    @Min(value = 1, message = "page_size 最小為 1")
    @Max(value = 50, message = "page_size 最大為 50")
    @JsonProperty("page_size")
    @Schema(description = "每頁資料筆數", example = "10")
    private Integer pageSize;

    @Size(max = 255, message = "last_evaluated_key 字數過長")
    @JsonProperty("last_evaluated_key")
    @Schema(description = "最後一筆資料鍵值")
    private String lastEvaluatedKey;
}
