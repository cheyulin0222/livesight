package com.arplanets.corexrapi.livesight.model.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanBatchUpdateResponse {

    @JsonProperty("live_sight_id")
    @Schema(description = "實境導覽 ID", example = "18fdc1e2-0d9b-456c-b15a-e5a4d8b4a120")
    private String liveSightId;

    @JsonProperty("success_items")
    @Schema(description = "新增成功方案列表")
    private List<PlanUpdateResponse> successItems;

    @JsonProperty("failed_items")
    @Schema(description = "新增成功方案列表")
    private List<PlanBatchUpdateFailedResponse> failedItems;

    @JsonProperty("total_count")
    @Schema(description = "總筆數")
    private int totalCount;

    @JsonProperty("success_count")
    @Schema(description = "成功筆數")
    private int successCount;

    @JsonProperty("failed_count")
    @Schema(description = "失敗筆數")
    private int failedCount;

    @JsonProperty("all_success")
    @Schema(description = "全部成功")
    private boolean allSuccess;
}
