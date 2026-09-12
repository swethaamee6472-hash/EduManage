package com.sset.erp.model;

import com.sset.erp.util.PasswordUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract Base Class representing a User in the ERP system.
 * Demonstrates:
 * - Abstraction: Cannot be instantiated directly; defines abstract template methods.
 * - Encapsulation: State is private and accessed via validated accessors.
 * - Inheritance: Extended by specialized roles (Admin, Faculty, Student, Parent).
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private Date createdAt;

    public User(String id, String username, String rawPassword, String fullName, String email, Role role) {
        this.id = id;
        this.username = username;
        this.salt = PasswordUtil.generateSalt();
        this.passwordHash = PasswordUtil.hashPassword(rawPassword, this.salt);
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
        this.createdAt = new Date();
    }

    // Constructor for loading existing users with pre-hashed passwords
    public User(String id, String username, String passwordHash, String salt, 
                String fullName, String email, Role role, boolean active, Date createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : new Date();
    }

    /**
     * Polymorphic method to get a stylized badge label for the user.
     * Overridden by each subclass to show role-specific metadata.
     */
    public abstract String getRoleBadge();

    /**
     * Polymorphic method to return key-value profile data for dashboard display.
     */
    public abstract Map<String, String> getProfileDetails();

    /**
     * Validates a raw password against the stored salt and hash.
     */
    public boolean validatePassword(String rawPassword) {
        if (rawPassword == null || this.passwordHash == null || this.salt == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(rawPassword, this.passwordHash, this.salt);
    }

    /**
     * Updates password with new salt and hash.
     */
    public void setPassword(String newRawPassword) {
        this.salt = PasswordUtil.generateSalt();
        this.passwordHash = PasswordUtil.hashPassword(newRawPassword, this.salt);
    }

    // Getters and Setters (Encapsulation)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        return sdf.format(createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ") [" + role + "]";
    }
}
