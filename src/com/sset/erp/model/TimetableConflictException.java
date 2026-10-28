package com.sset.erp.model;

/**
 * Exception thrown when a scheduling conflict occurs in the timetable
 * (e.g., faculty double-booked, room double-booked, or class overlapping).
 */
public class TimetableConflictException extends Exception {
    private final String conflictType;

    public TimetableConflictException(String conflictType, String message) {
        super(message);
        this.conflictType = conflictType;
    }

    public String getConflictType() {
        return conflictType;
    }
}
