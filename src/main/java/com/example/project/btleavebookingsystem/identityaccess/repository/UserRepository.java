package com.example.project.btleavebookingsystem.identityaccess.repository;

import com.example.project.btleavebookingsystem.identityaccess.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByStaffId(UUID staffId);
}