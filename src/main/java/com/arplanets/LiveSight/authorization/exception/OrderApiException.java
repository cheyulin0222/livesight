package com.arplanets.LiveSight.authorization.exception;

import com.arplanets.LiveSight.authorization.exception.enums.BusinessExceptionDisplay;
import com.arplanets.LiveSight.authorization.exception.enums.ErrorType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderApiException extends RuntimeException {

    private final ErrorType errorType;
    private BusinessExceptionDisplay code;
    private final String errorMessage;

    public OrderApiException(BusinessExceptionDisplay code) {
        super(code.description().concat("  ").concat(code.message()));
        this.code = code;
        this.errorMessage = "";
        this.errorType = ErrorType.BUSINESS;
    }
}
