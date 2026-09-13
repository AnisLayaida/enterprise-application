package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public record LeaveRequestSubmittedEvent(UUID leaveRequestId, UUID staffId)
        implements LeaveManagementDomainEvent {
}