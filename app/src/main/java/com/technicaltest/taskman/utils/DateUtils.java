package com.technicaltest.taskman.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    public static Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return Instant.MIN;
        }
        try {
            // standard ISO 8601 parsing (e.g. 2024-05-20T14:30:00.000Z)
            return Instant.parse(dateStr.trim());
        } catch (Exception e) {
            try {
                // handles if timezone/Z is missing (e.g. 2024-05-20T14:30:00)
                return LocalDateTime.parse(dateStr.trim()).toInstant(ZoneOffset.UTC);
            } catch (Exception e2) {
                try {
                    // handles simple date (2024-05-20)
                    return LocalDate.parse(dateStr.trim()).atStartOfDay().toInstant(ZoneOffset.UTC);
                } catch (Exception e3) {
                    return Instant.MIN;
                }
            }
        }
    }
}
