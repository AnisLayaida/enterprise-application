package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveUsageReportDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Read-side report of annual leave usage across the organisation or one manager's team.
 * Pure query: no aggregate is modified.
 */
@Service
public class GetLeaveUsageReportService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final StaffDirectory staffDirectory;

    public GetLeaveUsageReportService(LeaveAllowanceRepository leaveAllowanceRepository,
                                      StaffDirectory staffDirectory) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
        this.staffDirectory = staffDirectory;
    }

    public LeaveUsageReportDto getReport(Integer businessYear, UUID managerId) {
        int year = businessYear != null ? businessYear : Year.now().getValue();
        List<UUID> teamStaffIds = managerId == null ? null : staffDirectory.findDirectReportIds(managerId);

        List<LeaveUsageReportDto.StaffUsage> rows = leaveAllowanceRepository.findByBusinessYear(year).stream()
                .filter(a -> teamStaffIds == null || teamStaffIds.contains(a.getStaffId()))
                .map(GetLeaveUsageReportService::toUsage)
                .toList();

        long totalEntitled = rows.stream().mapToLong(LeaveUsageReportDto.StaffUsage::entitledDays).sum();
        long totalUsed = rows.stream().mapToLong(LeaveUsageReportDto.StaffUsage::usedDays).sum();
        long totalRemaining = rows.stream().mapToLong(LeaveUsageReportDto.StaffUsage::remainingDays).sum();

        return new LeaveUsageReportDto(
                year, rows.size(), totalEntitled, totalUsed, totalRemaining,
                utilisation(totalUsed, totalEntitled), rows);
    }

    private static LeaveUsageReportDto.StaffUsage toUsage(LeaveAllowance allowance) {
        return new LeaveUsageReportDto.StaffUsage(
                allowance.getStaffId(),
                allowance.getEntitledDays(),
                allowance.getEntitledDays() - allowance.getRemainingDays(),
                allowance.getRemainingDays());
    }

    /** Percentage of entitlement used, to one decimal place; zero when there is no entitlement. */
    private static double utilisation(long used, long entitled) {
        if (entitled == 0) {
            return 0.0;
        }
        return Math.round(used * 1000.0 / entitled) / 10.0;
    }
}