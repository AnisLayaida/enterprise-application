package com.example.project.btleavebookingsystem.staffmanagement.listener;

import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberAddedEvent;
import com.example.project.btleavebookingsystem.staffmanagement.event.StaffMemberUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StaffDirectoryEventListener {

    @EventListener
    public void onStaffAdded(StaffMemberAddedEvent event) {
    }

    @EventListener
    public void onStaffUpdated(StaffMemberUpdatedEvent event) {
    }
}