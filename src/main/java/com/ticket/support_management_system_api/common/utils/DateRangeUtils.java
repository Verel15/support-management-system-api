package com.ticket.support_management_system_api.common.utils;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class DateRangeUtils {

    private DateRangeUtils() {}

    public static LocalDateTime resolveStart(EDateRangeFilter dateRange) {
        if (dateRange == null) {
            return null;
        }
        return switch (dateRange) {
            case TODAY -> LocalDate.now().atStartOfDay();
            case THIS_WEEK -> LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay();
            case THIS_MONTH -> LocalDate.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .atStartOfDay();
        };
    }
}
