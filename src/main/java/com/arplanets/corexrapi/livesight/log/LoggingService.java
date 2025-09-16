package com.arplanets.corexrapi.livesight.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.arplanets.corexrapi.livesight.log.LogContext.ISO_8601_FORMATTER;


@Slf4j
@RequiredArgsConstructor
@Component
public class LoggingService {

    private final LogContext logContext;
    private final ObjectMapper objectMapper;

    public void info(String message) {
        Map<String, Object> context = new HashMap<>();
        context.put("message", message);
        log(LogLevel.INFO, context, null);
    }

    public void info(Map<String, Object> context) {
        log(LogLevel.INFO, context, null);

    }

    public void warn(String message) {
        Map<String, Object> context = new HashMap<>();
        context.put("message", message);
        log(LogLevel.WARN, context, null);
    }


    public void error(String message) {
        Map<String, Object> context = new HashMap<>();
        context.put("message", message);
        log(LogLevel.ERROR, context, null);

    }

    public void error(Map<String, Object> context) {
        log(LogLevel.ERROR, context, null);
    }

    public void error(String message, Throwable error) {
        Map<String, Object> context = new HashMap<>();
        context.put("message", message);
        ErrorContext errorResponse = ErrorContext.builder()
                .errorMessage(error.getMessage())
                .details(Arrays.toString(error.getStackTrace()))
                .build();

        log(LogLevel.ERROR, context, errorResponse);
    }

    public void error(ErrorContext errorContext) {
        log(LogLevel.ERROR, null, errorContext);
    }

    private void log(LogLevel level, Map<String, Object> context, ErrorContext errorContext) {
        LogMessage logMessage = logContext.buildApiMessage(
                level.name(),
                context,
                errorContext);

        doLog(level, logMessage);


    }

    public LogMessage initApiMessage(String targetVal) {
        return logContext.initApiMessage(targetVal);
    }

    public void infoByInitAPiMessage(LogMessage logMessage, Map<String, Object> context) {
        Instant now = Instant.now();

        logMessage.setLogLevel(LogLevel.INFO.name());
        logMessage.setContext(context);
        logMessage.setEventTimestamp(now.getEpochSecond());
        logMessage.setTime8601(ZonedDateTime.ofInstant(now, ZoneOffset.ofHours(8)).format(ISO_8601_FORMATTER));

        doLog(LogLevel.INFO, logMessage);
    }

    public void errorByInitAPiMessage(LogMessage logMessage, String message, Exception e) {
        Instant now = Instant.now();

        logMessage.setLogLevel(LogLevel.ERROR.name());
        logMessage.setErrorMessage(message);
        logMessage.setDetails(Arrays.toString(e.getStackTrace()));
        logMessage.setEventTimestamp(now.getEpochSecond());
        logMessage.setTime8601(ZonedDateTime.ofInstant(now, ZoneOffset.ofHours(8)).format(ISO_8601_FORMATTER));

        doLog(LogLevel.ERROR, logMessage);

    }

    private void doLog(LogLevel level, LogMessage logMessage) {
        try {
            String jsonLog = objectMapper.writeValueAsString(logMessage);
            switch (level) {
                case ERROR -> log.error("{}", jsonLog);
                case DEBUG -> log.debug("{}", jsonLog);
                case WARN -> log.warn("{}", jsonLog);
                case TRACE -> log.trace("{}", jsonLog);
                default -> log.info("{}", jsonLog);
            }
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON log");
        }
    }
}
