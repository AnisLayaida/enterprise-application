package com.example.project.btleavebookingsystem.leavemanagement.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "leave_allowances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"staff_id", "business_year"}))
public class LeaveAllowance {

    @Id
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "business_year", nullable = false)
    private int businessYear;

    @Column(name = "entitled_days", nullable = false)
    private int entitledDays;

    @Column(name = "remaining_days", nullable = false)
    private int remainingDays;

    protected LeaveAllowance() {
    }

    public LeaveAllowance(UUID staffId, int businessYear, int entitledDays) {
        this.id = UUID.randomUUID();
        this.staffId = staffId;
        this.businessYear = businessYear;
        this.entitledDays = entitledDays;
        this.remainingDays = entitledDays;
    }

    public void deduct(long days) {
        if (days > remainingDays) {
            throw new IllegalStateException("Cannot deduct more days than remain");
        }
        remainingDays -= days;
    }

    public void release(long days) {
        remainingDays = (int) Math.min(entitledDays, remainingDays + days);
    }

    public void amendEntitlement(int newEntitledDays) {
        int used = entitledDays - remainingDays;
        this.entitledDays = newEntitledDays;
        this.remainingDays = Math.max(0, newEntitledDays - used);
    }

    public UUID getId() { return id; }
    public UUID getStaffId() { return staffId; }
    public int getBusinessYear() { return businessYear; }
    public int getEntitledDays() { return entitledDays; }
    public int getRemainingDays() { return remainingDays; }
}