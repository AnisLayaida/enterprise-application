package com.example.project.btleavebookingsystem.shared.exception;

public class LeaveAllowanceAlreadyExistsException extends RuntimeException {
    public LeaveAllowanceAlreadyExistsException(String message) {
        super(message);
    }
}