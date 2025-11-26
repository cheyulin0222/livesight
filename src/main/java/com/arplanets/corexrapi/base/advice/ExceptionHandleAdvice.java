package com.arplanets.corexrapi.base.advice;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.PermissionDeniedException;
import com.arplanets.corexrapi.livesight.exception.enums.ErrorType;
import com.arplanets.corexrapi.livesight.log.ErrorContext;
import com.arplanets.corexrapi.livesight.log.Logger;
import com.arplanets.corexrapi.livesight.model.ResponseContext;
import com.arplanets.corexrapi.livesight.model.dto.res.ErrorResponse;
import com.arplanets.commons.utils.ClassUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import static com.arplanets.corexrapi.livesight.exception.enums.ErrorType.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class ExceptionHandleAdvice {

    /**
     * 處理 Controller Json 驗證失敗拋出的異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(HttpServletRequest request, MethodArgumentNotValidException ex) {

        String errMsg = extractedBindingResult(ex.getBindingResult());

        ErrorContext errorContext = buildErrorContext(REQUEST, BAD_REQUEST, errMsg, ex);

        setResponseContext(request, errorContext);

        Logger.error(errorContext);

        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), BAD_REQUEST);
    }

    /**
     * 處理 Controller URL 參數驗證失敗拋出的異常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(HttpServletRequest request, ConstraintViolationException ex) {
        String errMsg = extractedConstraintViolations(ex.getConstraintViolations());
        ErrorContext errorContext = buildErrorContext(REQUEST, BAD_REQUEST, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);

        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), BAD_REQUEST);
    }

    /**
     * 處理 Spring 無法正確解析 HTTP 請求體（通常是 JSON）時拋出的異常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>  handleValidationError(HttpServletRequest request, HttpMessageNotReadableException ex) {
        String errMsg = ExceptionUtils.getRootCauseMessage(ex);
        ErrorContext errorContext = buildErrorContext(REQUEST, BAD_REQUEST, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);

        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), BAD_REQUEST);
    }

    /**
     * 處理 Service 層級的異常
     */
    @ExceptionHandler(OrderApiException.class)
    public ResponseEntity<ErrorResponse> handleOrderApiException(HttpServletRequest request, OrderApiException ex){
        String errMsg = ex.getCode().message();

        ErrorContext errorContext = buildErrorContext(BUSINESS, INTERNAL_SERVER_ERROR, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);
        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), INTERNAL_SERVER_ERROR);

    }

    /**
     * 處理 Service 層級的異常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDeniedException(HttpServletRequest request, PermissionDeniedException ex){
        String errMsg = ex.getCode().message();

        ErrorContext errorContext = buildErrorContext(AUTHORITY, FORBIDDEN, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);
        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), FORBIDDEN);

    }

    /**
     * 處理 Database 層級的異常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(HttpServletRequest request, DataAccessException ex){
        String errMsg = ExceptionUtils.getRootCauseMessage(ex);
        ErrorContext errorContext = buildErrorContext(DATABASE, INTERNAL_SERVER_ERROR, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);
        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), INTERNAL_SERVER_ERROR);

    }

    /**
     * 處理其他異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(HttpServletRequest request, Exception ex){

        String errMsg = ExceptionUtils.getRootCauseMessage(ex);
        ErrorContext errorContext = buildErrorContext(SYSTEM, INTERNAL_SERVER_ERROR, errMsg, ex);
        setResponseContext(request, errorContext);

        Logger.error(errorContext);

        return new ResponseEntity<>(new ErrorResponse(errorContext.getErrorMessage()), INTERNAL_SERVER_ERROR);

    }

    private String extractedBindingResult(BindingResult bindingResult) {

        StringBuilder errorMessagesData = new StringBuilder();
        HashMap<String , Field> Fields = ClassUtil.getAllField(Objects.requireNonNull(bindingResult.getTarget()));

        for (ObjectError error : bindingResult.getAllErrors()) {
            if(!errorMessagesData.isEmpty()) {
                errorMessagesData.append(" ,");
            }

            if (error instanceof FieldError fieldError) {
                String Field = fieldError.getField();
                Field classFile = Fields.get(Field);
                if( classFile != null ) {
                    Schema schema = classFile.getAnnotation(Schema.class);
                    if( schema != null ) {
                        Field = schema.description();

                    }
                }
                errorMessagesData.append(Field.concat(":"));
            }
            errorMessagesData.append(error.getDefaultMessage());
        }

        return errorMessagesData.toString();
    }

    private String extractedConstraintViolations(Set<ConstraintViolation<?>> violations) {

        int index = 0;
        int size = violations.size();
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> constraintViolation : violations) {
            var filename = constraintViolation.getPropertyPath();
            sb.append(" ").append(filename).append(" : ").append(constraintViolation.getMessage());
            if (index != size - 1) {
                sb.append(";");
            }
            index++;
        }

        return sb.toString();
    }


    public static void setResponseContext(HttpServletRequest request, ErrorContext errorContext) {
        ResponseContext responseContext = (ResponseContext) request.getAttribute("responseContext");

        if (responseContext == null) {
            responseContext = new ResponseContext();
            request.setAttribute("responseContext", responseContext);
        }
        responseContext.setErrorContext(errorContext);
    }

    public static ErrorContext buildErrorContext(ErrorType errorType, HttpStatus status, String data, Exception e) {
        String message = "[" + errorType.name() + "]" + " " + data;
        return ErrorContext.builder()
                .errorCode(status.value())
                .errorMessage(message)
                .details(Arrays.toString(e.getStackTrace()))
                .build();

    }
}
