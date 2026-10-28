package com.sset.erp.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Faculty member in the ERP system.
 * Inherits common attributes and behaviors from User.
 * Supports Linways-style Class Advisor (Tutor) role assignment.
 */
public class FacultyUser extends User {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String department;
    private String designation;

    // Linways Class Advisor / Tutor assignment (the faculty's "own class")
    private String advisorDepartment;
    private int advisorSemester;
    private String advisorBatch;

    public FacultyUser(String id, String username, String rawPassword, String fullName, 
                       String email, String employeeId, String department, String designation) {
        this(id, username, rawPassword, fullName, email, employeeId, department, designation,
             department, 3, "2024-2028 (CS4)");
    }

    public FacultyUser(String id, String username, String rawPassword, String fullName, 
                       String email, String employeeId, String department, String designation,
                       String advisorDepartment, int advisorSemester, String advisorBatch) {
        super(id, username, rawPassword, fullName, email, Role.FACULTY);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.advisorDepartment = (advisorDepartment == null || advisorDepartment.trim().isEmpty()) ? department : advisorDepartment.trim();
        this.advisorSemester = advisorSemester <= 0 ? 3 : advisorSemester;
        this.advisorBatch = (advisorBatch == null || advisorBatch.trim().isEmpty()) ? "2024-2028 (CS4)" : advisorBatch.trim();
    }

    public FacultyUser(String id, String username, String passwordHash, String salt, 
                       String fullName, String email, boolean active, Date createdAt,
                       String employeeId, String department, String designation) {
        this(id, username, passwordHash, salt, fullName, email, active, createdAt,
             employeeId, department, designation, department, 3, "2024-2028 (CS4)");
    }

    public FacultyUser(String id, String username, String passwordHash, String salt, 
                       String fullName, String email, boolean active, Date createdAt,
                       String employeeId, String department, String designation,
                       String advisorDepartment, int advisorSemester, String advisorBatch) {
        super(id, username, passwordHash, salt, fullName, email, Role.FACULTY, active, createdAt);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.advisorDepartment = (advisorDepartment == null || advisorDepartment.trim().isEmpty()) ? department : advisorDepartment.trim();
        this.advisorSemester = advisorSemester <= 0 ? 3 : advisorSemester;
        this.advisorBatch = (advisorBatch == null || advisorBatch.trim().isEmpty()) ? "2024-2028 (CS4)" : advisorBatch.trim();
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

    public String getAdvisorDepartment() {
        return advisorDepartment;
    }

    public void setAdvisorDepartment(String advisorDepartment) {
        this.advisorDepartment = advisorDepartment;
    }

    public int getAdvisorSemester() {
        return advisorSemester;
    }

    public void setAdvisorSemester(int advisorSemester) {
        this.advisorSemester = advisorSemester;
    }

    public String getAdvisorBatch() {
        return advisorBatch;
    }

    public void setAdvisorBatch(String advisorBatch) {
        this.advisorBatch = advisorBatch;
    }

    public boolean isClassAdvisor() {
        return advisorBatch != null && !advisorBatch.trim().isEmpty();
    }

    public String getAdvisorClassLabel() {
        if (!isClassAdvisor()) return "Not Assigned";
        return "Sem " + advisorSemester + " " + advisorBatch + " (" + advisorDepartment + ")";
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
        details.put("Class Advisor Of", getAdvisorClassLabel());
        details.put("Account Status", isActive() ? "Active" : "Disabled");
        details.put("Member Since", getFormattedCreatedAt());
        return details;
    }
}
