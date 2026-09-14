package com.example.project.btleavebookingsystem.leavemanagement.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestStatusTest {

    @Test
    void pendingCanMoveToManagerReviewedApprovedRejectedOrCancelled() {
        assertThat(RequestStatus.PENDING.canTransitionTo(RequestStatus.MANAGER_REVIEWED)).isTrue();
        assertThat(RequestStatus.PENDING.canTransitionTo(RequestStatus.APPROVED)).isTrue();
        assertThat(RequestStatus.PENDING.canTransitionTo(RequestStatus.REJECTED)).isTrue();
        assertThat(RequestStatus.PENDING.canTransitionTo(RequestStatus.CANCELLED)).isTrue();
    }

    @Test
    void pendingCannotMoveDirectlyToHrReview() {
        assertThat(RequestStatus.PENDING.canTransitionTo(RequestStatus.HR_REVIEW)).isFalse();
    }

    @Test
    void managerReviewedCanOnlyEscalateToHrOrBeCancelled() {
        assertThat(RequestStatus.MANAGER_REVIEWED.canTransitionTo(RequestStatus.HR_REVIEW)).isTrue();
        assertThat(RequestStatus.MANAGER_REVIEWED.canTransitionTo(RequestStatus.CANCELLED)).isTrue();
    }

    @Test
    void managerReviewedCannotSkipStraightToApprovedOrRejected() {
        assertThat(RequestStatus.MANAGER_REVIEWED.canTransitionTo(RequestStatus.APPROVED)).isFalse();
        assertThat(RequestStatus.MANAGER_REVIEWED.canTransitionTo(RequestStatus.REJECTED)).isFalse();
    }

    @Test
    void cancelledIsATerminalState() {
        for (RequestStatus target : RequestStatus.values()) {
            assertThat(RequestStatus.CANCELLED.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    void approvedAndRejectedCanOnlyBeCancelled() {
        assertThat(RequestStatus.APPROVED.canTransitionTo(RequestStatus.CANCELLED)).isTrue();
        assertThat(RequestStatus.REJECTED.canTransitionTo(RequestStatus.CANCELLED)).isTrue();
        assertThat(RequestStatus.APPROVED.canTransitionTo(RequestStatus.PENDING)).isFalse();
    }

    @Test
    void onlyRejectedAndCancelledAreInactive() {
        assertThat(RequestStatus.PENDING.isActive()).isTrue();
        assertThat(RequestStatus.MANAGER_REVIEWED.isActive()).isTrue();
        assertThat(RequestStatus.HR_REVIEW.isActive()).isTrue();
        assertThat(RequestStatus.APPROVED.isActive()).isTrue();
        assertThat(RequestStatus.REJECTED.isActive()).isFalse();
        assertThat(RequestStatus.CANCELLED.isActive()).isFalse();
    }
}