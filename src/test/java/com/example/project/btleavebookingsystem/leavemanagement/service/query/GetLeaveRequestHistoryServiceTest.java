package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestHistoryResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.eventstore.LeaveEventRecord;
import com.example.project.btleavebookingsystem.leavemanagement.eventstore.LeaveEventRecordRepository;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetLeaveRequestHistoryServiceTest {

    private final UUID staffId = UUID.randomUUID();
    private final DateRange range = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));

    private LeaveRequestRepository leaveRequestRepository;
    private LeaveEventRecordRepository eventRecordRepository;
    private GetLeaveRequestHistoryService service;

    @BeforeEach
    void setUp() {
        leaveRequestRepository = mock(LeaveRequestRepository.class);
        eventRecordRepository = mock(LeaveEventRecordRepository.class);
        service = new GetLeaveRequestHistoryService(leaveRequestRepository, eventRecordRepository);
    }

    @Test
    void historyReplaysToTheSameStatusAsTheStateTableAndIsMarkedConsistent() {
        LeaveRequest request = approvedRequest();
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(eventRecordRepository.findByAggregateIdOrderBySequenceNumberAsc(request.getId())).thenReturn(List.of(
                new LeaveEventRecord(request.getId(), 1, "LeaveRequestSubmittedEvent", staffId, "{}"),
                new LeaveEventRecord(request.getId(), 2, "LeaveRequestApprovedEvent", staffId, "{\"numberOfDays\":5}")));

        LeaveRequestHistoryResponseDto history = service.getHistory(request.getId());

        assertThat(history.currentStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(history.replayedStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(history.consistent()).isTrue();
        assertThat(history.events()).hasSize(2);
        assertThat(history.events().get(1).payload()).containsEntry("numberOfDays", 5);
    }

    @Test
    void driftBetweenTheStateTableAndTheEventStreamIsReportedAsInconsistent() {
        LeaveRequest request = approvedRequest();
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(eventRecordRepository.findByAggregateIdOrderBySequenceNumberAsc(request.getId())).thenReturn(List.of(
                new LeaveEventRecord(request.getId(), 1, "LeaveRequestSubmittedEvent", staffId, "{}")));

        LeaveRequestHistoryResponseDto history = service.getHistory(request.getId());

        assertThat(history.replayedStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(history.consistent()).isFalse();
    }

    @Test
    void historyForAnUnknownRequestThrowsNotFound() {
        UUID missing = UUID.randomUUID();
        when(leaveRequestRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistory(missing))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private LeaveRequest approvedRequest() {
        LeaveRequest request = LeaveRequest.submit(staffId, range, LeaveType.ANNUAL, "Holiday", false);
        request.reviewByManager(true);
        request.pullDomainEvents();
        return request;
    }
}