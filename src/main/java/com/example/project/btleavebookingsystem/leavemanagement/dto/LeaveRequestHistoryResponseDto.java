package com.example.project.btleavebookingsystem.leavemanagement.dto;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LeaveRequestHistoryResponseDto(
        UUID leaveRequestId,
        RequestStatus currentStatus,
        RequestStatus replayedStatus,
        boolean consistent,
        List<EventEntry> events
) {
    public record EventEntry(
            long sequenceNumber,
            String eventType,
            LocalDateTime occurredAt,
            Map<String, Object> payload
    ) {
    }
}