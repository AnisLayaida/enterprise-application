package com.example.project.btleavebookingsystem.staffmanagement.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffDirectory {

    Optional<UUID> findLineManagerId(UUID staffId);

    List<UUID> findDirectReportIds(UUID managerId);

    boolean exists(UUID staffId);
}