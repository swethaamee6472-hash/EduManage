package com.sset.erp.ui.dashboard;

import com.sset.erp.model.ParentUser;
import com.sset.erp.model.Role;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Parent Portal Panel presenting child academic records, attendance indicators, and notifications.
 */
public class ParentDashboardPanel extends JPanel {
    private final ParentUser parentUser;

    public ParentDashboardPanel(ParentUser parentUser) {
        this.parentUser = parentUser;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Banner
        ModernCard bannerCard = new ModernCard(20);
        bannerCard.setLayout(new BorderLayout(16, 12));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        leftPanel.setOpaque(false);

        JLabel lblName = new JLabel("Welcome, " + parentUser.getFullName());
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Parent / Guardian Portal • Ward Roll No: " + parentUser.getStudentRollNumber());
        lblSub.setFont(UITheme.FONT_SUBHEADER);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        leftPanel.add(lblName);
        leftPanel.add(lblSub);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(BadgeLabel.forRole(Role.PARENT));
        rightPanel.add(BadgeLabel.forStatus(parentUser.isActive()));

        bannerCard.add(leftPanel, BorderLayout.WEST);
        bannerCard.add(rightPanel, BorderLayout.EAST);

        // Center Content (Left: Guardian Profile, Right: Ward Academic Pulse)
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // Left Card: Parent Account Info
        ModernCard profileCard = new ModernCard(20);
        profileCard.setLayout(new BorderLayout(12, 16));

        JLabel title = new JLabel("Guardian Account Details");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel detailsGrid = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsGrid.setOpaque(false);

        for (Map.Entry<String, String> entry : parentUser.getProfileDetails().entrySet()) {
            JLabel k = new JLabel(entry.getKey());
            k.setFont(UITheme.FONT_BODY_BOLD);
            k.setForeground(UITheme.TEXT_SECONDARY);

            JLabel v = new JLabel(entry.getValue());
            v.setFont(UITheme.FONT_BODY);
            v.setForeground(UITheme.TEXT_PRIMARY);

            detailsGrid.add(k);
            detailsGrid.add(v);
        }

        profileCard.add(title, BorderLayout.NORTH);
        profileCard.add(detailsGrid, BorderLayout.CENTER);

        // Right Card: Ward Academic Health Status
        ModernCard wardCard = new ModernCard(20);
        wardCard.setLayout(new BorderLayout(12, 16));

        JLabel wardTitle = new JLabel("Ward Academic Status: " + parentUser.getStudentRollNumber());
        wardTitle.setFont(UITheme.FONT_HEADER);
        wardTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel statusList = new JPanel(new GridLayout(3, 1, 0, 12));
        statusList.setOpaque(false);

        statusList.add(createStatusRow("Overall Attendance", "89.4% (Eligible for Exams)", UITheme.SUCCESS, "✅"));
        statusList.add(createStatusRow("Semester Fee Status", "Tuition & Bus Fees Cleared", UITheme.INFO, "💳"));
        statusList.add(createStatusRow("Faculty Advisor", "Dr. Litty Koshy (Dept. of CSE)", UITheme.PRIMARY, "📞"));

        wardCard.add(wardTitle, BorderLayout.NORTH);
        wardCard.add(statusList, BorderLayout.CENTER);

        grid.add(profileCard);
        grid.add(wardCard);

        add(bannerCard, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private JPanel createStatusRow(String title, String statusText, Color color, String icon) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(new Color(17, 23, 35));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_SUBHEADER);
        t.setForeground(UITheme.TEXT_PRIMARY);

        JLabel s = new JLabel(statusText);
        s.setFont(UITheme.FONT_BODY_BOLD);
        s.setForeground(color);

        textPanel.add(t);
        textPanel.add(s);

        p.add(iconLabel, BorderLayout.WEST);
        p.add(textPanel, BorderLayout.CENTER);

        return p;
    }
}
