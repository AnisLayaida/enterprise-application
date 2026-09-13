package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetOutstandingRequestsService {

    private final LeaveRequestRepository leaveRequestRepository;

    public GetOutstandingRequestsService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequestResponseDto> getOutstanding(UUID staffId) {
        List<RequestStatus> outstanding = List.of(
                RequestStatus.PENDING, RequestStatus.MANAGER_REVIEWED, RequestStatus.HR_REVIEW);

        return leaveRequestRepository.findByStatusIn(outstanding).stream()
                .filter(r -> staffId == null || r.getStaffId().equals(staffId))
                .map(LeaveRequestResponseDto::from)
                .toList();
    }
}