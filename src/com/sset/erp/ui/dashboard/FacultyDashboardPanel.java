package com.sset.erp.ui.dashboard;

import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.Role;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Faculty Dashboard Panel presenting academic profile, course allocations, and faculty tools.
 */
public class FacultyDashboardPanel extends JPanel {
    private final FacultyUser facultyUser;

    public FacultyDashboardPanel(FacultyUser facultyUser) {
        this.facultyUser = facultyUser;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Welcome Banner
        ModernCard welcomeCard = new ModernCard(20);
        welcomeCard.setLayout(new BorderLayout(16, 12));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        headerLeft.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome, " + facultyUser.getFullName());
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Department of " + facultyUser.getDepartment() + " • " + facultyUser.getDesignation());
        lblSub.setFont(UITheme.FONT_SUBHEADER);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        headerLeft.add(lblWelcome);
        headerLeft.add(lblSub);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerRight.setOpaque(false);
        headerRight.add(BadgeLabel.forRole(Role.FACULTY));
        headerRight.add(BadgeLabel.forStatus(facultyUser.isActive()));

        welcomeCard.add(headerLeft, BorderLayout.WEST);
        welcomeCard.add(headerRight, BorderLayout.EAST);

        // Center Content: 2 Columns (Profile Details on Left, Academic Modules on Right)
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        contentGrid.setOpaque(false);

        // Left: Profile Card
        ModernCard profileCard = new ModernCard(20);
        profileCard.setLayout(new BorderLayout(12, 16));

        JLabel profileTitle = new JLabel("Faculty Profile & Credentials");
        profileTitle.setFont(UITheme.FONT_HEADER);
        profileTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsPanel.setOpaque(false);

        for (Map.Entry<String, String> entry : facultyUser.getProfileDetails().entrySet()) {
            JLabel keyLabel = new JLabel(entry.getKey());
            keyLabel.setFont(UITheme.FONT_BODY_BOLD);
            keyLabel.setForeground(UITheme.TEXT_SECONDARY);

            JLabel valLabel = new JLabel(entry.getValue());
            valLabel.setFont(UITheme.FONT_BODY);
            valLabel.setForeground(UITheme.TEXT_PRIMARY);

            detailsPanel.add(keyLabel);
            detailsPanel.add(valLabel);
        }

        profileCard.add(profileTitle, BorderLayout.NORTH);
        profileCard.add(detailsPanel, BorderLayout.CENTER);

        // Right: Academic Workflow & Module Launcher
        ModernCard workflowCard = new ModernCard(20);
        workflowCard.setLayout(new BorderLayout(12, 16));

        JLabel workflowTitle = new JLabel("Academic Modules & Quick Actions");
        workflowTitle.setFont(UITheme.FONT_HEADER);
        workflowTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel modulesList = new JPanel(new GridLayout(3, 1, 0, 12));
        modulesList.setOpaque(false);

        modulesList.add(createModuleCard("Smart QR Attendance", "Launch dynamic attendance QR session", "Module 4", "📱"));
        modulesList.add(createModuleCard("Marks & Internal Assessment", "Enter internal test scores and GPA", "Module 5", "📝"));
        modulesList.add(createModuleCard("Timetable & Subject Allocation", "View assigned weekly lecture periods", "Module 13", "📅"));

        workflowCard.add(workflowTitle, BorderLayout.NORTH);
        workflowCard.add(modulesList, BorderLayout.CENTER);

        contentGrid.add(profileCard);
        contentGrid.add(workflowCard);

        add(welcomeCard, BorderLayout.NORTH);
        add(contentGrid, BorderLayout.CENTER);
    }

    private JPanel createModuleCard(String title, String desc, String badge, String icon) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(new Color(17, 23, 35));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));

        JPanel textP = new JPanel(new GridLayout(2, 1, 0, 2));
        textP.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_SUBHEADER);
        lblT.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblD = new JLabel(desc);
        lblD.setFont(UITheme.FONT_SMALL);
        lblD.setForeground(UITheme.TEXT_SECONDARY);

        textP.add(lblT);
        textP.add(lblD);

        ModernButton btn = new ModernButton("Open", ModernButton.Variant.OUTLINE);
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Module '" + title + "' is scheduled in Phase 2 of the implementation plan.",
            "Module Status", JOptionPane.INFORMATION_MESSAGE));

        p.add(lblIcon, BorderLayout.WEST);
        p.add(textP, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);

        return p;
    }
}
