package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.LeaveAccessPolicy;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResolveHRReviewService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LeaveAccessPolicy accessPolicy;

    public ResolveHRReviewService(LeaveRequestRepository leaveRequestRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  LeaveAccessPolicy accessPolicy) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.eventPublisher = eventPublisher;
        this.accessPolicy = accessPolicy;
    }

    @Transactional
    public LeaveRequestResponseDto resolve(UUID leaveRequestId, boolean approved, ActingUser actor) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));

        accessPolicy.assertCanResolveHR(actor, request.getStaffId());

        request.resolveHRReview(approved);
        leaveRequestRepository.save(request);
        request.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return LeaveRequestResponseDto.from(request);
    }
}