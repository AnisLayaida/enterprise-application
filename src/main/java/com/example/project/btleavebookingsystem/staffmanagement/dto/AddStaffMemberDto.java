package com.example.project.btleavebookingsystem.staffmanagement.dto;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AddStaffMemberDto(
        @NotBlank String firstName,
        @NotBlank String surname,
        @NotBlank @Email String email,
        @NotNull LocalDate hireDate,
        @NotBlank String department,
        UUID lineManagerId,
        @NotBlank String jobLevel,
        @NotNull StaffMember.EmploymentStatus employmentStatus,
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotNull Role.RoleName role
) {
}