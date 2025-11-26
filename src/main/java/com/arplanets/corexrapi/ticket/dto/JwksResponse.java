package com.arplanets.corexrapi.ticket.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwksResponse {
    private List<JwkDto> keys;

    public JwksResponse(List<JwkDto> keys) {
        this.keys = keys;
    }
}
