package com.example.project.btleavebookingsystem.staffmanagement.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staff_members")
public class StaffMember {

    @Id
    private UUID id;

    private String firstName;
    private String surname;
    private String email;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    private String department;

    @Column(name = "line_manager_id")
    private UUID lineManagerId;

    @Column(name = "job_level")
    private String jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus;

    protected StaffMember() {
    }

    public StaffMember(UUID id, String firstName, String surname, String email,
                       LocalDate hireDate, String department, UUID lineManagerId,
                       String jobLevel, EmploymentStatus employmentStatus) {
        this.id = id;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        this.hireDate = hireDate;
        this.department = department;
        this.lineManagerId = lineManagerId;
        this.jobLevel = jobLevel;
        this.employmentStatus = employmentStatus;
    }

    public void applyUpdate(String department, String jobLevel, EmploymentStatus employmentStatus) {
        this.department = department;
        this.jobLevel = jobLevel;
        this.employmentStatus = employmentStatus;
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public LocalDate getHireDate() { return hireDate; }
    public String getDepartment() { return department; }
    public UUID getLineManagerId() { return lineManagerId; }
    public String getJobLevel() { return jobLevel; }
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }

    public enum EmploymentStatus {
        ACTIVE, ON_LEAVE, TERMINATED
    }
}