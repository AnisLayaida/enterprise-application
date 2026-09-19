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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOutstandingRequestsServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private StaffDirectory staffDirectory;

    private GetOutstandingRequestsService service;

    private final UUID staffIdA = UUID.randomUUID();
    private final UUID staffIdB = UUID.randomUUID();
    private final UUID managerId = UUID.randomUUID();
    private final DateRange range = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));

    @BeforeEach
    void setUp() {
        service = new GetOutstandingRequestsService(leaveRequestRepository, staffDirectory);
    }

    @Test
    void returnsAllOutstandingRequestsCompanyWideWhenNoFiltersGiven() {
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestFor(staffIdA), requestFor(staffIdB)));

        List<LeaveRequestResponseDto> result = service.getOutstanding(null, null);

        assertThat(result).hasSize(2);
        verifyNoInteractions(staffDirectory);
    }

    @Test
    void filtersToASingleStaffMemberWhenStaffIdProvided() {
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestFor(staffIdA), requestFor(staffIdB)));

        List<LeaveRequestResponseDto> result = service.getOutstanding(staffIdA, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).staffId()).isEqualTo(staffIdA);
    }

    @Test
    void filtersToAManagersDirectReportsWhenManagerIdProvided() {
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of(staffIdA));
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestFor(staffIdA), requestFor(staffIdB)));

        List<LeaveRequestResponseDto> result = service.getOutstanding(null, managerId);

        assertThat(result).extracting(LeaveRequestResponseDto::staffId).containsExactly(staffIdA);
    }

    @Test
    void combinedFiltersReturnNothingWhenTheStaffMemberIsNotInThatManagersTeam() {
        when(staffDirectory.findDirectReportIds(managerId)).thenReturn(List.of(staffIdA));
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestFor(staffIdA), requestFor(staffIdB)));

        List<LeaveRequestResponseDto> result = service.getOutstanding(staffIdB, managerId);

        assertThat(result).isEmpty();
    }

    private LeaveRequest requestFor(UUID staffId) {
        return LeaveRequest.submit(staffId, range, LeaveType.ANNUAL, "Holiday", false);
    }
}