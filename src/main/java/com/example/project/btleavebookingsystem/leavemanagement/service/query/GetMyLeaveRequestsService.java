package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetMyLeaveRequestsService {

    private final LeaveRequestRepository leaveRequestRepository;

    public GetMyLeaveRequestsService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequestResponseDto> getForStaff(UUID staffId) {
        return leaveRequestRepository.findByStaffId(staffId).stream()
                .map(LeaveRequestResponseDto::from)
                .toList();
    }
}