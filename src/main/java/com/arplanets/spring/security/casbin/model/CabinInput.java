package com.arplanets.spring.security.casbin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CabinInput {

    private String sub;
    private String obj;
    private String act;
    private String dom;
}
