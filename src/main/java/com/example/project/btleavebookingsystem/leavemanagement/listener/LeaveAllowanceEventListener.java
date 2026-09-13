package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Component
public class LeaveAllowanceEventListener {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    public LeaveAllowanceEventListener(LeaveAllowanceRepository leaveAllowanceRepository) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
    }

    @EventListener
    @Transactional
    public void onApproved(LeaveRequestApprovedEvent event) {
        leaveAllowanceRepository.findByStaffIdAndBusinessYear(event.staffId(), Year.now().getValue())
                .ifPresent(allowance -> {
                    allowance.deduct(event.numberOfDays());
                    leaveAllowanceRepository.save(allowance);
                });
    }

    @EventListener
    @Transactional
    public void onCancelled(LeaveRequestCancelledEvent event) {
        if (!event.wasApproved()) {
            return;
        }
        leaveAllowanceRepository.findByStaffIdAndBusinessYear(event.staffId(), Year.now().getValue())
                .ifPresent(allowance -> {
                    allowance.release(event.numberOfDays());
                    leaveAllowanceRepository.save(allowance);
                });
    }
}