package com.arplanets.spring.security.casbin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCasbinData {

    private Boolean success;
    private String message;
    private String comment;
    private CasbinData data;

}
