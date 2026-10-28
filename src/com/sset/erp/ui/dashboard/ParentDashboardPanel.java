package com.sset.erp.ui.dashboard;

import com.sset.erp.model.ParentUser;
import com.sset.erp.model.Role;
import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.AcademicPerformanceSummary;
import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.repository.MysqlUserRepository;
import com.sset.erp.ui.FeeHistoryDialog;
import com.sset.erp.ui.ParentProgressDialog;
import com.sset.erp.ui.StudentTimetableDialog;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.util.Map;
import java.util.List;
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

        JPanel statusList = new JPanel(new GridLayout(6, 1, 0, 12));
        statusList.setOpaque(false);

        statusList.add(createStatusRow("Attendance", "Loading attendance records...", UITheme.INFO, "✅"));
        statusList.add(createStatusRow("Academic Progress", "Loading marks and assessments...", UITheme.INFO, "📊"));
        statusList.add(createStatusRow("Fees", "Loading fee balance...", UITheme.INFO, "💳"));
        ModernButton progressButton = new ModernButton("View Attendance & Marks", ModernButton.Variant.PRIMARY);
        progressButton.addActionListener(e -> {
            openWardProgress();
            refreshStatusRows(statusList);
        });
        statusList.add(progressButton);
        ModernButton feesButton = new ModernButton("View Fees & Payments", ModernButton.Variant.OUTLINE);
        feesButton.addActionListener(e -> {
            openWardFees();
            refreshStatusRows(statusList);
        });
        statusList.add(feesButton);
        ModernButton timetableButton = new ModernButton("View Class Timetable", ModernButton.Variant.OUTLINE);
        timetableButton.addActionListener(e -> openWardTimetable());
        statusList.add(timetableButton);

        refreshStatusRows(statusList);

        wardCard.add(wardTitle, BorderLayout.NORTH);
        wardCard.add(statusList, BorderLayout.CENTER);

        grid.add(profileCard);
        grid.add(wardCard);

        add(bannerCard, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private void refreshStatusRows(JPanel statusList) {
        try {
            List<AttendanceRecord> attendance = new FileAttendanceRepository()
                    .findByRollNumber(parentUser.getStudentRollNumber());
            List<AssessmentRecord> assessments = new FileAssessmentRepository()
                    .findByRollNumber(parentUser.getStudentRollNumber());
            FileFeeRepository feeRepository = new FileFeeRepository();
            List<FeeCharge> feeCharges = feeRepository.findChargesByStudent(parentUser.getStudentRollNumber());
            double earned = 0;
            double possible = 0;
            for (AssessmentRecord record : assessments) {
                earned += record.getScore();
                possible += record.getMaximumScore();
            }
            String attendanceText = attendance.size() + " class record"
                    + (attendance.size() == 1 ? "" : "s")
                    + " (total scheduled classes unavailable)";
            String progressText = possible == 0
                    ? assessments.size() + " assessments; no marks recorded"
                    : String.format("CGPA %.2f / 10; %.1f%% overall marks across %d assessments",
                            AcademicPerformanceSummary.from(assessments).getCgpa(),
                            earned / possible * 100.0, assessments.size());
            java.math.BigDecimal outstanding = java.math.BigDecimal.ZERO;
            int pendingCount = 0;
            for (FeeCharge charge : feeCharges) {
                java.math.BigDecimal bal = charge.getAmount().subtract(feeRepository.getPaidAmount(charge.getId()));
                outstanding = outstanding.add(bal);
                if (bal.signum() > 0) {
                    pendingCount++;
                }
            }
            String feeText;
            Color feeColor;
            if (pendingCount > 0) {
                feeText = pendingCount + " pending fee" + (pendingCount == 1 ? "" : "s")
                        + " • INR " + outstanding.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                        + " due";
                feeColor = UITheme.DANGER;
            } else if (!feeCharges.isEmpty()) {
                feeText = "All " + feeCharges.size() + " fees settled (INR 0.00 outstanding)";
                feeColor = UITheme.SUCCESS;
            } else {
                feeText = "No fee charges recorded";
                feeColor = UITheme.INFO;
            }
            statusList.removeAll();
            statusList.add(createStatusRow("Attendance", attendanceText, UITheme.INFO, "✅"));
            statusList.add(createStatusRow("Academic Progress", progressText, UITheme.PRIMARY, "📊"));
            statusList.add(createStatusRow("Fees & Pending Dues", feeText, feeColor, "💳"));
            ModernButton progressButton = new ModernButton("View Attendance & Marks", ModernButton.Variant.PRIMARY);
            progressButton.addActionListener(e -> openWardProgress());
            statusList.add(progressButton);
            ModernButton feesButton = new ModernButton("View Fees & Payments", ModernButton.Variant.OUTLINE);
            feesButton.addActionListener(e -> {
                openWardFees();
                refreshStatusRows(statusList);
            });
            statusList.add(feesButton);
            ModernButton timetableButton = new ModernButton("View Class Timetable", ModernButton.Variant.OUTLINE);
            timetableButton.addActionListener(e -> openWardTimetable());
            statusList.add(timetableButton);
            statusList.revalidate();
            statusList.repaint();
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load ward progress.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWardProgress() {
        new ParentProgressDialog((JFrame) SwingUtilities.getWindowAncestor(this), parentUser).setVisible(true);
    }

    private void openWardFees() {
        new FeeHistoryDialog(SwingUtilities.getWindowAncestor(this), parentUser.getStudentRollNumber(),
                "Ward Fees & Payment History").setVisible(true);
    }

    private void openWardTimetable() {
        StudentUser student = new MysqlUserRepository().findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(s -> s.getRollNumber().equalsIgnoreCase(parentUser.getStudentRollNumber()))
                .findFirst()
                .orElse(null);
        if (student != null) {
            new StudentTimetableDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    student.getDepartment(), student.getSemester(), student.getBatch(),
                    student.getRollNumber(), student.getFullName()).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Ward record not found for roll: " + parentUser.getStudentRollNumber(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStatusRow(String title, String statusText, Color color, String icon) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.CARD_BG_HOVER);
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
