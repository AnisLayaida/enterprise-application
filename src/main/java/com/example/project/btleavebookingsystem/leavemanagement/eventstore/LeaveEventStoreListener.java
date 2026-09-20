package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveManagementDomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LeaveEventStoreListener {

    private final LeaveEventRecordRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeaveEventStoreListener(LeaveEventRecordRepository repository) {
        this.repository = repository;
    }

    @EventListener
    @Transactional
    public void append(LeaveManagementDomainEvent event) {
        long nextSequence = repository.countByAggregateId(event.leaveRequestId()) + 1;

        repository.save(new LeaveEventRecord(
                event.leaveRequestId(),
                nextSequence,
                event.getClass().getSimpleName(),
                event.staffId(),
                serialise(event)));
    }

    private String serialise(LeaveManagementDomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialise domain event " + event.getClass().getSimpleName(), e);
        }
    }
}