package com.arplanets.commons.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DateTimeConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Taipei");

    /**
     * 將 ZonedDateTime 轉換為目標時區的 ISO 8601 字串。
     *
     * @param zonedDateTime 要轉換的 ZonedDateTime 物件。
     * @return 包含時區偏移量的 ISO 8601 字串，如果輸入為 null，則回傳 null。
     */
    public static String toFormattedString(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(TARGET_ZONE).format(FORMATTER);
    }

    /**
     * 將 ISO 8601 字串解析為 ZonedDateTime 物件。
     *
     * @param timeString 包含時間字串的字串。
     * @return 解析後的 Optional<ZonedDateTime>。
     */
    public static ZonedDateTime fromFormattedString(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            log.error("input String is null or empty");
            return null;
        }
        try {
            return ZonedDateTime.parse(timeString, FORMATTER);
        } catch (Exception e) {
            log.error("Cannot covert form input String: {}", timeString);
            return null;
        }
    }

    public static ZonedDateTime fromEpochSecondToZonedDateTime(String epochSecondString) {
        if (epochSecondString == null || epochSecondString.isEmpty()) {
            return null;
        }
        try {
            long epochSeconds = Long.parseLong(epochSecondString);
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), TARGET_ZONE);
        } catch (NumberFormatException e) {
            // 處理數字格式錯誤，例如日誌記錄或拋出自定義異常
            // 這裡簡單地回傳 null，或者您可以根據業務邏輯拋出IllegalArgumentException
            return null;
        }
    }


}
