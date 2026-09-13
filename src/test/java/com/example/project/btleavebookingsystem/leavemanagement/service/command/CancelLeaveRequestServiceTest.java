package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelLeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CancelLeaveRequestService service;

    private final UUID staffId = UUID.randomUUID();
    private final DateRange fiveDayRange = new DateRange(
            LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));

    @BeforeEach
    void setUp() {
        service = new CancelLeaveRequestService(leaveRequestRepository, eventPublisher);
    }

    @Test
    void cancelThrowsResourceNotFoundWhenRequestDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(leaveRequestRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelTransitionsPendingRequestToCancelledAndPublishesCancelledEvent() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        LeaveRequestResponseDto response = service.cancel(request.getId());

        assertThat(response.status().name()).isEqualTo("CANCELLED");
        verify(eventPublisher).publishEvent(any(Object.class));
    }
}