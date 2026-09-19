package com.example.project.btleavebookingsystem.leavemanagement.event;

import java.util.UUID;

public interface LeaveManagementDomainEvent {

    UUID leaveRequestId();

    UUID staffId();
}