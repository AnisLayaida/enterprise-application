package com.example.project.btleavebookingsystem.leavemanagement.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeaveAllowanceTest {

    @Test
    void newAllowanceStartsWithFullRemainingBalance() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        assertThat(allowance.getRemainingDays()).isEqualTo(25);
    }

    @Test
    void deductReducesRemainingDays() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        allowance.deduct(5);
        assertThat(allowance.getRemainingDays()).isEqualTo(20);
    }

    @Test
    void deductingMoreThanRemainingThrows() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        allowance.deduct(20);

        assertThatThrownBy(() -> allowance.deduct(10))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void releaseIncreasesRemainingDaysBackTowardsEntitlement() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        allowance.deduct(5);
        allowance.release(5);
        assertThat(allowance.getRemainingDays()).isEqualTo(25);
    }

    @Test
    void releaseNeverExceedsEntitledDays() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        allowance.release(100);
        assertThat(allowance.getRemainingDays()).isEqualTo(25);
    }

    @Test
    void amendEntitlementPreservesAlreadyUsedDays() {
        LeaveAllowance allowance = new LeaveAllowance(UUID.randomUUID(), 2026, 25);
        allowance.deduct(10);

        allowance.amendEntitlement(30);

        assertThat(allowance.getEntitledDays()).isEqualTo(30);
        assertThat(allowance.getRemainingDays()).isEqualTo(20);
    }
}