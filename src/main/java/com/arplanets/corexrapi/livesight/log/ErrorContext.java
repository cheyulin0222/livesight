package com.arplanets.corexrapi.livesight.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorContext {

    private Integer errorCode;

    private String errorMessage;

    private String details;
}
