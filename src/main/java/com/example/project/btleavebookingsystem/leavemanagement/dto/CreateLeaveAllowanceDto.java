package com.example.project.btleavebookingsystem.leavemanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLeaveAllowanceDto(
        @NotNull UUID staffId,
        @NotNull @Min(2000) Integer businessYear,
        @NotNull @Min(0) Integer entitledDays
) {
}