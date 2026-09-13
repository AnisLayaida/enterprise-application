package com.example.project.btleavebookingsystem.leavemanagement.repository;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveRequest;
import com.example.project.btleavebookingsystem.leavemanagement.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByStaffId(UUID staffId);

    List<LeaveRequest> findByStatusIn(List<RequestStatus> statuses);

    List<LeaveRequest> findByStaffIdInAndDateRange_StartDateBetween(
            List<UUID> staffIds, LocalDate from, LocalDate to);
}