package com.example.project.btleavebookingsystem.leavemanagement.service.query;

import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import com.example.project.btleavebookingsystem.staffmanagement.repository.StaffMemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GetTeamPendingRequestsService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffMemberRepository staffMemberRepository;

    public GetTeamPendingRequestsService(LeaveRequestRepository leaveRequestRepository,
                                         StaffMemberRepository staffMemberRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.staffMemberRepository = staffMemberRepository;
    }

    public List<LeaveRequestResponseDto> getForManager(UUID managerId, LocalDate from, LocalDate to) {
        List<UUID> teamStaffIds = staffMemberRepository.findByLineManagerId(managerId).stream()
                .map(s -> s.getId())
                .toList();

        List<RequestStatus> pendingStatuses = List.of(
                RequestStatus.PENDING, RequestStatus.MANAGER_REVIEWED, RequestStatus.HR_REVIEW);

        return leaveRequestRepository.findByStatusIn(pendingStatuses).stream()
                .filter(r -> teamStaffIds.contains(r.getStaffId()))
                .filter(r -> from == null || !r.getDateRange().getStartDate().isBefore(from))
                .filter(r -> to == null || !r.getDateRange().getStartDate().isAfter(to))
                .map(LeaveRequestResponseDto::from)
                .toList();
    }
}