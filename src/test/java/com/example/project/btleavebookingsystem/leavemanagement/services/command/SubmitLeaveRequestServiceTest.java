package com.example.project.btleavebookingsystem.leavemanagement.service.command;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.SubmitLeaveRequestDto;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmitLeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SubmitLeaveRequestService service;

    @BeforeEach
    void setUp() {
        service = new SubmitLeaveRequestService(leaveRequestRepository, eventPublisher);
    }

    @Test
    void submitSavesRequestAndPublishesSubmittedEvent() {
        UUID staffId = UUID.randomUUID();
        SubmitLeaveRequestDto dto = new SubmitLeaveRequestDto(
                LeaveType.ANNUAL, LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10),
                "Holiday", false);

        LeaveRequestResponseDto response = service.submit(staffId, dto);

        assertThat(response.staffId()).isEqualTo(staffId);
        assertThat(response.status().name()).isEqualTo("PENDING");
        verify(leaveRequestRepository).save(any());
        verify(eventPublisher).publishEvent(any(Object.class));
    }
}