package com.example.project.btleavebookingsystem.leavemanagement.repository;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveAllowanceRepository extends JpaRepository<LeaveAllowance, UUID> {

    Optional<LeaveAllowance> findByStaffIdAndBusinessYear(UUID staffId, int businessYear);

    List<LeaveAllowance> findByBusinessYear(int businessYear);
}