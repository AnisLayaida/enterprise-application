package com.example.project.btleavebookingsystem.leavemanagement.domain;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestEscalatedToHREvent;
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
    void managerReviewWithHrRequiredEscalatesAndRaisesEscalationEventRecordingTheRecommendation() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.ANNUAL, "Holiday", true);
        request.pullDomainEvents();

        request.reviewByManager(true);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.MANAGER_REVIEWED);
        LeaveRequestEscalatedToHREvent event =
                (LeaveRequestEscalatedToHREvent) request.pullDomainEvents().get(0);
        assertThat(event.leaveRequestId()).isEqualTo(request.getId());
        assertThat(event.managerRecommendedApproval()).isTrue();
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

    @Test
    void approvedEventCarriesTheLeaveTypeAndBusinessYearOfTheLeave() {
        LeaveRequest request = LeaveRequest.submit(staffId, fiveDayRange, LeaveType.SICK, "Unwell", false);
        request.pullDomainEvents();

        request.reviewByManager(true);

        LeaveRequestApprovedEvent event = (LeaveRequestApprovedEvent) request.pullDomainEvents().get(0);
        assertThat(event.leaveType()).isEqualTo(LeaveType.SICK);
        assertThat(event.businessYear()).isEqualTo(2026);
        assertThat(event.numberOfDays()).isEqualTo(5);
    }

    @Test
    void businessYearComesFromTheLeaveStartDateNotTheCurrentYear() {
        DateRange nextYearRange = new DateRange(LocalDate.of(2027, 1, 4), LocalDate.of(2027, 1, 8));
        LeaveRequest request = LeaveRequest.submit(staffId, nextYearRange, LeaveType.ANNUAL, "New Year trip", false);
        request.pullDomainEvents();

        request.reviewByManager(true);
        LeaveRequestApprovedEvent approved = (LeaveRequestApprovedEvent) request.pullDomainEvents().get(0);

        request.cancel();
        LeaveRequestCancelledEvent cancelled = (LeaveRequestCancelledEvent) request.pullDomainEvents().get(0);

        assertThat(approved.businessYear()).isEqualTo(2027);
        assertThat(cancelled.businessYear()).isEqualTo(2027);
    }
}