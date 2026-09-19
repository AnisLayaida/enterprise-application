package com.example.project.btleavebookingsystem.staffmanagement.api;

import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import com.example.project.btleavebookingsystem.staffmanagement.repository.StaffMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StaffDirectoryService implements StaffDirectory {

    private final StaffMemberRepository staffMemberRepository;

    public StaffDirectoryService(StaffMemberRepository staffMemberRepository) {
        this.staffMemberRepository = staffMemberRepository;
    }

    @Override
    public Optional<UUID> findLineManagerId(UUID staffId) {
        return staffMemberRepository.findById(staffId).map(StaffMember::getLineManagerId);
    }

    @Override
    public List<UUID> findDirectReportIds(UUID managerId) {
        return staffMemberRepository.findByLineManagerId(managerId).stream()
                .map(StaffMember::getId)
                .toList();
    }

    @Override
    public boolean exists(UUID staffId) {
        return staffMemberRepository.existsById(staffId);
    }
}