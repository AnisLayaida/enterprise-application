package com.example.project.btleavebookingsystem.identityaccess.service;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import com.example.project.btleavebookingsystem.identityaccess.domain.User;
import com.example.project.btleavebookingsystem.identityaccess.dto.UserRoleResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.repository.RoleRepository;
import com.example.project.btleavebookingsystem.identityaccess.repository.UserRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChangeUserRoleServiceTest {

    private final UUID adminStaffId = UUID.randomUUID();
    private final UUID janeStaffId = UUID.randomUUID();

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private ChangeUserRoleService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        service = new ChangeUserRoleService(userRepository, roleRepository);
    }

    @Test
    void adminCanPromoteAStaffMemberToManager() {
        User jane = new User(janeStaffId, "jane", "hash", new Role(Role.RoleName.STAFF));
        when(userRepository.findByStaffId(janeStaffId)).thenReturn(Optional.of(jane));
        when(roleRepository.findByName(Role.RoleName.MANAGER)).thenReturn(Optional.of(new Role(Role.RoleName.MANAGER)));

        UserRoleResponseDto response = service.changeRole(janeStaffId, Role.RoleName.MANAGER, adminStaffId);

        assertThat(response.role()).isEqualTo("MANAGER");
        assertThat(jane.getRole().getName()).isEqualTo(Role.RoleName.MANAGER);
        verify(userRepository).save(jane);
    }

    @Test
    void adminCannotChangeTheirOwnRoleAndNothingIsLoadedOrSaved() {
        assertThatThrownBy(() -> service.changeRole(adminStaffId, Role.RoleName.STAFF, adminStaffId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own role");

        verifyNoInteractions(userRepository, roleRepository);
    }

    @Test
    void changingTheRoleOfAnUnknownStaffMemberThrowsNotFound() {
        when(userRepository.findByStaffId(janeStaffId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeRole(janeStaffId, Role.RoleName.MANAGER, adminStaffId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userEntityRejectsANullRole() {
        User jane = new User(janeStaffId, "jane", "hash", new Role(Role.RoleName.STAFF));

        assertThatThrownBy(() -> jane.changeRole(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}