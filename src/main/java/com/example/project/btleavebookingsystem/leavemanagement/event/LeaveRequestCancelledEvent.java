package com.example.project.btleavebookingsystem.leavemanagement.event;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import java.util.UUID;

public record LeaveRequestCancelledEvent(UUID leaveRequestId,
                                         UUID staffId,
                                         boolean wasApproved,
                                         LeaveType leaveType,
                                         int businessYear,
                                         long numberOfDays)
        implements LeaveManagementDomainEvent {
}