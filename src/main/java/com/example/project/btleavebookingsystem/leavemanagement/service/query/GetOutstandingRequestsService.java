package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.staffmanagement.api.StaffDirectory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetOutstandingRequestsService {

    private static final List<RequestStatus> OUTSTANDING = List.of(
            RequestStatus.PENDING, RequestStatus.MANAGER_REVIEWED, RequestStatus.HR_REVIEW);

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffDirectory staffDirectory;

    public GetOutstandingRequestsService(LeaveRequestRepository leaveRequestRepository,
                                         StaffDirectory staffDirectory) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.staffDirectory = staffDirectory;
    }

    public List<LeaveRequestResponseDto> getOutstanding(UUID staffId, UUID managerId) {
        List<UUID> teamStaffIds = managerId == null ? null : staffDirectory.findDirectReportIds(managerId);

        return leaveRequestRepository.findByStatusIn(OUTSTANDING).stream()
                .filter(r -> staffId == null || r.getStaffId().equals(staffId))
                .filter(r -> teamStaffIds == null || teamStaffIds.contains(r.getStaffId()))
                .map(LeaveRequestResponseDto::from)
                .toList();
    }
}