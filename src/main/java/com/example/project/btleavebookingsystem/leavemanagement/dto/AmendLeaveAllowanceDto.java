package com.example.project.btleavebookingsystem.leavemanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AmendLeaveAllowanceDto(@NotNull @Min(0) Integer entitledDays) {
}