package com.arplanets.corexrapi.livesight.exception;

import com.arplanets.corexrapi.livesight.exception.enums.BusinessExceptionDisplay;
import com.arplanets.corexrapi.livesight.exception.enums.ErrorType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionDeniedException extends RuntimeException {

    private final ErrorType errorType;
    private BusinessExceptionDisplay code;
    private final String errorMessage;

    public PermissionDeniedException(BusinessExceptionDisplay code) {
        super(code.description().concat("  ").concat(code.message()));
        this.code = code;
        this.errorMessage = "";
        this.errorType = ErrorType.AUTHORITY;
    }
}
