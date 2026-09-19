package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestEscalatedToHREvent;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewLeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LeaveAccessPolicy accessPolicy;

    private ReviewLeaveRequestService service;

    private final UUID staffId = UUID.randomUUID();
    private final ActingUser lineManager = new ActingUser(UUID.randomUUID(), false);
    private final DateRange fiveDayRange = new DateRange(
            LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));

    @BeforeEach
    void setUp() {
        service = new ReviewLeaveRequestService(leaveRequestRepository, eventPublisher, accessPolicy);
    }

    @Test
    void reviewThrowsResourceNotFoundWhenRequestDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(leaveRequestRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.review(missingId, true, lineManager))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reviewApprovesDirectlyAndPublishesApprovedEventWhenNoHrRequired() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        LeaveRequestResponseDto response = service.review(request.getId(), true, lineManager);

        assertThat(response.status().name()).isEqualTo("APPROVED");
        verify(accessPolicy).assertCanReview(lineManager, staffId);
        verify(eventPublisher).publishEvent(any(LeaveRequestApprovedEvent.class));
    }

    @Test
    void reviewEscalatesToManagerReviewedAndPublishesEscalationEventWhenHrRequired() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", true);
        request.pullDomainEvents();
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        LeaveRequestResponseDto response = service.review(request.getId(), true, lineManager);

        assertThat(response.status().name()).isEqualTo("MANAGER_REVIEWED");
        verify(eventPublisher).publishEvent(any(LeaveRequestEscalatedToHREvent.class));
    }

    @Test
    void managerOutsideTheReportingLineIsDeniedAndTheRequestIsUnchanged() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();
        ActingUser otherManager = new ActingUser(UUID.randomUUID(), false);
        when(leaveRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        doThrow(new AccessDeniedException("denied")).when(accessPolicy).assertCanReview(otherManager, staffId);

        assertThatThrownBy(() -> service.review(request.getId(), true, otherManager))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        verify(leaveRequestRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}