package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
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
class GetOutstandingRequestsServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    private GetOutstandingRequestsService service;

    private final UUID staffIdA = UUID.randomUUID();
    private final UUID staffIdB = UUID.randomUUID();
    private final DateRange range = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));

    @BeforeEach
    void setUp() {
        service = new GetOutstandingRequestsService(leaveRequestRepository);
    }

    @Test
    void returnsAllOutstandingRequestsWhenNoStaffIdFilterGiven() {
        LeaveRequest requestA = LeaveRequest.submit(staffIdA, range, LeaveType.ANNUAL, "Holiday", false);
        LeaveRequest requestB = LeaveRequest.submit(staffIdB, range, LeaveType.ANNUAL, "Holiday", false);
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestA, requestB));

        List<LeaveRequestResponseDto> result = service.getOutstanding(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void filtersToASingleStaffMemberWhenStaffIdProvided() {
        LeaveRequest requestA = LeaveRequest.submit(staffIdA, range, LeaveType.ANNUAL, "Holiday", false);
        LeaveRequest requestB = LeaveRequest.submit(staffIdB, range, LeaveType.ANNUAL, "Holiday", false);
        when(leaveRequestRepository.findByStatusIn(any())).thenReturn(List.of(requestA, requestB));

        List<LeaveRequestResponseDto> result = service.getOutstanding(staffIdA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).staffId()).isEqualTo(staffIdA);
    }
}