package com.example.project.btleavebookingsystem.leavemanagement.access;

import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Object-level authorisation for Leave Management (OWASP API1:2023 Broken Object Level Authorisation).
 * Role checks (@PreAuthorize) decide WHAT kind of user may call an endpoint; this policy decides
 * WHOSE leave they may act on, based on ownership and the line-management relationship.
 * Denials raise AccessDeniedException, which the global handler logs and maps to 403.
 */
@Component
public class LeaveAccessPolicy {

    private final StaffDirectory staffDirectory;

    public LeaveAccessPolicy(StaffDirectory staffDirectory) {
        this.staffDirectory = staffDirectory;
    }

    /** Owners may withdraw their own request; administrators may cancel on anyone's behalf. */
    public void assertCanCancel(ActingUser actor, UUID requestOwnerId) {
        if (actor.admin() || actor.staffId().equals(requestOwnerId)) {
            return;
        }
        throw new AccessDeniedException("Only the requester or an administrator can cancel this leave request");
    }

    /** The requester's line manager, or an administrator, may review - but never the requester. */
    public void assertCanReview(ActingUser actor, UUID requestOwnerId) {
        rejectSelfApproval(actor, requestOwnerId);
        if (actor.admin() || isLineManagerOf(actor, requestOwnerId)) {
            return;
        }
        throw new AccessDeniedException(
                "Only the requester's line manager or an administrator can review this leave request");
    }

    /** HR resolution is an administrator responsibility, and never for the resolver's own request. */
    public void assertCanResolveHR(ActingUser actor, UUID requestOwnerId) {
        rejectSelfApproval(actor, requestOwnerId);
        if (actor.admin()) {
            return;
        }
        throw new AccessDeniedException("Only HR (administrators) can resolve an escalated leave request");
    }

    /** Leave data is visible to the staff member, their line manager and administrators. */
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