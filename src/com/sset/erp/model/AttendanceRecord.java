package com.sset.erp.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A student's attendance at a class session.
 */
public class AttendanceRecord {
    private final String id;
    private final String rollNumber;
    private final String subject;
    private final String facultyName;
    private final LocalDateTime recordedAt;

    public AttendanceRecord(String id, String rollNumber, String subject, String facultyName,
                            LocalDateTime recordedAt) {
        this.id = requireText(id, "Record ID");
        this.rollNumber = requireText(rollNumber, "Student roll number");
        this.subject = requireText(subject, "Subject");
        this.facultyName = requireText(facultyName, "Faculty name");
        if (recordedAt == null) {
            throw new IllegalArgumentException("Attendance date is required.");
        }
        this.recordedAt = recordedAt;
    }

    public static AttendanceRecord create(String rollNumber, String subject, String facultyName) {
        return new AttendanceRecord(UUID.randomUUID().toString(), rollNumber, subject, facultyName,
                LocalDateTime.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getSubject() {
        return subject;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
}
