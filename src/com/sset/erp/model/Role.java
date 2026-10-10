package com.sset.erp.model;

import java.awt.Color;

/**
 * Enumeration representing user roles in the Student Data Management System.
 * Part of Module 1: Authentication & Role Management.
 */
public enum Role {
    ADMIN("Administrator", new Color(107, 33, 168), new Color(243, 232, 255)),    // Purple 800 on Purple 100
    FACULTY("Faculty Member", new Color(30, 64, 175), new Color(219, 234, 254)),   // Blue 800 on Blue 100
    STUDENT("Student", new Color(6, 95, 70), new Color(209, 250, 229)),           // Green 800 on Green 100
    PARENT("Parent / Guardian", new Color(146, 64, 14), new Color(254, 243, 199)); // Amber 800 on Amber 100

    private final String displayName;
    private final Color primaryColor;
    private final Color badgeBgColor;

    Role(String displayName, Color primaryColor, Color badgeBgColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.badgeBgColor = badgeBgColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getBadgeBgColor() {
        return badgeBgColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
