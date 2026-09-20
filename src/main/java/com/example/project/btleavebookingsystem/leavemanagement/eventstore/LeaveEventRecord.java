package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_event_store",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_leave_event_stream_sequence",
                columnNames = {"aggregate_id", "sequence_number"}),
        indexes = @Index(name = "idx_leave_event_aggregate", columnList = "aggregate_id"))
public class LeaveEventRecord {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(name = "sequence_number", nullable = false, updatable = false)
    private long sequenceNumber;

    @Column(name = "event_type", nullable = false, updatable = false, length = 100)
    private String eventType;

    @Column(name = "staff_id", nullable = false, updatable = false)
    private UUID staffId;

    @Column(name = "payload", nullable = false, updatable = false, length = 2000)
    private String payload;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    protected LeaveEventRecord() {
    }

    public LeaveEventRecord(UUID aggregateId, long sequenceNumber, String eventType,
                            UUID staffId, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.sequenceNumber = sequenceNumber;
        this.eventType = eventType;
        this.staffId = staffId;
        this.payload = payload;
        this.occurredAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public long getSequenceNumber() { return sequenceNumber; }
    public String getEventType() { return eventType; }
    public UUID getStaffId() { return staffId; }
    public String getPayload() { return payload; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}