package com.sset.erp.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Student in the ERP system.
 * Inherits common attributes and behaviors from User.
 */
public class StudentUser extends User {
    private static final long serialVersionUID = 1L;

    private String rollNumber;
    private String department;
    private int semester;
    private String batch;

    public StudentUser(String id, String username, String rawPassword, String fullName, 
                       String email, String rollNumber, String department, int semester, String batch) {
        super(id, username, rawPassword, fullName, email, Role.STUDENT);
        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
        this.batch = batch;
    }

    public StudentUser(String id, String username, String passwordHash, String salt, 
                       String fullName, String email, boolean active, Date createdAt,
                       String rollNumber, String department, int semester, String batch) {
        super(id, username, passwordHash, salt, fullName, email, Role.STUDENT, active, createdAt);
        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
        this.batch = batch;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    @Override
    public String getRoleBadge() {
        return "STUDENT: " + rollNumber + " (S" + semester + " " + department + ")";
    }

    @Override
    public Map<String, String> getProfileDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Full Name", getFullName());
        details.put("Username", getUsername());
        details.put("Email", getEmail());
        details.put("Role", getRole().getDisplayName());
        details.put("Roll Number", rollNumber);
        details.put("Department", department);
        details.put("Semester", "Semester " + semester);
        details.put("Batch", batch);
        details.put("Account Status", isActive() ? "Active" : "Disabled");
        details.put("Enrolled On", getFormattedCreatedAt());
        return details;
    }
}
