package com.arplanets.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IncomingDataDto {
    private String service;

    @JsonProperty("client_id")
    private String clientId;

    private PayloadDto payload;
}
