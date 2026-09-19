package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestEscalatedToHREvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestRejectedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveNotificationListenerTest {

    private final UUID staffId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    private LeaveNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new LeaveNotificationListener();
    }

    @Test
    void submittedRequestRaisesAManagerAlertOnly() {
        listener.onSubmitted(new LeaveRequestSubmittedEvent(requestId, staffId));

        assertThat(listener.getManagerAlertCount()).isEqualTo(1);
        assertThat(listener.getHrAlertCount()).isZero();
        assertThat(listener.getStaffAlertCount()).isZero();
    }

    @Test
    void escalatedRequestRaisesAnHrAlertOnly() {
        listener.onEscalated(new LeaveRequestEscalatedToHREvent(requestId, staffId, true));

        assertThat(listener.getHrAlertCount()).isEqualTo(1);
        assertThat(listener.getManagerAlertCount()).isZero();
        assertThat(listener.getStaffAlertCount()).isZero();
    }

    @Test
    void approvedRequestRaisesAStaffAlertOnly() {
        listener.onApproved(new LeaveRequestApprovedEvent(requestId, staffId, LeaveType.ANNUAL, 2026, 5));

        assertThat(listener.getStaffAlertCount()).isEqualTo(1);
        assertThat(listener.getManagerAlertCount()).isZero();
    }

    @Test
    void rejectedRequestRaisesAStaffAlertOnly() {
        listener.onRejected(new LeaveRequestRejectedEvent(requestId, staffId));

        assertThat(listener.getStaffAlertCount()).isEqualTo(1);
        assertThat(listener.getManagerAlertCount()).isZero();
    }

    @Test
    void cancelledRequestRaisesAStaffAlertOnly() {
        listener.onCancelled(new LeaveRequestCancelledEvent(requestId, staffId, true, LeaveType.ANNUAL, 2026, 5));

        assertThat(listener.getStaffAlertCount()).isEqualTo(1);
        assertThat(listener.getManagerAlertCount()).isZero();
    }
}