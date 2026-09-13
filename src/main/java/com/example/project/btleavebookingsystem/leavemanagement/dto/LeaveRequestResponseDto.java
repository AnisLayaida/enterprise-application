package com.example.project.btleavebookingsystem.leavemanagement.dto;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveRequestResponseDto(
        UUID id,
        UUID staffId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveType leaveType,
        String reason,
        RequestStatus status,
        boolean requiresHRApproval,
        LocalDateTime createdAt
) {
    public static LeaveRequestResponseDto from(LeaveRequest request) {
        return new LeaveRequestResponseDto(
                request.getId(),
                request.getStaffId(),
                request.getDateRange().getStartDate(),
                request.getDateRange().getEndDate(),
                request.getLeaveType(),
                request.getReason(),
                request.getStatus(),
                request.isRequiresHRApproval(),
                request.getCreatedAt()
        );
    }
}