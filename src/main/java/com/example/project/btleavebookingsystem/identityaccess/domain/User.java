package com.example.project.btleavebookingsystem.identityaccess.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(name = "staff_id", nullable = false, unique = true)
    private UUID staffId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected User() {
    }

    public User(UUID staffId, String username, String passwordHash, Role role) {
        this.id = UUID.randomUUID();
        this.staffId = staffId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UUID getId() { return id; }
    public UUID getStaffId() { return staffId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}