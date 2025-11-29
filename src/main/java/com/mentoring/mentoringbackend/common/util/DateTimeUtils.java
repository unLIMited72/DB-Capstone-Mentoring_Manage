package com.mentoring.mentoringbackend.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtils() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(DEFAULT_ZONE).format(DEFAULT_FORMATTER);
    }
}
