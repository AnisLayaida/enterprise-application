package com.example.project.btleavebookingsystem.leavemanagement.listener;

import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveType;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestApprovedEvent;
import com.example.project.btleavebookingsystem.leavemanagement.event.LeaveRequestCancelledEvent;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LeaveAllowanceEventListener {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    public LeaveAllowanceEventListener(LeaveAllowanceRepository leaveAllowanceRepository) {
        this.leaveAllowanceRepository = leaveAllowanceRepository;
    }

    @EventListener
    @Transactional
    public void onApproved(LeaveRequestApprovedEvent event) {
        if (event.leaveType() != LeaveType.ANNUAL) {
            return;
        }
        leaveAllowanceRepository.findByStaffIdAndBusinessYear(event.staffId(), event.businessYear())
                .ifPresent(allowance -> {
                    allowance.deduct(event.numberOfDays());
                    leaveAllowanceRepository.save(allowance);
                });
    }

    @EventListener
    @Transactional
    public void onCancelled(LeaveRequestCancelledEvent event) {
        if (!event.wasApproved() || event.leaveType() != LeaveType.ANNUAL) {
            return;
        }
        leaveAllowanceRepository.findByStaffIdAndBusinessYear(event.staffId(), event.businessYear())
                .ifPresent(allowance -> {
                    allowance.release(event.numberOfDays());
                    leaveAllowanceRepository.save(allowance);
                });
    }
}