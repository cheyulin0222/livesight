package com.arplanets.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

/**
 * 這是最外層的 DTO，用來映射 S3 JSON 的根物件
 */
@Data
@NoArgsConstructor
// (重要) 忽略 JSON 中有、但我們 Java DTO 中沒有的欄位
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleContentUrlDto {

    /**
     * (*** 修正 ***)
     * 1. 映射 "attr" 欄位
     * 2. 它的類型是我們下面定義的 AttrDto 內部類別
     */
    @JsonProperty("attr")
    private AttrDto attr;

    /**
     * 這是內部的 "attr" DTO，
     * 用來映射 "attr": { ... } 物件的內容
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略 "fullscreen"
    public static class AttrDto {

        /**
         * (*** 修正 ***)
         * 1. 映射 "hyperlink" 欄位
         * 2. 這是我們真正要的 URL
         */
        @JsonProperty("hyperlink")
        private String hyperlink;
    }
}
