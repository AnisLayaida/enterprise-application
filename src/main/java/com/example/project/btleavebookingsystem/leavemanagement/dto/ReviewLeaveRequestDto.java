package com.example.project.btleavebookingsystem.leavemanagement.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewLeaveRequestDto(@NotNull Boolean approved) {
}