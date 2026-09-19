package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestHistoryResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.eventstore.LeaveEventRecord;
import com.example.project.btleavebookingsystem.leavemanagement.eventstore.LeaveEventRecordRepository;
import com.example.project.btleavebookingsystem.leavemanagement.eventstore.LeaveRequestStatusProjection;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetLeaveRequestHistoryService {

    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {};

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveEventRecordRepository eventRecordRepository;
    private final LeaveAccessPolicy accessPolicy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetLeaveRequestHistoryService(LeaveRequestRepository leaveRequestRepository,
                                         LeaveEventRecordRepository eventRecordRepository,
                                         LeaveAccessPolicy accessPolicy) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.accessPolicy = accessPolicy;
    }

    public LeaveRequestHistoryResponseDto getHistory(UUID leaveRequestId, ActingUser viewer) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));

        accessPolicy.assertCanView(viewer, request.getStaffId());

        List<LeaveEventRecord> stream =
                eventRecordRepository.findByAggregateIdOrderBySequenceNumberAsc(leaveRequestId);

        RequestStatus replayed = LeaveRequestStatusProjection.project(
                stream.stream().map(LeaveEventRecord::getEventType).toList());

        List<LeaveRequestHistoryResponseDto.EventEntry> entries = stream.stream()
                .map(record -> new LeaveRequestHistoryResponseDto.EventEntry(
                        record.getSequenceNumber(),
                        record.getEventType(),
                        record.getOccurredAt(),
                        parse(record.getPayload())))
                .toList();

        return new LeaveRequestHistoryResponseDto(
                request.getId(),
                request.getStatus(),
                replayed,
                request.getStatus() == replayed,
                entries);
    }

    private Map<String, Object> parse(String payload) {
        try {
            return objectMapper.readValue(payload, PAYLOAD_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Corrupt event payload in event store", e);
        }
    }
}