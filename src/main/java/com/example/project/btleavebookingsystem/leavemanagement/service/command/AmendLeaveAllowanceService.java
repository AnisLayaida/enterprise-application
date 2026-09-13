package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

@Service
public class AmendLeaveAllowanceService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    public AmendLeaveAllowanceService(LeaveAllowanceRepository leaveAllowanceRepository) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
    }

    @Transactional
    public LeaveAllowanceResponseDto amend(UUID staffId, int newEntitledDays) {
        LeaveAllowance allowance = leaveAllowanceRepository
                .findByStaffIdAndBusinessYear(staffId, Year.now().getValue())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No leave allowance found for staff " + staffId));

        allowance.amendEntitlement(newEntitledDays);
        leaveAllowanceRepository.save(allowance);
        return LeaveAllowanceResponseDto.from(allowance);
    }
}