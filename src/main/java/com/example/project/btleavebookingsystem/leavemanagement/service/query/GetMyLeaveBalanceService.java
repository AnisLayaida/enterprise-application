package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.UUID;

@Service
public class GetMyLeaveBalanceService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final LeaveAccessPolicy accessPolicy;

    public GetMyLeaveBalanceService(LeaveAllowanceRepository leaveAllowanceRepository,
                                    LeaveAccessPolicy accessPolicy) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
        this.accessPolicy = accessPolicy;
    }

    /** The caller's own balance (identity comes from the verified token, so no further check). */
    public LeaveAllowanceResponseDto getForStaff(UUID staffId) {
        int currentYear = Year.now().getValue();
        return leaveAllowanceRepository.findByStaffIdAndBusinessYear(staffId, currentYear)
                .map(LeaveAllowanceResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No leave allowance found for staff " + staffId + " in " + currentYear));
    }

    /** Another staff member's balance, visible only to their line manager or an administrator. */
    public LeaveAllowanceResponseDto getForStaff(UUID staffId, ActingUser viewer) {
        accessPolicy.assertCanView(viewer, staffId);
        return getForStaff(staffId);
    }
}