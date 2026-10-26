package com.sset.erp.model;

/**
 * Categories of student-facing updates.
 */
public enum NotificationType {
    ATTENDANCE("Attendance"),
    EXAM("Exam"),
    EVENT("Event"),
    FEE("Fee"),
    ANNOUNCEMENT("Announcement");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
