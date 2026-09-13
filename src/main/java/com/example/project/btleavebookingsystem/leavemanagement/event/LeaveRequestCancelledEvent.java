package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public record LeaveRequestCancelledEvent(UUID leaveRequestId, UUID staffId, boolean wasApproved, long numberOfDays)
        implements LeaveManagementDomainEvent {
}