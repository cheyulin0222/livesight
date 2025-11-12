package com.arplanets.jwt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略 null 欄位
public class JwkDto {
    private String kty; // Key Type (例如 "RSA")
    private String use; // Key Use (例如 "sig" for signature)
    private String kid; // Key ID
    private String alg; // Algorithm (例如 "RS256")
    private String n;   // RSA Modulus
    private String e;   // RSA Exponent
}
