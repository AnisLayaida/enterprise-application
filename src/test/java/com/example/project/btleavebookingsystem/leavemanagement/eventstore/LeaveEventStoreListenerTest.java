package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LeaveEventStoreListenerTest {

    private final UUID requestId = UUID.randomUUID();
    private final UUID staffId = UUID.randomUUID();

    private LeaveEventRecordRepository repository;
    private LeaveEventStoreListener listener;

    @BeforeEach
    void setUp() {
        repository = mock(LeaveEventRecordRepository.class);
        listener = new LeaveEventStoreListener(repository);
    }

    @Test
    void firstEventInAStreamIsAppendedWithSequenceOne() {
        when(repository.countByAggregateId(requestId)).thenReturn(0L);

        listener.append(new LeaveRequestSubmittedEvent(requestId, staffId));

        ArgumentCaptor<LeaveEventRecord> saved = ArgumentCaptor.forClass(LeaveEventRecord.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getAggregateId()).isEqualTo(requestId);
        assertThat(saved.getValue().getSequenceNumber()).isEqualTo(1);
        assertThat(saved.getValue().getEventType()).isEqualTo("LeaveRequestSubmittedEvent");
        assertThat(saved.getValue().getStaffId()).isEqualTo(staffId);
    }

    @Test
    void laterEventsAreAppendedWithTheNextSequenceNumberAndAFullJsonPayload() {
        when(repository.countByAggregateId(requestId)).thenReturn(2L);

        listener.append(new LeaveRequestApprovedEvent(requestId, staffId, LeaveType.ANNUAL, 2026, 5));

        ArgumentCaptor<LeaveEventRecord> saved = ArgumentCaptor.forClass(LeaveEventRecord.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getSequenceNumber()).isEqualTo(3);
        assertThat(saved.getValue().getEventType()).isEqualTo("LeaveRequestApprovedEvent");
        assertThat(saved.getValue().getPayload())
                .contains("\"leaveType\":\"ANNUAL\"")
                .contains("\"businessYear\":2026")
                .contains("\"numberOfDays\":5");
    }
}