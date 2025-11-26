package com.arplanets.corexrapi.livesight.security.handler;

import com.arplanets.corexrapi.base.advice.ExceptionHandleAdvice;
import com.arplanets.corexrapi.livesight.log.ErrorContext;
import com.arplanets.corexrapi.livesight.log.Logger;
import com.arplanets.corexrapi.livesight.model.dto.res.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.arplanets.corexrapi.livesight.exception.enums.ErrorType.AUTHORITY;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorContext errorContext = ExceptionHandleAdvice.buildErrorContext(AUTHORITY, FORBIDDEN, "Access Denied", accessDeniedException);

        ExceptionHandleAdvice.setResponseContext(request, errorContext);

        Logger.error(errorContext);

        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(errorContext.getErrorMessage()));




    }
}
