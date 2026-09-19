package com.example.project.btleavebookingsystem.staffmanagement.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Open host service published by the Staff Management context.
 * Other contexts query organisational relationships through this interface rather than
 * reaching into Staff Management's repository, keeping the context boundary explicit.
 */
public interface StaffDirectory {

    /** The staff member's line manager, or empty if they have none or do not exist. */
    Optional<UUID> findLineManagerId(UUID staffId);

    /** Ids of all staff whose line manager is the given manager. */
    List<UUID> findDirectReportIds(UUID managerId);

    boolean exists(UUID staffId);
}