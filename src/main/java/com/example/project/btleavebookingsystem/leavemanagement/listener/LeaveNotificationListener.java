package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestEscalatedToHREvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestRejectedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Raises the alerts required by the brief:
 *  - manager alert when a leave request is submitted and is pending review
 *  - HR alert when a manager escalates a request that requires HR approval
 *  - staff alert when their request is approved, rejected or cancelled
 *
 * Alerts fire only after the originating transaction commits, so no one is notified
 * about a change that was rolled back. In this prototype the delivery channel is a
 * structured ALERT log entry; in production this listener would hand off to an
 * email/Teams service or publish to a notifications queue.
 */
@Component
public class LeaveNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(LeaveNotificationListener.class);

    private final AtomicInteger managerAlertCount = new AtomicInteger(0);
    private final AtomicInteger hrAlertCount = new AtomicInteger(0);
    private final AtomicInteger staffAlertCount = new AtomicInteger(0);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSubmitted(LeaveRequestSubmittedEvent event) {
        managerAlertCount.incrementAndGet();
        log.info("ALERT [manager]: new leave request {} from staff {} is pending review "
                + "(visible via GET /api/leave-requests/team)", event.leaveRequestId(), event.staffId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEscalated(LeaveRequestEscalatedToHREvent event) {
        hrAlertCount.incrementAndGet();
        log.info("ALERT [HR]: leave request {} from staff {} requires HR approval "
                        + "(manager recommendation: {})",
                event.leaveRequestId(), event.staffId(),
                event.managerRecommendedApproval() ? "approve" : "reject");
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
        log.info("ALERT [staff {}]: your leave request {} has been REJECTED",
                event.staffId(), event.leaveRequestId());
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

    public int getHrAlertCount() {
        return hrAlertCount.get();
    }

    public int getStaffAlertCount() {
        return staffAlertCount.get();
    }
}