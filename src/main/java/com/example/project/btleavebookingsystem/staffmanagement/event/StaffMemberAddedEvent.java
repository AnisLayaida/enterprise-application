package com.example.project.btleavebookingsystem.staffmanagement.event;

import java.util.UUID;

public record StaffMemberAddedEvent(
        UUID staffId,
        String firstName,
        String surname,
        String email,
        String department
) {
}