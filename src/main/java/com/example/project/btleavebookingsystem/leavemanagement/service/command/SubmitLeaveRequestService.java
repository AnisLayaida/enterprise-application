package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.DateRange;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.SubmitLeaveRequestDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.shared.exception.LeaveOverlapException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubmitLeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubmitLeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LeaveRequestResponseDto submit(UUID staffId, SubmitLeaveRequestDto dto) {
        DateRange dateRange = new DateRange(dto.startDate(), dto.endDate());

        boolean hasOverlap = leaveRequestRepository.findByStaffId(staffId).stream()
                .filter(existing -> existing.getStatus().isActive())
                .anyMatch(existing -> existing.getDateRange().overlaps(dateRange));

        if (hasOverlap) {
            throw new LeaveOverlapException(
                    "Requested dates overlap with an existing active leave request for this staff member");
        }

        LeaveRequest request = LeaveRequest.submit(
                staffId, dateRange, dto.leaveType(), dto.reason(), dto.requiresHRApproval());

        leaveRequestRepository.save(request);
        request.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return LeaveRequestResponseDto.from(request);
    }
}