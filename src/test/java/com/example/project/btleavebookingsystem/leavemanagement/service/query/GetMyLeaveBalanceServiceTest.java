package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyLeaveBalanceServiceTest {

    @Mock
    private LeaveAllowanceRepository leaveAllowanceRepository;

    @Mock
    private LeaveAccessPolicy accessPolicy;

    private GetMyLeaveBalanceService service;

    private final UUID staffId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetMyLeaveBalanceService(leaveAllowanceRepository, accessPolicy);
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

    @Test
    void deniedViewerNeverReadsAnotherStaffMembersAllowance() {
        ActingUser unrelatedManager = new ActingUser(UUID.randomUUID(), false);
        doThrow(new AccessDeniedException("denied")).when(accessPolicy).assertCanView(unrelatedManager, staffId);

        assertThatThrownBy(() -> service.getForStaff(staffId, unrelatedManager))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(leaveAllowanceRepository);
    }
}