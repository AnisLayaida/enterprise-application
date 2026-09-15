package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetLeaveRequestByIdService {

    private final LeaveRequestRepository leaveRequestRepository;

    public GetLeaveRequestByIdService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public LeaveRequestResponseDto getById(UUID id) {
        return leaveRequestRepository.findById(id)
                .map(LeaveRequestResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }
}