package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LeaveAllowanceEventListenerTest {

    private static final int NEXT_YEAR = 2027;

    private final UUID staffId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    private LeaveAllowanceRepository repository;
    private LeaveAllowanceEventListener listener;

    @BeforeEach
    void setUp() {
        repository = mock(LeaveAllowanceRepository.class);
        listener = new LeaveAllowanceEventListener(repository);
    }

    @Test
    void approvedAnnualLeaveDeductsFromTheLeavesOwnBusinessYear() {
        LeaveAllowance allowance2027 = new LeaveAllowance(staffId, NEXT_YEAR, 25);
        when(repository.findByStaffIdAndBusinessYear(staffId, NEXT_YEAR)).thenReturn(Optional.of(allowance2027));

        listener.onApproved(new LeaveRequestApprovedEvent(requestId, staffId, LeaveType.ANNUAL, NEXT_YEAR, 5));

        verify(repository).findByStaffIdAndBusinessYear(staffId, NEXT_YEAR);
        verify(repository).save(allowance2027);
        assertThat(allowance2027.getRemainingDays()).isEqualTo(20);
    }

    @ParameterizedTest
    @EnumSource(value = LeaveType.class, names = "ANNUAL", mode = EnumSource.Mode.EXCLUDE)
    void approvedNonAnnualLeaveNeverTouchesTheAllowance(LeaveType nonAnnualType) {
        listener.onApproved(new LeaveRequestApprovedEvent(requestId, staffId, nonAnnualType, NEXT_YEAR, 5));

        verifyNoInteractions(repository);
    }

    @Test
    void cancellingApprovedAnnualLeaveReleasesDaysToTheLeavesOwnBusinessYear() {
        LeaveAllowance allowance2027 = new LeaveAllowance(staffId, NEXT_YEAR, 25);
        allowance2027.deduct(5);
        when(repository.findByStaffIdAndBusinessYear(staffId, NEXT_YEAR)).thenReturn(Optional.of(allowance2027));

        listener.onCancelled(new LeaveRequestCancelledEvent(requestId, staffId, true, LeaveType.ANNUAL, NEXT_YEAR, 5));

        verify(repository).findByStaffIdAndBusinessYear(staffId, NEXT_YEAR);
        verify(repository).save(allowance2027);
        assertThat(allowance2027.getRemainingDays()).isEqualTo(25);
    }

    @ParameterizedTest
    @EnumSource(value = LeaveType.class, names = "ANNUAL", mode = EnumSource.Mode.EXCLUDE)
    void cancellingApprovedNonAnnualLeaveNeverTouchesTheAllowance(LeaveType nonAnnualType) {
        listener.onCancelled(new LeaveRequestCancelledEvent(requestId, staffId, true, nonAnnualType, NEXT_YEAR, 5));

        verifyNoInteractions(repository);
    }

    @Test
    void cancellingPendingAnnualLeaveReleasesNothing() {
        listener.onCancelled(new LeaveRequestCancelledEvent(requestId, staffId, false, LeaveType.ANNUAL, NEXT_YEAR, 5));

        verifyNoInteractions(repository);
    }
}