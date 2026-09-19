package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTeamPendingRequestsServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private StaffDirectory staffDirectory;

    private GetTeamPendingRequestsService service;

    private final UUID managerId = UUID.randomUUID();
    private final UUID teamMemberId = UUID.randomUUID();
    private final UUID nonTeamMemberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetTeamPendingRequestsService(leaveRequestRepository, staffDirectory);
    }

    @Test
    void onlyReturnsRequestsFromStaffReportingToThisManager() {
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of(teamMemberId));

        DateRange range = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));
        LeaveRequest teamRequest = LeaveRequest.submit(teamMemberId, range, LeaveType.ANNUAL, "Holiday", false);
        LeaveRequest otherRequest = LeaveRequest.submit(nonTeamMemberId, range, LeaveType.ANNUAL, "Holiday", false);

        when(leaveRequestRepository.findByStatusIn(any()))
                .thenReturn(List.of(teamRequest, otherRequest));

        List<LeaveRequestResponseDto> result = service.getForManager(managerId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).staffId()).isEqualTo(teamMemberId);
    }

    @Test
    void filtersOutRequestsStartingBeforeTheGivenFromDate() {
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of(teamMemberId));

        DateRange earlyRange = new DateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5));
        LeaveRequest earlyRequest = LeaveRequest.submit(teamMemberId, earlyRange, LeaveType.ANNUAL, "Early trip", false);

        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(earlyRequest));

        List<LeaveRequestResponseDto> result = service.getForManager(managerId, LocalDate.of(2026, 10, 1), null);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenManagerHasNoTeamMembers() {
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of());
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of());

        List<LeaveRequestResponseDto> result = service.getForManager(managerId, null, null);

        assertThat(result).isEmpty();
    }
}