package com.arplanets.LiveSight.authorization.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class LoggingService {

    private final LogContext logContext;
    private final ObjectMapper objectMapper;

    public void info(String message) {
        executeLogging(() -> log(LogLevel.INFO, message, null, null), message);
    }


    public void info(String message, Map<String, Object> context) {
        executeLogging(() -> log(LogLevel.INFO, message, context, null), message);

    }

    public void warn(String message) {
        executeLogging(() -> log(LogLevel.WARN, message, null, null), message);
    }


    public void error(String message) {
        ErrorContext errorResponse = ErrorContext.builder()
                .errorMessage(message)
                .build();

        executeLogging(() -> log(LogLevel.ERROR, message, null, errorResponse), message);
    }



    public void error(String message, Map<String, Object> context) {
        ErrorContext errorResponse = ErrorContext.builder()
                .errorMessage(message)
                .build();

        executeLogging(() -> log(LogLevel.ERROR, message, context, errorResponse), message);
    }

    public void error(String message, Throwable error) {
        ErrorContext errorResponse = ErrorContext.builder()
                .errorMessage(message)
                .details(Arrays.toString(error.getStackTrace()))
                .build();

        executeLogging(() -> log(LogLevel.ERROR, message, null, errorResponse), message);
    }

    public void error(String message, Throwable error, Map<String, Object> context) {
        ErrorContext errorResponse = ErrorContext.builder()
                .errorMessage(message)
                .details(Arrays.toString(error.getStackTrace()))
                .build();

        executeLogging(() -> log(LogLevel.ERROR, message, context, errorResponse), message);
    }

    public void error(ErrorContext errorContext) {

        executeLogging(() -> log(LogLevel.ERROR, errorContext.getErrorMessage(), null, errorContext), errorContext.getErrorMessage());
    }

    private void log(LogLevel level, String message, Map<String, Object> context, ErrorContext errorContext) {
        try {
            if (context == null) {
                context = new HashMap<>();
            }

            context.put("message", message);

            LogMessage logMessage = logContext.buildApiMessage(
                    level.name(),
                    context,
                    errorContext);

            String jsonLog = objectMapper.writeValueAsString(logMessage);

            switch (level) {
                case INFO -> log.info("{}", jsonLog);
                case ERROR -> log.error("{}", jsonLog);
                default -> log.debug("{}", jsonLog);
            }
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON log");
        }
    }

    private void executeLogging(Runnable loggingAction, String originalMessage) {
        try {
            loggingAction.run();
        } catch (Exception e) {
            log.warn("Failed to process and log message: '{}'", originalMessage, e);
        }
    }
}
