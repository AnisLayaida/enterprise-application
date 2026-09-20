package com.example.project.btleavebookingsystem.leavemanagement.dto;

import java.util.List;
import java.util.UUID;

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