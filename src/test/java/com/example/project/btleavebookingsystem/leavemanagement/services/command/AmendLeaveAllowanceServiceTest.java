package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendLeaveAllowanceServiceTest {

    @Mock
    private LeaveAllowanceRepository leaveAllowanceRepository;

    private AmendLeaveAllowanceService service;

    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AmendLeaveAllowanceService(leaveAllowanceRepository);
    }

    @Test
    void amendThrowsResourceNotFoundWhenNoAllowanceExistsForCurrentYear() {
        when(leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, Year.now().getValue()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.amend(staffId, 30))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void amendIncreasesEntitlementWhilePreservingDaysAlreadyUsed() {
        LeaveAllowance allowance = new LeaveAllowance(staffId, Year.now().getValue(), 25);
        allowance.deduct(10); // 10 used, 15 remaining
        when(leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, Year.now().getValue()))
                .thenReturn(Optional.of(allowance));

        LeaveAllowanceResponseDto response = service.amend(staffId, 30);

        assertThat(response.entitledDays()).isEqualTo(30);
        assertThat(response.remainingDays()).isEqualTo(20); // 30 - 10 used
    }
}