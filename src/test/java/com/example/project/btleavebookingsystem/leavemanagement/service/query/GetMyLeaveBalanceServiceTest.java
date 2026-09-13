package com.example.project.btleavebookingsystem.leavemanagement.service.query;

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
class GetMyLeaveBalanceServiceTest {

    @Mock
    private LeaveAllowanceRepository leaveAllowanceRepository;

    private GetMyLeaveBalanceService service;

    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetMyLeaveBalanceService(leaveAllowanceRepository);
    }

    @Test
    void throwsResourceNotFoundWhenNoAllowanceExistsForCurrentYear() {
        when(leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, Year.now().getValue()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForStaff(staffId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnsBalanceWhenAllowanceExists() {
        LeaveAllowance allowance = new LeaveAllowance(staffId, Year.now().getValue(), 25);
        when(leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, Year.now().getValue()))
                .thenReturn(Optional.of(allowance));

        LeaveAllowanceResponseDto response = service.getForStaff(staffId);

        assertThat(response.entitledDays()).isEqualTo(25);
        assertThat(response.remainingDays()).isEqualTo(25);
    }
}