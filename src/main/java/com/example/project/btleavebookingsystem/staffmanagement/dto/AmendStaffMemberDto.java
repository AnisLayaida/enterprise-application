package com.example.project.btleavebookingsystem.staffmanagement.dto;

import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmendStaffMemberDto(
        @NotBlank String department,
        @NotBlank String jobLevel,
        @NotNull StaffMember.EmploymentStatus employmentStatus
) {
}