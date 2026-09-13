package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public record LeaveRequestApprovedEvent(UUID leaveRequestId, UUID staffId, long numberOfDays)
        implements LeaveManagementDomainEvent {
}