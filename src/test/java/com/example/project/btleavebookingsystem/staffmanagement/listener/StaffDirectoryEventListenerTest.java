package com.example.project.btleavebookingsystem.staffmanagement.listener;

import com.example.project.btleavebookingsystem.shared.messaging.RabbitTopologyConfig;
import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberAddedEvent;
import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaffDirectoryEventListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private StaffDirectoryEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new StaffDirectoryEventListener(rabbitTemplate);
    }

    @Test
    void handleStaffMemberAddedIncrementsTheAddedCounterOnly() {
        StaffMemberAddedEvent event = new StaffMemberAddedEvent(
                UUID.randomUUID(), "Jane", "Doe", "jane.doe@bt.com", "QA");

        listener.handleStaffMemberAdded(event);

        assertThat(listener.getAddedHandledCount()).isEqualTo(1);
        assertThat(listener.getUpdatedHandledCount()).isEqualTo(0);
        assertThat(listener.getUnrecognisedCount()).isEqualTo(0);
    }

    @Test
    void handleStaffMemberUpdatedIncrementsTheUpdatedCounterOnly() {
        StaffMemberUpdatedEvent event = new StaffMemberUpdatedEvent(
                UUID.randomUUID(), "QA", "Senior Engineer", "ACTIVE");

        listener.handleStaffMemberUpdated(event);

        assertThat(listener.getUpdatedHandledCount()).isEqualTo(1);
        assertThat(listener.getAddedHandledCount()).isEqualTo(0);
        assertThat(listener.getUnrecognisedCount()).isEqualTo(0);
    }

    @Test
    void handleUnrecognisedIncrementsTheUnrecognisedCounterOnlyForAnUnknownPayloadType() {
        listener.handleUnrecognised("some-unexpected-string-payload");

        assertThat(listener.getUnrecognisedCount()).isEqualTo(1);
        assertThat(listener.getAddedHandledCount()).isEqualTo(0);
        assertThat(listener.getUpdatedHandledCount()).isEqualTo(0);
    }

    @Test
    void onStaffAddedPublishesToTheCorrectExchangeAndRoutingKey() {
        StaffMemberAddedEvent event = new StaffMemberAddedEvent(
                UUID.randomUUID(), "Jane", "Doe", "jane.doe@bt.com", "QA");

        listener.onStaffAdded(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitTopologyConfig.STAFF_EVENTS_EXCHANGE,
                RabbitTopologyConfig.STAFF_MEMBER_ADDED_ROUTING_KEY,
                event);
    }

    @Test
    void onStaffUpdatedPublishesToTheCorrectExchangeAndRoutingKey() {
        StaffMemberUpdatedEvent event = new StaffMemberUpdatedEvent(
                UUID.randomUUID(), "QA", "Senior Engineer", "ACTIVE");

        listener.onStaffUpdated(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitTopologyConfig.STAFF_EVENTS_EXCHANGE,
                RabbitTopologyConfig.STAFF_MEMBER_UPDATED_ROUTING_KEY,
                event);
    }
}