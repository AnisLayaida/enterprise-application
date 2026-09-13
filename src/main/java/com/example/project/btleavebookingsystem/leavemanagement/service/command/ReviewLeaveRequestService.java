package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewLeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewLeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LeaveRequestResponseDto review(UUID leaveRequestId, boolean approved) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));

        request.reviewByManager(approved);
        leaveRequestRepository.save(request);
        request.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return LeaveRequestResponseDto.from(request);
    }
}