package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveEventRecordRepository extends JpaRepository<LeaveEventRecord, UUID> {

    List<LeaveEventRecord> findByAggregateIdOrderBySequenceNumberAsc(UUID aggregateId);

    long countByAggregateId(UUID aggregateId);
}