package com.example.project.btleavebookingsystem.leavemanagement.dto;

import java.util.List;
import java.util.UUID;

/**
 * System-wide (or team-level) annual leave usage for one business year.
 * "Used" days are approved annual leave charged against the allowance, whether or not
 * the leave dates have yet passed.
 */
public record LeaveUsageReportDto(
        int businessYear,
        int staffCount,
        long totalEntitledDays,
        long totalUsedDays,
        long totalRemainingDays,
        double utilisationPercent,
        List<StaffUsage> staff
) {
    public record StaffUsage(UUID staffId, int entitledDays, int usedDays, int remainingDays) {
    }
}