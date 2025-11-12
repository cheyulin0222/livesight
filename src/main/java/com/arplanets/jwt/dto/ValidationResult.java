package com.arplanets.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Lombok: 產生一個包含所有參數的建構子
public class ValidationResult {
    private String finalUrl;
    private String userId;
}