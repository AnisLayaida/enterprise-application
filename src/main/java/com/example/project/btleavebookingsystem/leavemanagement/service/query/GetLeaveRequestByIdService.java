package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetLeaveRequestByIdService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveAccessPolicy accessPolicy;

    public GetLeaveRequestByIdService(LeaveRequestRepository leaveRequestRepository,
                                      LeaveAccessPolicy accessPolicy) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.accessPolicy = accessPolicy;
    }

    public LeaveRequestResponseDto getById(UUID id, ActingUser viewer) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));

        accessPolicy.assertCanView(viewer, request.getStaffId());

        return LeaveRequestResponseDto.from(request);
    }
}