package com.sset.erp.ui.dashboard;

import com.sset.erp.model.Role;
import com.sset.erp.model.StudentUser;
import com.sset.erp.ui.StudentAttendanceDialog;
import com.sset.erp.ui.StudentAssessmentDialog;
import com.sset.erp.ui.StudentNotificationsDialog;
import com.sset.erp.ui.StudentTimetableDialog;
import com.sset.erp.ui.FeeHistoryDialog;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.QRCodeUtil;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Student Dashboard Panel displaying student personal records, Digital ID preview, and academic tools.
 */
public class StudentDashboardPanel extends JPanel {
    private final StudentUser studentUser;
    private final com.sset.erp.service.AuthService authService;
    private final JFrame parentFrame;

    public StudentDashboardPanel(com.sset.erp.service.AuthService authService, StudentUser studentUser, JFrame parentFrame) {
        this.authService = authService;
        this.studentUser = studentUser;
        this.parentFrame = parentFrame;
        initUI();
    }

    private void initUI() {
        removeAll();
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // Top Banner
        ModernCard bannerCard = new ModernCard(20);
        bannerCard.setLayout(new BorderLayout(16, 12));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        leftPanel.setOpaque(false);

        JLabel lblName = new JLabel("Welcome back, " + studentUser.getFullName());
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Roll Number: " + studentUser.getRollNumber() + " • Semester " + studentUser.getSemester() + " " + studentUser.getDepartment());
        lblSub.setFont(UITheme.FONT_SUBHEADER);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        leftPanel.add(lblName);
        leftPanel.add(lblSub);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        ModernButton btnEditProfile = new ModernButton("Edit Profile", ModernButton.Variant.OUTLINE);
        btnEditProfile.addActionListener(e -> {
            com.sset.erp.ui.EditProfileDialog dialog = new com.sset.erp.ui.EditProfileDialog(parentFrame, studentUser, authService, () -> {
                initUI();
            });
            dialog.setVisible(true);
        });

        rightPanel.add(btnEditProfile);
        rightPanel.add(BadgeLabel.forRole(Role.STUDENT));
        rightPanel.add(BadgeLabel.forStatus(studentUser.isActive()));

        bannerCard.add(leftPanel, BorderLayout.WEST);
        bannerCard.add(rightPanel, BorderLayout.EAST);

        // Center: 2 Column Layout (Left: Academic Profile, Right: Digital ID Card Preview)
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // Left Column: Student Details
        ModernCard profileCard = new ModernCard(20);
        profileCard.setLayout(new BorderLayout(12, 16));

        JLabel title = new JLabel("Student Academic Profile");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel detailsGrid = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsGrid.setOpaque(false);

        for (Map.Entry<String, String> entry : studentUser.getProfileDetails().entrySet()) {
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

        // Right Column: Digital ID Card & Academic Modules
        ModernCard idCard = new ModernCard(20);
        idCard.setLayout(new BorderLayout(12, 16));

        JLabel idTitle = new JLabel("Student Portal & Digital ID");
        idTitle.setFont(UITheme.FONT_HEADER);
        idTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel idBox = createDigitalIdPreview();

        JPanel modulesPreview = new JPanel(new GridLayout(0, 2, 10, 10));
        modulesPreview.setOpaque(false);

        String feeSubtitle = "View balances and receipts";
        try {
            com.sset.erp.repository.FileFeeRepository feeRepo = new com.sset.erp.repository.FileFeeRepository();
            java.util.List<com.sset.erp.model.FeeCharge> charges = feeRepo.findChargesByStudent(studentUser.getRollNumber());
            java.math.BigDecimal outstanding = java.math.BigDecimal.ZERO;
            int pendingCount = 0;
            for (com.sset.erp.model.FeeCharge c : charges) {
                java.math.BigDecimal bal = c.getAmount().subtract(feeRepo.getPaidAmount(c.getId()));
                if (bal.signum() > 0) {
                    outstanding = outstanding.add(bal);
                    pendingCount++;
                }
            }
            if (pendingCount > 0) {
                feeSubtitle = "⚠️ INR " + outstanding.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " pending";
            } else if (!charges.isEmpty()) {
                feeSubtitle = "✅ All dues cleared";
            }
        } catch (Exception ignored) {}

        modulesPreview.add(createActionPill("📱 Download QR", "Get personal QR for attendance", this::downloadStudentQR));
        modulesPreview.add(createActionPill("📊 Marks & CGPA", "View grades and academic performance",
                this::showAssessmentResults));
        modulesPreview.add(createActionPill("✅ Attendance", "View your attendance history", this::showAttendanceHistory));
        modulesPreview.add(createActionPill("🔔 Notifications", "View notices from faculty", this::showNotifications));
        modulesPreview.add(createActionPill("💳 Fees & Payments", feeSubtitle, this::showFeeHistory));
        modulesPreview.add(createActionPill("📅 Timetable & Courses", "View weekly schedule & subjects", this::showTimetable));

        idCard.add(idTitle, BorderLayout.NORTH);
        idCard.add(idBox, BorderLayout.CENTER);
        idCard.add(modulesPreview, BorderLayout.SOUTH);

        grid.add(profileCard);
        grid.add(idCard);

        add(bannerCard, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private JPanel createDigitalIdPreview() {
        // Outer ID card container with curved border & drop shadow feel
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();
                // Clean white card background with rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
                // Subtle card border
                g2.setColor(new Color(203, 213, 225)); // Slate 300
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.dispose();
            }

        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        // 1. Top Institution Header Banner with Gradient
        JPanel headerBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 64, 175), // Blue 800
                    w, h, new Color(59, 130, 246)  // Blue 500
                );
                g2.setPaint(gp);
                // Fill top rounded corners
                g2.fillRoundRect(0, 0, w - 1, h + 16, 16, 16);
                g2.dispose();
            }
        };
        headerBanner.setOpaque(false);
        headerBanner.setLayout(new BorderLayout());
        headerBanner.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel collegeName = new JLabel("🎓 EDUMANAGE ACADEMIC IDENTITY");
        collegeName.setFont(new Font("SansSerif", Font.BOLD, 11));
        collegeName.setForeground(Color.WHITE);

        JLabel validBadge = new JLabel("VERIFIED STUDENT");
        validBadge.setFont(new Font("SansSerif", Font.BOLD, 9));
        validBadge.setForeground(new Color(187, 247, 208)); // Emerald 200

        headerBanner.add(collegeName, BorderLayout.WEST);
        headerBanner.add(validBadge, BorderLayout.EAST);

        // 2. Middle Content: Avatar + Student Info + QR Code
        JPanel middleContent = new JPanel(new BorderLayout(14, 0));
        middleContent.setOpaque(false);
        middleContent.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Modern Student Avatar Icon / Monogram
        String initials = "ST";
        if (studentUser.getFullName() != null && !studentUser.getFullName().trim().isEmpty()) {
            String[] parts = studentUser.getFullName().trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1 && parts[0].length() >= 1) {
                initials = ("" + parts[0].charAt(0)).toUpperCase();
            }
        }
        final String avatarText = initials;

        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityRendering(g2);
                int size = Math.min(getWidth(), getHeight()) - 4;
                // Soft gradient circle
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(224, 231, 255), // Indigo 100
                    getWidth(), getHeight(), new Color(199, 210, 254) // Indigo 200
                );
                g2.setPaint(gp);
                g2.fillOval(2, 2, size, size);

                g2.setColor(new Color(165, 180, 252));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2, 2, size, size);

                g2.setColor(new Color(49, 46, 129)); // Indigo 900
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(avatarText)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(avatarText, tx, ty);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(74, 74));
        avatarPanel.setOpaque(false);

        // Info Column
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(studentUser.getFullName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel rollLbl = new JLabel("ID: " + studentUser.getRollNumber());
        rollLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        rollLbl.setForeground(UITheme.PRIMARY);

        JLabel deptLbl = new JLabel("Dept: " + studentUser.getDepartment());
        deptLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deptLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel batchLbl = new JLabel("Batch: " + studentUser.getBatch() + "  |  Sem " + studentUser.getSemester());
        batchLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        batchLbl.setForeground(UITheme.TEXT_MUTED);

        infoPanel.add(nameLbl);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(rollLbl);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(deptLbl);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(batchLbl);

        // QR Code Card on the right
        JPanel qrContainer = new JPanel();
        qrContainer.setLayout(new BoxLayout(qrContainer, BoxLayout.Y_AXIS));
        qrContainer.setOpaque(false);

        JPanel qrBox = new JPanel(new BorderLayout());
        qrBox.setPreferredSize(new Dimension(84, 84));
        qrBox.setBackground(Color.WHITE);
        qrBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(3, 3, 3, 3)
        ));

        try {
            String studentPayload = "STUDENT_ID:" + studentUser.getRollNumber();
            BufferedImage qrImg = QRCodeUtil.generateQRCode(studentPayload, 76, 76);
            JLabel qrLabel = new JLabel(new ImageIcon(qrImg));
            qrBox.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel qrLabel = new JLabel("QR Error", SwingConstants.CENTER);
            qrLabel.setFont(UITheme.FONT_SMALL);
            qrLabel.setForeground(Color.RED);
            qrBox.add(qrLabel, BorderLayout.CENTER);
        }

        JLabel scanHint = new JLabel("Scan for Attendance", SwingConstants.CENTER);
        scanHint.setFont(new Font("SansSerif", Font.PLAIN, 9));
        scanHint.setForeground(UITheme.TEXT_MUTED);
        scanHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        qrContainer.add(qrBox);
        qrContainer.add(Box.createVerticalStrut(3));
        qrContainer.add(scanHint);

        middleContent.add(avatarPanel, BorderLayout.WEST);
        middleContent.add(infoPanel, BorderLayout.CENTER);
        middleContent.add(qrContainer, BorderLayout.EAST);

        // 3. Footer Bar with security chip & validity note
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setBackground(new Color(248, 250, 252)); // Slate 50
        footerBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
            new EmptyBorder(6, 16, 8, 16)
        ));

        JLabel footerNote = new JLabel("Official Student Pass • SSET Autonomous ERP");
        footerNote.setFont(new Font("SansSerif", Font.PLAIN, 10));
        footerNote.setForeground(UITheme.TEXT_MUTED);

        JLabel statusPill = new JLabel("● ACTIVE");
        statusPill.setFont(new Font("SansSerif", Font.BOLD, 9));
        statusPill.setForeground(new Color(22, 163, 74)); // Green 600

        footerBar.add(footerNote, BorderLayout.WEST);
        footerBar.add(statusPill, BorderLayout.EAST);

        card.add(headerBanner, BorderLayout.NORTH);
        card.add(middleContent, BorderLayout.CENTER);
        card.add(footerBar, BorderLayout.SOUTH);

        return card;
    }

    private void downloadStudentQR() {
        try {
            String studentPayload = "STUDENT_ID:" + studentUser.getRollNumber();
            BufferedImage qrImage = QRCodeUtil.generateQRCode(studentPayload, 300, 300);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Student QR Code");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
            fileChooser.setSelectedFile(new File("My_QR_" + studentUser.getRollNumber() + ".png"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".png");
                }
                ImageIO.write(qrImage, "PNG", fileToSave);
                JOptionPane.showMessageDialog(this, 
                    "QR Code saved successfully to: \n" + fileToSave.getAbsolutePath(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to generate/save QR Code: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createActionPill(String title, String desc, Runnable action) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(UITheme.CARD_BG_HOVER);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_BODY_BOLD);
        t.setForeground(UITheme.TEXT_PRIMARY);
        JLabel d = new JLabel(desc);
        d.setFont(UITheme.FONT_SMALL);
        d.setForeground(UITheme.TEXT_SECONDARY);

        p.add(t);
        p.add(d);
        
        if (action != null) {
            p.setCursor(new Cursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    p.setBackground(new Color(28, 36, 54));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    p.setBackground(new Color(17, 23, 35));
                }
            });
        }
        
        return p;
    }

    private void showAssessmentResults() {
        new StudentAssessmentDialog(parentFrame, studentUser).setVisible(true);
    }

    private void showAttendanceHistory() {
        new StudentAttendanceDialog(parentFrame, studentUser).setVisible(true);
    }

    private void showNotifications() {
        new StudentNotificationsDialog(parentFrame, studentUser).setVisible(true);
    }

    private void showFeeHistory() {
        new FeeHistoryDialog(parentFrame, studentUser.getRollNumber(), "Fees & Payment History").setVisible(true);
    }

    private void showTimetable() {
        new StudentTimetableDialog(parentFrame, studentUser.getDepartment(), studentUser.getSemester(),
                studentUser.getBatch(), studentUser.getRollNumber(), studentUser.getFullName()).setVisible(true);
    }
}
