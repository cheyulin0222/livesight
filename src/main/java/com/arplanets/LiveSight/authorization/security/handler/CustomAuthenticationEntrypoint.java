package com.arplanets.LiveSight.authorization.security.handler;

import com.arplanets.LiveSight.authorization.advice.ExceptionHandleAdvice;
import com.arplanets.LiveSight.authorization.log.ErrorContext;
import com.arplanets.LiveSight.authorization.log.Logger;
import com.arplanets.LiveSight.authorization.model.dto.res.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;


import java.io.IOException;

import static com.arplanets.LiveSight.authorization.exception.enums.ErrorType.AUTHORITY;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorContext errorContext = ExceptionHandleAdvice.buildErrorContext(AUTHORITY, UNAUTHORIZED, AUTHORITY.getLabel(), authException);

        ExceptionHandleAdvice.setResponseContext(request, errorContext);

        Logger.error(errorContext);

        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(errorContext.getErrorMessage()));

    }
}
