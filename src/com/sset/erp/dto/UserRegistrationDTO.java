package com.sset.erp.dto;

import com.sset.erp.model.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object capturing user input during registration.
 * Decouples presentation/input layer from domain entities.
 */
public class UserRegistrationDTO {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role;

    // Role-specific fields
    private String adminLevel;        // Admin
    private String employeeId;       // Faculty
    private String department;       // Faculty & Student
    private String designation;      // Faculty
    private String rollNumber;       // Student
    private int semester;            // Student
    private String batch;            // Student
    private String studentRollNumber;// Parent
    private String emergencyContact; // Parent

    public UserRegistrationDTO() {
        this.role = Role.STUDENT;
        this.semester = 3;
    }

    /**
     * Validates fields according to business rules.
     * @return List of error messages; empty if validation passes.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (username == null || username.trim().length() < 3) {
            errors.add("Username must be at least 3 characters long.");
        }
        if (password == null || password.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("Full name cannot be blank.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            errors.add("A valid email address is required.");
        }
        if (role == null) {
            errors.add("User role must be selected.");
            return errors;
        }

        switch (role) {
            case ADMIN:
                if (adminLevel == null || adminLevel.trim().isEmpty()) {
                    adminLevel = "System Administrator";
                }
                break;
            case FACULTY:
                if (employeeId == null || employeeId.trim().isEmpty()) {
                    errors.add("Faculty Employee ID is required.");
                }
                if (department == null || department.trim().isEmpty()) {
                    errors.add("Department is required.");
                }
                if (designation == null || designation.trim().isEmpty()) {
                    designation = "Assistant Professor";
                }
                break;
            case STUDENT:
                if (rollNumber == null || rollNumber.trim().isEmpty()) {
                    errors.add("Student Roll Number is required.");
                }
                if (department == null || department.trim().isEmpty()) {
                    errors.add("Department is required.");
                }
                if (semester < 1 || semester > 8) {
                    errors.add("Semester must be between 1 and 8.");
                }
                if (batch == null || batch.trim().isEmpty()) {
                    batch = "2024-2028";
                }
                break;
            case PARENT:
                if (studentRollNumber == null || studentRollNumber.trim().isEmpty()) {
                    errors.add("Ward's Student Roll Number is required.");
                }
                if (emergencyContact == null || emergencyContact.trim().length() < 10) {
                    errors.add("Emergency contact must be at least 10 digits.");
                }
                break;
        }

        return errors;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username != null ? username.trim() : null; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName != null ? fullName.trim() : null; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim() : null; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
