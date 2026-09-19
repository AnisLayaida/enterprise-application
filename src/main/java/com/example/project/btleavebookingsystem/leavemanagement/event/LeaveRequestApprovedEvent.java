package com.example.project.btleavebookingsystem.leavemanagement.event;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import java.util.UUID;

public record LeaveRequestApprovedEvent(UUID leaveRequestId,
                                        UUID staffId,
                                        LeaveType leaveType,
                                        int businessYear,
                                        long numberOfDays)
        implements LeaveManagementDomainEvent {
}