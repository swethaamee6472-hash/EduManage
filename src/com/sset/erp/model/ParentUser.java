package com.sset.erp.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Parent or Guardian in the ERP system.
 * Linked to a ward's student roll number.
 * Inherits common attributes and behaviors from User.
 */
public class ParentUser extends User {
    private static final long serialVersionUID = 1L;

    private String studentRollNumber;
    private String emergencyContact;

    public ParentUser(String id, String username, String rawPassword, String fullName, 
                      String email, String studentRollNumber, String emergencyContact) {
        super(id, username, rawPassword, fullName, email, Role.PARENT);
        this.studentRollNumber = studentRollNumber;
        this.emergencyContact = emergencyContact;
    }

    public ParentUser(String id, String username, String passwordHash, String salt, 
                      String fullName, String email, boolean active, Date createdAt,
                      String studentRollNumber, String emergencyContact) {
        super(id, username, passwordHash, salt, fullName, email, Role.PARENT, active, createdAt);
        this.studentRollNumber = studentRollNumber;
        this.emergencyContact = emergencyContact;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String getRoleBadge() {
        return "PARENT / GUARDIAN (Ward: " + studentRollNumber + ")";
    }

    @Override
    public Map<String, String> getProfileDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Parent Name", getFullName());
        details.put("Username", getUsername());
        details.put("Email", getEmail());
        details.put("Role", getRole().getDisplayName());
        details.put("Ward Roll Number", studentRollNumber);
        details.put("Contact Number", emergencyContact);
        details.put("Account Status", isActive() ? "Active" : "Disabled");
        details.put("Registered On", getFormattedCreatedAt());
        return details;
    }
}
