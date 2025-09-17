package com.arplanets.corexrapi.livesight.log;

import ch.qos.logback.core.PropertyDefinerBase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TaipeiTimestampPropertyDefiner extends PropertyDefinerBase {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    @Override
    public String getPropertyValue() {
        return LocalDateTime.now(TAIPEI_ZONE).format(FORMATTER);
    }
}
