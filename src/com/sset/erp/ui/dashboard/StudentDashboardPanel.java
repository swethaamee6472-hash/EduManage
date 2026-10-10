package com.sset.erp.ui.dashboard;

import com.sset.erp.model.Role;
import com.sset.erp.model.StudentUser;
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

    public StudentDashboardPanel(StudentUser studentUser) {
        this.studentUser = studentUser;
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

        JLabel lblName = new JLabel("Welcome back, " + studentUser.getFullName());
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Roll Number: " + studentUser.getRollNumber() + " • Semester " + studentUser.getSemester() + " " + studentUser.getDepartment());
        lblSub.setFont(UITheme.FONT_SUBHEADER);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        leftPanel.add(lblName);
        leftPanel.add(lblSub);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
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

        JLabel idTitle = new JLabel("Digital Student Identity Card (Module 3 Preview)");
        idTitle.setFont(UITheme.FONT_HEADER);
        idTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel idBox = createDigitalIdPreview();

        JPanel modulesPreview = new JPanel(new GridLayout(2, 2, 10, 10));
        modulesPreview.setOpaque(false);

        modulesPreview.add(createActionPill("📱 Download QR", "Get personal QR for attendance", this::downloadStudentQR));
        modulesPreview.add(createActionPill("📊 Marks & CGPA", "View internal test scores", null));
        modulesPreview.add(createActionPill("📄 Resume Builder", "Generate ATS resume", null));
        modulesPreview.add(createActionPill("🎯 Placement Check", "Check job eligibility", null));

        idCard.add(idTitle, BorderLayout.NORTH);
        idCard.add(idBox, BorderLayout.CENTER);
        idCard.add(modulesPreview, BorderLayout.SOUTH);

        grid.add(profileCard);
        grid.add(idCard);

        add(bannerCard, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private JPanel createDigitalIdPreview() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(new Color(17, 23, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(49, 134, 255, 120), 1),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel sset = new JLabel("EDUMANAGE DIGITAL STUDENT IDENTITY");
        sset.setFont(new Font("SansSerif", Font.BOLD, 12));
        sset.setForeground(UITheme.ACCENT_CYAN);
        head.add(sset, BorderLayout.WEST);

        // Body
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // Avatar Placeholder
        JPanel avatar = new JPanel();
        avatar.setPreferredSize(new Dimension(80, 90));
        avatar.setBackground(new Color(24, 32, 48));
        avatar.setBorder(BorderFactory.createLineBorder(new Color(49, 134, 255, 100), 1));
        JLabel avText = new JLabel("PHOTO");
        avText.setFont(UITheme.FONT_SMALL);
        avText.setForeground(UITheme.ACCENT_CYAN);
        avatar.add(avText);

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 2));
        info.setOpaque(false);

        JLabel l1 = new JLabel(studentUser.getFullName());
        l1.setFont(UITheme.FONT_SUBHEADER);
        l1.setForeground(UITheme.TEXT_PRIMARY);

        JLabel l2 = new JLabel("Roll No: " + studentUser.getRollNumber());
        l2.setFont(UITheme.FONT_BODY_BOLD);
        l2.setForeground(UITheme.PRIMARY);

        JLabel l3 = new JLabel("Dept: " + studentUser.getDepartment());
        l3.setFont(UITheme.FONT_SMALL);
        l3.setForeground(UITheme.TEXT_SECONDARY);

        JLabel l4 = new JLabel("Batch: " + studentUser.getBatch() + " • Sem " + studentUser.getSemester());
        l4.setFont(UITheme.FONT_SMALL);
        l4.setForeground(UITheme.TEXT_SECONDARY);

        info.add(l1);
        info.add(l2);
        info.add(l3);
        info.add(l4);

        // Actual QR Code Box
        JPanel qrBox = new JPanel(new BorderLayout());
        qrBox.setPreferredSize(new Dimension(80, 80));
        qrBox.setBackground(Color.WHITE);
        qrBox.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        
        try {
            String studentPayload = "STUDENT_ID:" + studentUser.getRollNumber();
            BufferedImage qrImg = QRCodeUtil.generateQRCode(studentPayload, 80, 80);
            JLabel qrLabel = new JLabel(new ImageIcon(qrImg));
            qrBox.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel qrLabel = new JLabel("QR Error", SwingConstants.CENTER);
            qrLabel.setForeground(Color.RED);
            qrBox.add(qrLabel, BorderLayout.CENTER);
        }

        body.add(avatar, BorderLayout.WEST);
        body.add(info, BorderLayout.CENTER);
        body.add(qrBox, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

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
        p.setBackground(new Color(17, 23, 35));
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
}
