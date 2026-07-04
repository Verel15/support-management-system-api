package com.ticket.support_management_system_api.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class RemainingTimeUtils {

    private RemainingTimeUtils() {}

    public static String resolve(LocalDateTime dueDate) {
        return resolve(dueDate, LocalDateTime.now());
    }

    public static String resolve(LocalDateTime dueDate, LocalDateTime now) {
        if (dueDate == null) {
            return null;
        }
        Duration duration = Duration.between(now, dueDate);
        boolean overdue = duration.isNegative();
        Duration magnitude = duration.abs();

        String formatted = format(magnitude);
        return overdue ? "เกินกำหนด " + formatted : "เหลือ " + formatted;
    }

    public static String resolveClosed(LocalDateTime dueDate, LocalDateTime closedAt) {
        if (dueDate == null || closedAt == null) {
            return "เสร็จสิ้น";
        }
        return closedAt.isAfter(dueDate) ? "เสร็จสิ้น (เกินกำหนด)" : "เสร็จสิ้น";
    }

    private static String format(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " นาที";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " ชั่วโมง";
        }
        long days = duration.toDays();
        return days + " วัน";
    }
}
