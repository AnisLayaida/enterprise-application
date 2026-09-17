package com.example.project.btleavebookingsystem.leavemanagement.domain;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import com.example.project.btleavebookingsystem.shared.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeaveRequestTest {

    private final UUID staffId = UUID.randomUUID();

    private final DateRange fiveDayRange = new DateRange(
            LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));

    @Test
    void submitCreatesPendingRequestAndRaisesSubmittedEvent() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(request.pullDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(LeaveRequestSubmittedEvent.class);
    }

    @Test
    void pullDomainEventsClearsTheListAfterReading() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();

        assertThat(request.pullDomainEvents()).isEmpty();
    }

    @Test
    void managerApprovalWithNoHrRequiredGoesStraightToApprovedAndRaisesApprovedEvent() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();

        request.reviewByManager(true);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(request.pullDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(LeaveRequestApprovedEvent.class);
    }

    @Test
    void managerApprovalWithHrRequiredEscalatesAndRaisesNoOutcomeEventYet() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", true);
        request.pullDomainEvents();

        request.reviewByManager(true);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.MANAGER_REVIEWED);
        assertThat(request.pullDomainEvents()).isEmpty();
    }

    @Test
    void hrReviewAfterEscalationResolvesToApprovedAndRaisesEvent() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", true);
        request.reviewByManager(true);
        request.pullDomainEvents();

        request.resolveHRReview(true);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(request.pullDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(LeaveRequestApprovedEvent.class);
    }

    @Test
    void cancellingAnApprovedRequestFlagsWasApprovedTrueInTheEvent() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.reviewByManager(true);
        request.pullDomainEvents();

        request.cancel();

        LeaveRequestCancelledEvent event = (LeaveRequestCancelledEvent) request.pullDomainEvents().get(0);
        assertThat(event.wasApproved()).isTrue();
        assertThat(event.numberOfDays()).isEqualTo(5);
    }

    @Test
    void cancellingAPendingRequestFlagsWasApprovedFalse() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.pullDomainEvents();

        request.cancel();

        LeaveRequestCancelledEvent event = (LeaveRequestCancelledEvent) request.pullDomainEvents().get(0);
        assertThat(event.wasApproved()).isFalse();
    }

    @Test
    void cancellingAnAlreadyCancelledRequestThrows() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", false);
        request.cancel();

        assertThatThrownBy(request::cancel)
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}