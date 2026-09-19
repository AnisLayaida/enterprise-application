package com.example.project.btleavebookingsystem.leavemanagement.eventstore;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestEscalatedToHREvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestRejectedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;

import java.util.List;
import java.util.Map;

/**
 * Derives a leave request's status purely by replaying its event stream in order
 * (state as a left fold over events). Used to verify that the event store and the
 * state table agree.
 */
public final class LeaveRequestStatusProjection {

    private static final Map<String, RequestStatus> STATUS_AFTER_EVENT = Map.of(
            LeaveRequestSubmittedEvent.class.getSimpleName(), RequestStatus.PENDING,
            LeaveRequestEscalatedToHREvent.class.getSimpleName(), RequestStatus.MANAGER_REVIEWED,
            LeaveRequestApprovedEvent.class.getSimpleName(), RequestStatus.APPROVED,
            LeaveRequestRejectedEvent.class.getSimpleName(), RequestStatus.REJECTED,
            LeaveRequestCancelledEvent.class.getSimpleName(), RequestStatus.CANCELLED
    );

    private LeaveRequestStatusProjection() {
    }

    /**
     * @return the status after applying every event in order, or null for an empty stream
     */
    public static RequestStatus project(List<String> eventTypesInOrder) {
        RequestStatus status = null;
        for (String eventType : eventTypesInOrder) {
            RequestStatus next = STATUS_AFTER_EVENT.get(eventType);
            if (next == null) {
                throw new IllegalStateException("Unknown event type in leave request stream: " + eventType);
            }
            status = next;
        }
        return status;
    }
}