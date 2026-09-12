package com.sset.erp.model;

import java.awt.Color;

/**
 * Enumeration representing user roles in the Student Data Management System.
 * Part of Module 1: Authentication & Role Management.
 */
public enum Role {
    ADMIN("Administrator", new Color(192, 132, 252), new Color(45, 27, 78)),    // Antigravity Purple
    FACULTY("Faculty Member", new Color(96, 165, 250), new Color(23, 37, 84)),   // Antigravity Blue
    STUDENT("Student", new Color(52, 211, 153), new Color(6, 68, 50)),           // Antigravity Green
    PARENT("Parent / Guardian", new Color(251, 191, 36), new Color(69, 39, 10)); // Antigravity Amber

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
