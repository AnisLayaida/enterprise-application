package com.example.project.btleavebookingsystem.leavemanagement.domain;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveManagementDomainEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestRejectedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestSubmittedEvent;
import com.example.project.btleavebookingsystem.shared.exception.InvalidStateTransitionException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Embedded
    private DateRange dateRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "requires_hr_approval", nullable = false)
    private boolean requiresHRApproval;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private final List<LeaveManagementDomainEvent> domainEvents = new ArrayList<>();

    protected LeaveRequest() {
    }

    private LeaveRequest(UUID staffId, DateRange dateRange, LeaveType leaveType,
                         String reason, boolean requiresHRApproval) {
        this.id = UUID.randomUUID();
        this.staffId = staffId;
        this.dateRange = dateRange;
        this.leaveType = leaveType;
        this.reason = reason;
        this.requiresHRApproval = requiresHRApproval;
        this.status = RequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static LeaveRequest submit(UUID staffId, DateRange dateRange, LeaveType leaveType,
                                      String reason, boolean requiresHRApproval) {
        LeaveRequest request = new LeaveRequest(staffId, dateRange, leaveType, reason, requiresHRApproval);
        request.domainEvents.add(new LeaveRequestSubmittedEvent(request.id, request.staffId));
        return request;
    }

    public void reviewByManager(boolean approved) {
        RequestStatus next = requiresHRApproval
                ? RequestStatus.MANAGER_REVIEWED
                : (approved ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        transitionTo(next);
        if (!requiresHRApproval) {
            raiseOutcomeEvent(approved);
        }
    }

    public void resolveHRReview(boolean approved) {
        transitionTo(approved ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        raiseOutcomeEvent(approved);
    }

    public void cancel() {
        transitionTo(RequestStatus.CANCELLED);
        domainEvents.add(new LeaveRequestCancelledEvent(id, staffId));
    }

    private void raiseOutcomeEvent(boolean approved) {
        if (approved) {
            domainEvents.add(new LeaveRequestApprovedEvent(id, staffId, dateRange.numberOfDays()));
        } else {
            domainEvents.add(new LeaveRequestRejectedEvent(id, staffId));
        }
    }

    private void transitionTo(RequestStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidStateTransitionException(
                    "Cannot move leave request from " + status + " to " + next);
        }
        this.status = next;
    }

    public List<LeaveManagementDomainEvent> pullDomainEvents() {
        List<LeaveManagementDomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public UUID getId() { return id; }
    public UUID getStaffId() { return staffId; }
    public DateRange getDateRange() { return dateRange; }
    public LeaveType getLeaveType() { return leaveType; }
    public String getReason() { return reason; }
    public RequestStatus getStatus() { return status; }
    public boolean isRequiresHRApproval() { return requiresHRApproval; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}