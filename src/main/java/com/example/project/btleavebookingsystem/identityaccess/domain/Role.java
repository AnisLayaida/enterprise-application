package com.example.project.btleavebookingsystem.identityaccess.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    protected Role() {
    }

    public Role(RoleName name) {
        this.name = name;
    }

    public UUID getId() { return id; }
    public RoleName getName() { return name; }

    public enum RoleName {
        STAFF, MANAGER, ADMIN
    }
}