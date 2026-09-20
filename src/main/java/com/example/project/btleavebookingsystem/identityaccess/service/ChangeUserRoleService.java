package com.example.project.btleavebookingsystem.identityaccess.service;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import com.example.project.btleavebookingsystem.identityaccess.domain.User;
import com.example.project.btleavebookingsystem.identityaccess.dto.UserRoleResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.repository.RoleRepository;
import com.example.project.btleavebookingsystem.identityaccess.repository.UserRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangeUserRoleService {

    private static final Logger log = LoggerFactory.getLogger(ChangeUserRoleService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public ChangeUserRoleService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public UserRoleResponseDto changeRole(UUID targetStaffId, Role.RoleName newRoleName, UUID actingStaffId) {
        if (targetStaffId.equals(actingStaffId)) {
            throw new AccessDeniedException("Administrators cannot change their own role");
        }

        User user = userRepository.findByStaffId(targetStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("No user account found for staff " + targetStaffId));

        Role newRole = roleRepository.findByName(newRoleName)
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + newRoleName));

        Role.RoleName previousRole = user.getRole().getName();
        user.changeRole(newRole);
        userRepository.save(user);

        log.info("AUDIT role change: staff {} ({}) changed from {} to {} by staff {}",
                targetStaffId, user.getUsername(), previousRole, newRoleName, actingStaffId);

        return UserRoleResponseDto.from(user);
    }
}