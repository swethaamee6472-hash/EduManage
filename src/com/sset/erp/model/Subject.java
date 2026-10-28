package com.sset.erp.model;

import java.util.Objects;

/**
 * Domain entity representing an academic subject or course offering.
 */
public class Subject {
    private final String code;
    private final String name;
    private final String department;
    private final int semester;
    private final int credits;
    private final String type; // e.g. "Theory", "Lab", "Elective"

    public Subject(String code, String name, String department, int semester, int credits, String type) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code is required.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (semester < 1 || semester > 8) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }
        if (credits < 1) {
            throw new IllegalArgumentException("Credits must be a positive integer.");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.department = department.trim();
        this.semester = semester;
        this.credits = credits;
        this.type = (type == null || type.trim().isEmpty()) ? "Theory" : type.trim();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getSemester() {
        return semester;
    }

    public int getCredits() {
        return credits;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return code + " - " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return code.equalsIgnoreCase(subject.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.toUpperCase());
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + credits + " Cr, Sem " + semester + ")";
    }
}
