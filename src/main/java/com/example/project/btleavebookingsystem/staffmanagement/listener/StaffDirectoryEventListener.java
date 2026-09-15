package com.example.project.btleavebookingsystem.staffmanagement.listener;

import com.example.project.btleavebookingsystem.shared.messaging.RabbitTopologyConfig;
import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberAddedEvent;
import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RabbitListener(queues = RabbitTopologyConfig.STAFF_DIRECTORY_QUEUE)
public class StaffDirectoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(StaffDirectoryEventListener.class);

    private final RabbitTemplate rabbitTemplate;

    private final AtomicInteger addedHandledCount = new AtomicInteger(0);
    private final AtomicInteger updatedHandledCount = new AtomicInteger(0);
    private final AtomicInteger unrecognisedCount = new AtomicInteger(0);

    public StaffDirectoryEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStaffAdded(StaffMemberAddedEvent event) {
        log.info("Publishing StaffMemberAddedEvent for staffId={} to RabbitMQ", event.staffId());
        rabbitTemplate.convertAndSend(
                RabbitTopologyConfig.STAFF_EVENTS_EXCHANGE,
                RabbitTopologyConfig.STAFF_MEMBER_ADDED_ROUTING_KEY,
                event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStaffUpdated(StaffMemberUpdatedEvent event) {
        log.info("Publishing StaffMemberUpdatedEvent for staffId={} to RabbitMQ", event.staffId());
        rabbitTemplate.convertAndSend(
                RabbitTopologyConfig.STAFF_EVENTS_EXCHANGE,
                RabbitTopologyConfig.STAFF_MEMBER_UPDATED_ROUTING_KEY,
                event);
    }

    @RabbitHandler
    public void handleStaffMemberAdded(StaffMemberAddedEvent added) {
        addedHandledCount.incrementAndGet();
        log.info("Consumed StaffMemberAddedEvent from RabbitMQ: staffId={}, name={} {} " +
                        "— simulating an external HR/directory system receiving this notification",
                added.staffId(), added.firstName(), added.surname());
    }

    @RabbitHandler
    public void handleStaffMemberUpdated(StaffMemberUpdatedEvent updated) {
        updatedHandledCount.incrementAndGet();
        log.info("Consumed StaffMemberUpdatedEvent from RabbitMQ: staffId={}, department={}, jobLevel={} " +
                        "— simulating an external HR/directory system receiving this notification",
                updated.staffId(), updated.department(), updated.jobLevel());
    }

    @RabbitHandler(isDefault = true)
    public void handleUnrecognised(Object payload) {
        unrecognisedCount.incrementAndGet();
        log.warn("Received unrecognised message type on staff directory queue: {}", payload.getClass());
    }

    public int getAddedHandledCount() {
        return addedHandledCount.get();
    }

    public int getUpdatedHandledCount() {
        return updatedHandledCount.get();
    }

    public int getUnrecognisedCount() {
        return unrecognisedCount.get();
    }
}