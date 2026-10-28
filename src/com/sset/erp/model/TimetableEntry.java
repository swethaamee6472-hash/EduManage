package com.sset.erp.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a scheduled lecture or lab slot in the academic timetable.
 */
public class TimetableEntry {
    public static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    public static final int PERIOD_COUNT = 6;

    private final String id;
    private final String dayOfWeek;
    private final int periodNumber;
    private final String timeSlot;
    private final String department;
    private final int semester;
    private final String batch;
    private final String subjectCode;
    private final String subjectName;
    private final String facultyUsername;
    private final String facultyName;
    private final String roomNumber;
    private final LocalDateTime updatedAt;

    public TimetableEntry(String id, String dayOfWeek, int periodNumber, String timeSlot,
                          String department, int semester, String batch, String subjectCode,
                          String subjectName, String facultyUsername, String facultyName,
                          String roomNumber, LocalDateTime updatedAt) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Timetable entry ID is required.");
        }
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("Day of week is required.");
        }
        if (periodNumber < 1 || periodNumber > PERIOD_COUNT) {
            throw new IllegalArgumentException("Period number must be between 1 and " + PERIOD_COUNT + ".");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (semester < 1 || semester > 8) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }
        if (facultyUsername == null || facultyUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty username is required.");
        }
        if (facultyName == null || facultyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty name is required.");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }

        this.id = id.trim();
        this.dayOfWeek = normalizeDay(dayOfWeek);
        this.periodNumber = periodNumber;
        this.timeSlot = (timeSlot == null || timeSlot.trim().isEmpty())
                ? getTimeSlotForPeriod(periodNumber) : timeSlot.trim();
        this.department = department.trim();
        this.semester = semester;
        this.batch = (batch == null || batch.trim().isEmpty()) ? "All Batches" : batch.trim();
        this.subjectCode = subjectCode.trim().toUpperCase();
        this.subjectName = subjectName.trim();
        this.facultyUsername = facultyUsername.trim();
        this.facultyName = facultyName.trim();
        this.roomNumber = roomNumber.trim();
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static TimetableEntry create(String dayOfWeek, int periodNumber, String department,
                                        int semester, String batch, String subjectCode,
                                        String subjectName, String facultyUsername,
                                        String facultyName, String roomNumber) {
        return new TimetableEntry(UUID.randomUUID().toString(), dayOfWeek, periodNumber,
                getTimeSlotForPeriod(periodNumber), department, semester, batch, subjectCode,
                subjectName, facultyUsername, facultyName, roomNumber, LocalDateTime.now());
    }

    public static String getTimeSlotForPeriod(int period) {
        return switch (period) {
            case 1 -> "09:00 - 10:00 AM";
            case 2 -> "10:00 - 11:00 AM";
            case 3 -> "11:15 - 12:15 PM";
            case 4 -> "12:15 - 01:15 PM";
            case 5 -> "02:00 - 03:00 PM";
            case 6 -> "03:00 - 04:00 PM";
            default -> "Period " + period;
        };
    }

    public static String normalizeDay(String day) {
        if (day == null) return "Monday";
        String lower = day.trim().toLowerCase();
        for (String d : DAYS_OF_WEEK) {
            if (d.toLowerCase().equals(lower)) {
                return d;
            }
        }
        return day.trim();
    }

    public String getId() {
        return id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getDepartment() {
        return department;
    }

    public int getSemester() {
        return semester;
    }

    public String getBatch() {
        return batch;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimetableEntry that = (TimetableEntry) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return dayOfWeek + " P" + periodNumber + " (" + timeSlot + "): " + subjectCode + " [" + roomNumber + "] (" + facultyName + ")";
    }
}
