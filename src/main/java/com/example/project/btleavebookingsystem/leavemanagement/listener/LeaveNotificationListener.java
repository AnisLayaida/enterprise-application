package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestRejectedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LeaveNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(LeaveNotificationListener.class);

    private final AtomicInteger managerAlertCount = new AtomicInteger(0);
    private final AtomicInteger staffAlertCount = new AtomicInteger(0);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSubmitted(LeaveRequestSubmittedEvent event) {
        managerAlertCount.incrementAndGet();
        log.info("ALERT [manager]: new leave request pending review - {} "
                + "(visible to the line manager via GET /api/leave-requests/team)", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onApproved(LeaveRequestApprovedEvent event) {
        staffAlertCount.incrementAndGet();
        log.info("ALERT [staff {}]: your {} leave request {} has been APPROVED ({} working days, business year {})",
                event.staffId(), event.leaveType(), event.leaveRequestId(),
                event.numberOfDays(), event.businessYear());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRejected(LeaveRequestRejectedEvent event) {
        staffAlertCount.incrementAndGet();
        log.info("ALERT [staff]: your leave request has been REJECTED - {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCancelled(LeaveRequestCancelledEvent event) {
        staffAlertCount.incrementAndGet();
        log.info("ALERT [staff {}]: your {} leave request {} has been CANCELLED{}",
                event.staffId(), event.leaveType(), event.leaveRequestId(),
                event.wasApproved() ? " (" + event.numberOfDays() + " working days returned where applicable)" : "");
    }

    public int getManagerAlertCount() {
        return managerAlertCount.get();
    }

    public int getStaffAlertCount() {
        return staffAlertCount.get();
    }
}