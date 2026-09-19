package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public record LeaveRequestEscalatedToHREvent(UUID leaveRequestId,
                                             UUID staffId,
                                             boolean managerRecommendedApproval)
        implements LeaveManagementDomainEvent {
}