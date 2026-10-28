package com.sset.erp.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing the allocation of an academic subject to a faculty member.
 */
public class SubjectAllocation {
    private final String id;
    private final String subjectCode;
    private final String subjectName;
    private final String department;
    private final int semester;
    private final String batch;
    private final String facultyUsername;
    private final String facultyName;
    private final String academicYear;
    private final LocalDateTime allocatedAt;

    public SubjectAllocation(String id, String subjectCode, String subjectName, String department,
                             int semester, String batch, String facultyUsername, String facultyName,
                             String academicYear, LocalDateTime allocatedAt) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation ID is required.");
        }
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (semester < 1 || semester > 8) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }
        if (facultyUsername == null || facultyUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty username is required.");
        }
        if (facultyName == null || facultyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty name is required.");
        }
        this.id = id.trim();
        this.subjectCode = subjectCode.trim().toUpperCase();
        this.subjectName = subjectName.trim();
        this.department = department.trim();
        this.semester = semester;
        this.batch = (batch == null || batch.trim().isEmpty()) ? "All Batches" : batch.trim();
        this.facultyUsername = facultyUsername.trim();
        this.facultyName = facultyName.trim();
        this.academicYear = (academicYear == null || academicYear.trim().isEmpty()) ? "2026-2027" : academicYear.trim();
        this.allocatedAt = allocatedAt == null ? LocalDateTime.now() : allocatedAt;
    }

    public static SubjectAllocation create(String subjectCode, String subjectName, String department,
                                           int semester, String batch, String facultyUsername,
                                           String facultyName, String academicYear) {
        return new SubjectAllocation(UUID.randomUUID().toString(), subjectCode, subjectName, department,
                semester, batch, facultyUsername, facultyName, academicYear, LocalDateTime.now());
    }

    public String getId() {
        return id;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
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

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectAllocation that = (SubjectAllocation) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return subjectCode + " (" + subjectName + ") -> " + facultyName + " [Sem " + semester + ", " + batch + "]";
    }
}
