package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

@Service
public class GetMyLeaveBalanceService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    public GetMyLeaveBalanceService(LeaveAllowanceRepository leaveAllowanceRepository) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
    }

    public LeaveAllowanceResponseDto getForStaff(UUID staffId) {
        int currentYear = Year.now().getValue();
        return leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, currentYear)
                .map(LeaveAllowanceResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No leave allowance found for staff " + staffId + " in " + currentYear));
    }
}