package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.dto.CreateLeaveAllowanceDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.LeaveAllowanceAlreadyExistsException;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateLeaveAllowanceService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final StaffDirectory staffDirectory;

    public CreateLeaveAllowanceService(LeaveAllowanceRepository leaveAllowanceRepository,
                                       StaffDirectory staffDirectory) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
        this.staffDirectory = staffDirectory;
    }

    @Transactional
    public LeaveAllowanceResponseDto create(CreateLeaveAllowanceDto dto) {
        if (!staffDirectory.exists(dto.staffId())) {
            throw new ResourceNotFoundException("Staff member not found: " + dto.staffId());
        }

        leaveAllowanceRepository.findByStaffIdAndBusinessYear(dto.staffId(), dto.businessYear())
                .ifPresent(existing -> {
                    throw new LeaveAllowanceAlreadyExistsException(
                            "A leave allowance already exists for staff " + dto.staffId()
                                    + " in business year " + dto.businessYear());
                });

        LeaveAllowance allowance = new LeaveAllowance(dto.staffId(), dto.businessYear(), dto.entitledDays());
        leaveAllowanceRepository.save(allowance);

        return LeaveAllowanceResponseDto.from(allowance);
    }
}