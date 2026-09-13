package com.example.project.btleavebookingsystem.leavemanagement.dto;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;

import java.util.UUID;

public record LeaveAllowanceResponseDto(
        UUID id, UUID staffId, int businessYear, int entitledDays, int remainingDays
) {
    public static LeaveAllowanceResponseDto from(LeaveAllowance a) {
        return new LeaveAllowanceResponseDto(
                a.getId(), a.getStaffId(), a.getBusinessYear(), a.getEntitledDays(), a.getRemainingDays());
    }
}