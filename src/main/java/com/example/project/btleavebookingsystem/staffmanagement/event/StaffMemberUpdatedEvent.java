package com.example.project.btleavebookingsystem.staffmanagement.event;

import java.util.UUID;

public record StaffMemberUpdatedEvent(
        UUID staffId,
        String department,
        String jobLevel,
        String employmentStatus
) {
}