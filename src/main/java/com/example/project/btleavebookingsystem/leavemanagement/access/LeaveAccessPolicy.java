package com.example.project.btleavebookingsystem.leavemanagement.access;

import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LeaveAccessPolicy {

    private final StaffDirectory staffDirectory;

    public LeaveAccessPolicy(StaffDirectory staffDirectory) {
        this.staffDirectory = staffDirectory;
    }

    public void assertCanCancel(ActingUser actor, UUID requestOwnerId) {
        if (actor.admin() || actor.staffId().equals(requestOwnerId)) {
            return;
        }
        throw new AccessDeniedException("Only the requester or an administrator can cancel this leave request");
    }

    public void assertCanReview(ActingUser actor, UUID requestOwnerId) {
        rejectSelfApproval(actor, requestOwnerId);
        if (actor.admin() || isLineManagerOf(actor, requestOwnerId)) {
            return;
        }
        throw new AccessDeniedException(
                "Only the requester's line manager or an administrator can review this leave request");
    }

    public void assertCanResolveHR(ActingUser actor, UUID requestOwnerId) {
        rejectSelfApproval(actor, requestOwnerId);
        if (actor.admin()) {
            return;
        }
        throw new AccessDeniedException("Only HR (administrators) can resolve an escalated leave request");
    }

    public void assertCanView(ActingUser actor, UUID subjectStaffId) {
        if (actor.admin() || actor.staffId().equals(subjectStaffId) || isLineManagerOf(actor, subjectStaffId)) {
            return;
        }
        throw new AccessDeniedException("You may only view leave data for yourself or your direct reports");
    }

    private boolean isLineManagerOf(ActingUser actor, UUID staffId) {
        return staffDirectory.findLineManagerId(staffId)
                .map(actor.staffId()::equals)
                .orElse(false);
    }

    private void rejectSelfApproval(ActingUser actor, UUID requestOwnerId) {
        if (actor.staffId().equals(requestOwnerId)) {
            throw new AccessDeniedException("Staff cannot review or approve their own leave request");
        }
    }
}