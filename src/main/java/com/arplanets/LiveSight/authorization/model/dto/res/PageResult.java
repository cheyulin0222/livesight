package com.arplanets.LiveSight.authorization.model.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    @Schema(description = "資料列表")
    private List<T> items;

    @Schema(description = "最後一筆資料鍵值")
    @JsonProperty("last_evaluated_key")
    private String lastEvaluatedKey;

    @Schema(description = "是否有下一頁")
    @JsonProperty("has_next_page")
    private boolean hasNextPage;

    public <R> PageResult<R> mapItems(Function<? super T, ? extends R> mapper) {
        List<R> newItems = this.items.stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageResult<>(newItems, this.lastEvaluatedKey, this.hasNextPage);
    }

}
