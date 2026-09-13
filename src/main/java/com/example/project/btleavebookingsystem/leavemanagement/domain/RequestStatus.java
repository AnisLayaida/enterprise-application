package com.example.project.btleavebookingsystem.leavemanagement.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum RequestStatus {
    PENDING,
    MANAGER_REVIEWED,
    HR_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED;

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, EnumSet.of(MANAGER_REVIEWED, CANCELLED),
            MANAGER_REVIEWED, EnumSet.of(HR_REVIEW, APPROVED, REJECTED, CANCELLED),
            HR_REVIEW, EnumSet.of(APPROVED, REJECTED, CANCELLED),
            APPROVED, EnumSet.of(CANCELLED),
            REJECTED, EnumSet.of(CANCELLED),
            CANCELLED, EnumSet.noneOf(RequestStatus.class)
    );

    public boolean canTransitionTo(RequestStatus next) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }
}