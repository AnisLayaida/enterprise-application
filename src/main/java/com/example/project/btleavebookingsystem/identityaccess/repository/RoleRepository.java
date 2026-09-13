package com.example.project.btleavebookingsystem.identityaccess.repository;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(Role.RoleName name);
}