package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public record LeaveRequestRejectedEvent(UUID leaveRequestId, UUID staffId)
        implements LeaveManagementDomainEvent {
}