package com.example.project.btleavebookingsystem.staffmanagement.repository;

import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

    List<StaffMember> findByLineManagerId(UUID lineManagerId);
}