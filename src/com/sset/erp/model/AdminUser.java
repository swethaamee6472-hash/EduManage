package com.sset.erp.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an Administrator user in the ERP system.
 * Inherits common attributes and behaviors from User.
 */
public class AdminUser extends User {
    private static final long serialVersionUID = 1L;

    private String adminLevel;

    public AdminUser(String id, String username, String rawPassword, String fullName, String email, String adminLevel) {
        super(id, username, rawPassword, fullName, email, Role.ADMIN);
        this.adminLevel = adminLevel != null ? adminLevel : "Super Administrator";
    }

    public AdminUser(String id, String username, String passwordHash, String salt, 
                     String fullName, String email, boolean active, Date createdAt, String adminLevel) {
        super(id, username, passwordHash, salt, fullName, email, Role.ADMIN, active, createdAt);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    @Override
    public String getRoleBadge() {
        return "ADMIN: " + adminLevel;
    }

    @Override
    public Map<String, String> getProfileDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Full Name", getFullName());
        details.put("Username", getUsername());
        details.put("Email", getEmail());
        details.put("Role", getRole().getDisplayName());
        details.put("Admin Level", adminLevel);
        details.put("Account Status", isActive() ? "Active" : "Disabled");
        details.put("Created At", getFormattedCreatedAt());
        return details;
    }
}
