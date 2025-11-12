package com.arplanets.jwt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayloadDto {
    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("ticket_objid")
    private long ticketObjid;

    @JsonProperty("auth_type")
    private String authType;

    @JsonProperty("auth_type_id")
    private String authTypeId;
}
