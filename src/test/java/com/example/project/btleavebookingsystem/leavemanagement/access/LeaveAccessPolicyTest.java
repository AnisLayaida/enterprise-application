package com.example.project.btleavebookingsystem.leavemanagement.access;

import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaveAccessPolicyTest {

    private final UUID janeId = UUID.randomUUID();
    private final UUID sarahId = UUID.randomUUID();      // Jane's line manager
    private final UUID otherManagerId = UUID.randomUUID();
    private final UUID otherStaffId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    private final ActingUser jane = new ActingUser(janeId, false);
    private final ActingUser sarah = new ActingUser(sarahId, false);
    private final ActingUser otherManager = new ActingUser(otherManagerId, false);
    private final ActingUser otherStaff = new ActingUser(otherStaffId, false);
    private final ActingUser admin = new ActingUser(adminId, true);

    private LeaveAccessPolicy policy;

    @BeforeEach
    void setUp() {
        StaffDirectory staffDirectory = mock(StaffDirectory.class);
        when(staffDirectory.findLineManagerId(janeId)).thenReturn(Optional.of(sarahId));
        when(staffDirectory.findLineManagerId(adminId)).thenReturn(Optional.empty());
        policy = new LeaveAccessPolicy(staffDirectory);
    }

    @Test
    void ownerCanCancelTheirOwnRequest() {
        assertThatCode(() -> policy.assertCanCancel(jane, janeId)).doesNotThrowAnyException();
    }

    @Test
    void adminCanCancelAnyonesRequest() {
        assertThatCode(() -> policy.assertCanCancel(admin, janeId)).doesNotThrowAnyException();
    }

    @Test
    void anotherStaffMemberCannotCancelSomeoneElsesRequest() {
        assertThatThrownBy(() -> policy.assertCanCancel(otherStaff, janeId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void lineManagerCanReviewTheirDirectReportsRequest() {
        assertThatCode(() -> policy.assertCanReview(sarah, janeId)).doesNotThrowAnyException();
    }

    @Test
    void managerOutsideTheReportingLineCannotReview() {
        assertThatThrownBy(() -> policy.assertCanReview(otherManager, janeId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanReviewAnotherStaffMembersRequest() {
        assertThatCode(() -> policy.assertCanReview(admin, janeId)).doesNotThrowAnyException();
    }

    @Test
    void nobodyCanReviewTheirOwnRequestNotEvenAnAdmin() {
        assertThatThrownBy(() -> policy.assertCanReview(admin, adminId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own leave request");
    }

    @Test
    void adminCannotResolveHrReviewOfTheirOwnRequest() {
        assertThatThrownBy(() -> policy.assertCanResolveHR(admin, adminId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void staffMemberCanViewTheirOwnLeaveData() {
        assertThatCode(() -> policy.assertCanView(jane, janeId)).doesNotThrowAnyException();
    }

    @Test
    void lineManagerCanViewTheirDirectReportsLeaveData() {
        assertThatCode(() -> policy.assertCanView(sarah, janeId)).doesNotThrowAnyException();
    }

    @Test
    void unrelatedManagerCannotViewAnotherTeamsLeaveData() {
        assertThatThrownBy(() -> policy.assertCanView(otherManager, janeId))
                .isInstanceOf(AccessDeniedException.class);
    }
}