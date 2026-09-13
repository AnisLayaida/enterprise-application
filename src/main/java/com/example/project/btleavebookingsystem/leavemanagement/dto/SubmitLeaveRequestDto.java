package com.example.project.btleavebookingsystem.leavemanagement.dto;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SubmitLeaveRequestDto(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Size(max = 500) String reason,
        boolean requiresHRApproval
) {
}