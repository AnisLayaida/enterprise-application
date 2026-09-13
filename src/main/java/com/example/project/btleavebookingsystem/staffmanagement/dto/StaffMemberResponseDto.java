package com.example.project.btleavebookingsystem.staffmanagement.dto;

import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;

import java.time.LocalDate;
import java.util.UUID;

public record StaffMemberResponseDto(
        UUID id, String firstName, String surname, String email,
        LocalDate hireDate, String department, UUID lineManagerId,
        String jobLevel, StaffMember.EmploymentStatus employmentStatus
) {
    public static StaffMemberResponseDto from(StaffMember s) {
        return new StaffMemberResponseDto(
                s.getId(), s.getFirstName(), s.getSurname(), s.getEmail(),
                s.getHireDate(), s.getDepartment(), s.getLineManagerId(),
                s.getJobLevel(), s.getEmploymentStatus());
    }
}