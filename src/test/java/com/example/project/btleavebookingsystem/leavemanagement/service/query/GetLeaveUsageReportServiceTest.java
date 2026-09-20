package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveUsageReportDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetLeaveUsageReportServiceTest {

    private final UUID staffA = UUID.randomUUID();
    private final UUID staffB = UUID.randomUUID();
    private final UUID managerId = UUID.randomUUID();

    private LeaveAllowanceRepository repository;
    private StaffDirectory staffDirectory;
    private GetLeaveUsageReportService service;

    @BeforeEach
    void setUp() {
        repository = mock(LeaveAllowanceRepository.class);
        staffDirectory = mock(StaffDirectory.class);
        service = new GetLeaveUsageReportService(repository, staffDirectory);
    }

    @Test
    void companyWideReportTotalsEntitlementUsageAndUtilisation() {
        LeaveAllowance a = new LeaveAllowance(staffA, 2026, 25);
        a.deduct(5);
        LeaveAllowance b = new LeaveAllowance(staffB, 2026, 30);
        b.deduct(10);
        when(repository.findByBusinessYear(2026)).thenReturn(List.of(a, b));

        LeaveUsageReportDto report = service.getReport(2026, null);

        assertThat(report.staffCount()).isEqualTo(2);
        assertThat(report.totalEntitledDays()).isEqualTo(55);
        assertThat(report.totalUsedDays()).isEqualTo(15);
        assertThat(report.totalRemainingDays()).isEqualTo(40);
        assertThat(report.utilisationPercent()).isEqualTo(27.3);
        verifyNoInteractions(staffDirectory);
    }

    @Test
    void teamReportIncludesOnlyTheManagersDirectReports() {
        LeaveAllowance a = new LeaveAllowance(staffA, 2026, 25);
        a.deduct(5);
        LeaveAllowance b = new LeaveAllowance(staffB, 2026, 30);
        when(repository.findByBusinessYear(2026)).thenReturn(List.of(a, b));
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of(staffA));

        LeaveUsageReportDto report = service.getReport(2026, managerId);

        assertThat(report.staff()).extracting(LeaveUsageReportDto.StaffUsage::staffId).containsExactly(staffA);
        assertThat(report.totalUsedDays()).isEqualTo(5);
        assertThat(report.utilisationPercent()).isEqualTo(20.0);
    }

    @Test
    void yearWithNoAllowancesDefaultsToTheCurrentYearAndReportsZeroUtilisation() {
        int currentYear = Year.now().getValue();
        when(repository.findByBusinessYear(currentYear)).thenReturn(List.of());

        LeaveUsageReportDto report = service.getReport(null, null);

        assertThat(report.businessYear()).isEqualTo(currentYear);
        assertThat(report.staffCount()).isZero();
        assertThat(report.utilisationPercent()).isEqualTo(0.0);
    }
}