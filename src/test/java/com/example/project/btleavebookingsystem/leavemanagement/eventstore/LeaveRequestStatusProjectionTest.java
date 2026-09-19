package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeaveRequestStatusProjectionTest {

    @Test
    void directManagerApprovalReplaysToApproved() {
        assertThat(LeaveRequestStatusProjection.project(List.of(
                "LeaveRequestSubmittedEvent", "LeaveRequestApprovedEvent")))
                .isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void managerRejectionReplaysToRejected() {
        assertThat(LeaveRequestStatusProjection.project(List.of(
                "LeaveRequestSubmittedEvent", "LeaveRequestRejectedEvent")))
                .isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    void escalationWithoutHrDecisionReplaysToManagerReviewed() {
        assertThat(LeaveRequestStatusProjection.project(List.of(
                "LeaveRequestSubmittedEvent", "LeaveRequestEscalatedToHREvent")))
                .isEqualTo(RequestStatus.MANAGER_REVIEWED);
    }

    @Test
    void fullHrPathFollowedByCancellationReplaysToCancelled() {
        assertThat(LeaveRequestStatusProjection.project(List.of(
                "LeaveRequestSubmittedEvent", "LeaveRequestEscalatedToHREvent",
                "LeaveRequestApprovedEvent", "LeaveRequestCancelledEvent")))
                .isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void emptyStreamReplaysToNull() {
        assertThat(LeaveRequestStatusProjection.project(List.of())).isNull();
    }

    @Test
    void unknownEventTypeInTheStreamIsRejected() {
        assertThatThrownBy(() -> LeaveRequestStatusProjection.project(List.of("SomethingElseEvent")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SomethingElseEvent");
    }
}