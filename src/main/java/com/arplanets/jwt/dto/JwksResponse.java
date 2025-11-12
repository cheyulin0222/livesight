package com.arplanets.jwt.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwksResponse {
    private List<JwkDto> keys;

    public JwksResponse(List<JwkDto> keys) {
        this.keys = keys;
    }
}
