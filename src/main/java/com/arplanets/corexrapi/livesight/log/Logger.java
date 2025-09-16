package com.arplanets.corexrapi.livesight.log;


public class Logger {

    private static LoggingService loggingService;

    static void initializeLoggingService(LoggingService service) {
        loggingService = service;
    }

    public static void info(String message) {
        loggingService.info(message);
    }

    public static void warn(String message) {
        loggingService.warn(message);
    }

    public static void error(String message) {
        loggingService.error(message);
    }

    public static void error(String message, Throwable error) {
        loggingService.error(message, error);
    }

    public static void error(ErrorContext errorContext) {
        loggingService.error(errorContext);
    }
}
