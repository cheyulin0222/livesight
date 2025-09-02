package com.arplanets.spring.security.casbin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CasbinData {

    @JsonProperty("casbin_csv_p")
    private List<List<String>> casbinCsvP;
    @JsonProperty("casbin_csv_g")
    private List<List<String>> casbinCsvG;
    private Map<String, Object> profile;
}
