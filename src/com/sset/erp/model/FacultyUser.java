package com.sset.erp.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Faculty member in the ERP system.
 * Inherits common attributes and behaviors from User.
 */
public class FacultyUser extends User {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String department;
    private String designation;

    public FacultyUser(String id, String username, String rawPassword, String fullName, 
                       String email, String employeeId, String department, String designation) {
        super(id, username, rawPassword, fullName, email, Role.FACULTY);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
    }

    public FacultyUser(String id, String username, String passwordHash, String salt, 
                       String fullName, String email, boolean active, Date createdAt,
                       String employeeId, String department, String designation) {
        super(id, username, passwordHash, salt, fullName, email, Role.FACULTY, active, createdAt);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public String getRoleBadge() {
        return "FACULTY: " + designation + " (" + department + ")";
    }

    @Override
    public Map<String, String> getProfileDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Full Name", getFullName());
        details.put("Username", getUsername());
        details.put("Email", getEmail());
        details.put("Role", getRole().getDisplayName());
        details.put("Employee ID", employeeId);
        details.put("Department", department);
        details.put("Designation", designation);
        details.put("Account Status", isActive() ? "Active" : "Disabled");
        details.put("Member Since", getFormattedCreatedAt());
        return details;
    }
}
