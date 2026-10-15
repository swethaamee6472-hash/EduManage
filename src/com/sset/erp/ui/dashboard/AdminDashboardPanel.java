package com.sset.erp.ui.dashboard;

import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.*;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.LiveWebcamQRScanner;
import com.sset.erp.ui.RegisterDialog;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.QRCodeUtil;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Admin Dashboard Panel providing institutional metrics and User Management controls.
 * Supports viewing, filtering, creating, and toggling user accounts.
 */
public class AdminDashboardPanel extends JPanel {
    private final AuthService authService;
    private final AdminUser adminUser;
    private final Frame parentFrame;

    private JLabel lblTotalUsers;
    private JLabel lblFacultyCount;
    private JLabel lblStudentCount;
    private JLabel lblParentCount;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> roleFilterComboBox;

    public AdminDashboardPanel(AuthService authService, AdminUser adminUser, Frame parentFrame) {
        this.authService = authService;
        this.adminUser = adminUser;
        this.parentFrame = parentFrame;
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 24, 24, 24));

        // Top Summary Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);

        lblTotalUsers = new JLabel("0");
        lblFacultyCount = new JLabel("0");
        lblStudentCount = new JLabel("0");
        lblParentCount = new JLabel("0");

        statsPanel.add(createStatCard("Total Users", lblTotalUsers, new Color(79, 70, 229), "👥"));
        statsPanel.add(createStatCard("Faculty Members", lblFacultyCount, Role.FACULTY.getPrimaryColor(), "👨‍🏫"));
        statsPanel.add(createStatCard("Active Students", lblStudentCount, Role.STUDENT.getPrimaryColor(), "🎓"));
        statsPanel.add(createStatCard("Parents Linked", lblParentCount, Role.PARENT.getPrimaryColor(), "👪"));

        // Central User Management Card
        ModernCard tableCard = new ModernCard(20);
        tableCard.setLayout(new BorderLayout(12, 12));

        // Toolbar: Vertical box layout with Title on top row and Actions bar below (wraps cleanly)
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        toolbarPanel.setOpaque(false);
        toolbarPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel tableTitle = new JLabel("User Accounts Directory & Access Control");
        tableTitle.setFont(UITheme.FONT_HEADER);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);
        titleRow.add(tableTitle, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actionPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter Role:");
        filterLabel.setFont(UITheme.FONT_BODY_BOLD);
        filterLabel.setForeground(UITheme.TEXT_SECONDARY);

        roleFilterComboBox = new JComboBox<>(new String[]{"All Roles", "ADMIN", "FACULTY", "STUDENT", "PARENT"});
        roleFilterComboBox.setFont(UITheme.FONT_BODY);
        roleFilterComboBox.setBackground(UITheme.INPUT_BG);
        roleFilterComboBox.setForeground(UITheme.TEXT_PRIMARY);
        roleFilterComboBox.setPreferredSize(new Dimension(120, 36));
        roleFilterComboBox.addActionListener(e -> applyFilter());

        ModernButton btnViewQR = new ModernButton("View / Save QR", ModernButton.Variant.OUTLINE);
        btnViewQR.setPreferredSize(new Dimension(140, 36));
        btnViewQR.addActionListener(e -> viewOrSaveSelectedUserQR());

        ModernButton btnDownloadAllQR = new ModernButton("Download All QR", ModernButton.Variant.OUTLINE);
        btnDownloadAllQR.setPreferredSize(new Dimension(145, 36));
        btnDownloadAllQR.setToolTipText("Save a ZIP containing a QR PNG for every user account");
        btnDownloadAllQR.addActionListener(e -> downloadAllUserQRs());
        titleRow.add(btnDownloadAllQR, BorderLayout.EAST);

        ModernButton btnScanQR = new ModernButton("📷 Scan QR", ModernButton.Variant.PRIMARY);
        btnScanQR.setPreferredSize(new Dimension(120, 36));
        btnScanQR.addActionListener(e -> scanUserQR());

        ModernButton btnAddUser = new ModernButton("+ Add User", ModernButton.Variant.PRIMARY);
        btnAddUser.setPreferredSize(new Dimension(120, 36));
        btnAddUser.addActionListener(e -> openAddUserDialog());

        ModernButton btnToggleStatus = new ModernButton("Toggle Status", ModernButton.Variant.OUTLINE);
        btnToggleStatus.setPreferredSize(new Dimension(130, 36));
        btnToggleStatus.addActionListener(e -> toggleSelectedUser());

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Variant.GHOST);
        btnRefresh.setPreferredSize(new Dimension(95, 36));
        btnRefresh.addActionListener(e -> refreshData());

        actionPanel.add(filterLabel);
        actionPanel.add(roleFilterComboBox);
        actionPanel.add(btnViewQR);
        actionPanel.add(btnScanQR);
        actionPanel.add(btnAddUser);
        actionPanel.add(btnToggleStatus);
        actionPanel.add(btnRefresh);

        toolbarPanel.add(titleRow);
        toolbarPanel.add(Box.createVerticalStrut(6));
        toolbarPanel.add(actionPanel);

        // Table Setup
        String[] columns = {"User ID", "Username", "Full Name", "Email", "Role", "Status", "Registered Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(UITheme.FONT_BODY);
        userTable.setRowHeight(38);
        userTable.setBackground(UITheme.CARD_BG);
        userTable.setForeground(UITheme.TEXT_PRIMARY);
        userTable.setGridColor(UITheme.BORDER);
        userTable.setShowVerticalLines(false);
        userTable.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        userTable.setSelectionForeground(UITheme.TEXT_PRIMARY);

        JTableHeader header = userTable.getTableHeader();
        header.setFont(UITheme.FONT_BODY_BOLD);
        header.setBackground(UITheme.BG_DARK);
        header.setForeground(UITheme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Dark Renderers
        DarkTextRenderer darkTextRenderer = new DarkTextRenderer();
        userTable.getColumnModel().getColumn(0).setCellRenderer(darkTextRenderer);
        userTable.getColumnModel().getColumn(1).setCellRenderer(darkTextRenderer);
        userTable.getColumnModel().getColumn(2).setCellRenderer(darkTextRenderer);
        userTable.getColumnModel().getColumn(3).setCellRenderer(darkTextRenderer);
        userTable.getColumnModel().getColumn(4).setCellRenderer(new RoleBadgeRenderer());
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        userTable.getColumnModel().getColumn(6).setCellRenderer(darkTextRenderer);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);

        tableCard.add(toolbarPanel, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(statsPanel, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(8, 6));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        headerRow.add(titleLabel, BorderLayout.WEST);
        headerRow.add(iconLabel, BorderLayout.EAST);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        // Update metric counters
        lblTotalUsers.setText(String.valueOf(authService.getUserCount()));
        lblFacultyCount.setText(String.valueOf(authService.getUserCountByRole(Role.FACULTY)));
        lblStudentCount.setText(String.valueOf(authService.getUserCountByRole(Role.STUDENT)));
        lblParentCount.setText(String.valueOf(authService.getUserCountByRole(Role.PARENT)));

        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String selected = (String) roleFilterComboBox.getSelectedItem();

        List<User> users;
        if (selected == null || "All Roles".equals(selected)) {
            users = authService.getAllUsers();
        } else {
            Role r = Role.valueOf(selected);
            users = authService.getUsersByRole(r);
        }

        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.isActive(),
                u.getFormattedCreatedAt()
            });
        }
    }

    private void openAddUserDialog() {
        RegisterDialog dialog = new RegisterDialog(parentFrame, authService);
        dialog.setVisible(true);
        refreshData();
    }

    private void toggleSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to modify status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        if (username.equalsIgnoreCase(adminUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot deactivate your own administrative account.", "Action Prohibited", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = authService.toggleUserStatus(userId);
        if (success) {
            refreshData();
            JOptionPane.showMessageDialog(this, "Status for user '" + username + "' updated successfully.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewOrSaveSelectedUserQR() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the directory to view or export their QR code.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        Optional<User> optUser = authService.getUserById(userId);
        if (!optUser.isPresent()) {
            JOptionPane.showMessageDialog(this, "User record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User u = optUser.get();
        String payload = getQRPayload(u);

        try {
            BufferedImage qrImage = QRCodeUtil.generateQRCode(payload, 280, 280);

            JDialog qrDialog = new JDialog(parentFrame, "QR Pass: " + u.getFullName(), true);
            qrDialog.setSize(380, 480);
            qrDialog.setLocationRelativeTo(this);
            qrDialog.getContentPane().setBackground(UITheme.CARD_BG);
            qrDialog.setLayout(new BorderLayout(12, 12));

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

            JLabel titleLbl = new JLabel(u.getFullName());
            titleLbl.setFont(UITheme.FONT_HEADER);
            titleLbl.setForeground(UITheme.TEXT_PRIMARY);
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel roleBadge = BadgeLabel.forRole(u.getRole());
            roleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subLbl = new JLabel(u.getUsername() + " (" + u.getId() + ")");
            subLbl.setFont(UITheme.FONT_SMALL);
            subLbl.setForeground(UITheme.TEXT_MUTED);
            subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel qrDisplay = new JLabel(new ImageIcon(qrImage));
            qrDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
            qrDisplay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)
            ));

            JLabel payloadLbl = new JLabel("Encoded: " + payload);
            payloadLbl.setFont(UITheme.FONT_SMALL);
            payloadLbl.setForeground(UITheme.TEXT_SECONDARY);
            payloadLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            contentPanel.add(titleLbl);
            contentPanel.add(Box.createVerticalStrut(4));
            contentPanel.add(roleBadge);
            contentPanel.add(Box.createVerticalStrut(4));
            contentPanel.add(subLbl);
            contentPanel.add(Box.createVerticalStrut(12));
            contentPanel.add(qrDisplay);
            contentPanel.add(Box.createVerticalStrut(8));
            contentPanel.add(payloadLbl);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setOpaque(false);

            ModernButton btnSave = new ModernButton("💾 Save PNG", ModernButton.Variant.PRIMARY);
            btnSave.addActionListener(ev -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save QR Code");
                fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
                fileChooser.setSelectedFile(new File("QR_" + u.getUsername() + ".png"));
                if (fileChooser.showSaveDialog(qrDialog) == JFileChooser.APPROVE_OPTION) {
                    File target = fileChooser.getSelectedFile();
                    if (!target.getName().toLowerCase().endsWith(".png")) {
                        target = new File(target.getParentFile(), target.getName() + ".png");
                    }
                    try {
                        ImageIO.write(qrImage, "PNG", target);
                        JOptionPane.showMessageDialog(qrDialog, "QR code saved successfully to:\n" + target.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(qrDialog, "Failed to save QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            ModernButton btnClose = new ModernButton("Close", ModernButton.Variant.GHOST);
            btnClose.addActionListener(ev -> qrDialog.dispose());

            buttonPanel.add(btnSave);
            buttonPanel.add(btnClose);

            qrDialog.add(contentPanel, BorderLayout.CENTER);
            qrDialog.add(buttonPanel, BorderLayout.SOUTH);
            qrDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate QR Code: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadAllUserQRs() {
        List<User> users = authService.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no user accounts to export.", "No Users", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Download QR Codes for All Users");
        fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP Archives", "zip"));
        fileChooser.setSelectedFile(new File("EduManage_QR_Codes.zip"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = fileChooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".zip")) {
            target = new File(target.getParentFile(), target.getName() + ".zip");
        }

        int exportedCount = 0;
        int userIndex = 1;
        try (FileOutputStream fileOutput = new FileOutputStream(target);
             ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {
            for (User user : users) {
                String fileName = String.format(
                    "QR_%03d_%s_%s.png",
                    userIndex++,
                    sanitizeFileName(user.getUsername()),
                    sanitizeFileName(user.getId())
                );
                BufferedImage qrImage = QRCodeUtil.generateQRCode(getQRPayload(user), 280, 280);

                zipOutput.putNextEntry(new ZipEntry(fileName));
                if (!ImageIO.write(qrImage, "PNG", zipOutput)) {
                    throw new IllegalStateException("PNG format is not available.");
                }
                zipOutput.closeEntry();
                exportedCount++;
            }
        } catch (Exception ex) {
            if (target.exists() && !target.delete()) {
                // Keep the incomplete archive if the operating system prevents cleanup.
            }
            JOptionPane.showMessageDialog(
                this,
                "Failed to download all QR codes: " + ex.getMessage(),
                "Download Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            exportedCount + " QR code(s) downloaded successfully to:\n" + target.getAbsolutePath(),
            "QR Codes Downloaded",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "user";
        }
        String safeValue = value.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safeValue.isBlank() ? "user" : safeValue;
    }

    private void scanUserQR() {
        LiveWebcamQRScanner scanner = new LiveWebcamQRScanner(decodedText -> {
            if (decodedText != null && !decodedText.isBlank()) {
                handleScannedAdminQR(decodedText);
            }
        });
        scanner.setVisible(true);
    }

    private void handleScannedAdminQR(String decodedText) {
        String query = decodedText.trim();
        Optional<User> foundUser = Optional.empty();

        if (query.startsWith("STUDENT_ID:")) {
            String roll = query.substring("STUDENT_ID:".length()).trim();
            for (User u : authService.getAllUsers()) {
                if (u instanceof StudentUser && roll.equalsIgnoreCase(((StudentUser) u).getRollNumber())) {
                    foundUser = Optional.of(u);
                    break;
                }
            }
        } else if (query.startsWith("FACULTY_ID:")) {
            String empId = query.substring("FACULTY_ID:".length()).trim();
            for (User u : authService.getAllUsers()) {
                if (u instanceof FacultyUser && empId.equalsIgnoreCase(((FacultyUser) u).getEmployeeId())) {
                    foundUser = Optional.of(u);
                    break;
                }
            }
        } else if (query.startsWith("USER_ID:")) {
            String uid = query.substring("USER_ID:".length()).trim();
            foundUser = authService.getUserById(uid);
        } else if (query.startsWith("USER:")) {
            String uname = query.substring("USER:".length()).trim();
            for (User u : authService.getAllUsers()) {
                if (uname.equalsIgnoreCase(u.getUsername())) {
                    foundUser = Optional.of(u);
                    break;
                }
            }
        } else {
            // General fallback: match by ID, username, or student roll number
            for (User u : authService.getAllUsers()) {
                if (query.equalsIgnoreCase(u.getId()) || query.equalsIgnoreCase(u.getUsername())) {
                    foundUser = Optional.of(u);
                    break;
                }
                if (u instanceof StudentUser && query.equalsIgnoreCase(((StudentUser) u).getRollNumber())) {
                    foundUser = Optional.of(u);
                    break;
                }
            }
        }

        if (foundUser.isPresent()) {
            User u = foundUser.get();
            StringBuilder details = new StringBuilder();
            details.append("User Found in EduManage Directory:\n\n");
            details.append("• Full Name: ").append(u.getFullName()).append("\n");
            details.append("• Username: ").append(u.getUsername()).append("\n");
            details.append("• Role: ").append(u.getRole().getDisplayName()).append("\n");
            details.append("• Email: ").append(u.getEmail()).append("\n");
            details.append("• Status: ").append(u.isActive() ? "Active" : "Disabled").append("\n");

            if (u instanceof StudentUser) {
                StudentUser s = (StudentUser) u;
                details.append("• Roll Number: ").append(s.getRollNumber()).append("\n");
                details.append("• Department: ").append(s.getDepartment()).append("\n");
                details.append("• Semester: ").append(s.getSemester()).append("\n");
            } else if (u instanceof FacultyUser) {
                FacultyUser f = (FacultyUser) u;
                details.append("• Employee ID: ").append(f.getEmployeeId()).append("\n");
                details.append("• Department: ").append(f.getDepartment()).append("\n");
                details.append("• Designation: ").append(f.getDesignation()).append("\n");
            } else if (u instanceof ParentUser) {
                ParentUser p = (ParentUser) u;
                details.append("• Ward Roll No: ").append(p.getStudentRollNumber()).append("\n");
                details.append("• Contact: ").append(p.getEmergencyContact()).append("\n");
            }

            // Also highlight row in table
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                if (u.getId().equals(tableModel.getValueAt(r, 0))) {
                    userTable.setRowSelectionInterval(r, r);
                    userTable.scrollRectToVisible(userTable.getCellRect(r, 0, true));
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, details.toString(), "Admin QR Verification Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "QR decoded data:\n\"" + decodedText + "\"\n\nNo matching student, faculty, or system user found.", "QR Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String getQRPayload(User u) {
        if (u instanceof StudentUser) {
            return "STUDENT_ID:" + ((StudentUser) u).getRollNumber();
        } else if (u instanceof FacultyUser) {
            return "FACULTY_ID:" + ((FacultyUser) u).getEmployeeId();
        } else {
            return "USER_ID:" + u.getId();
        }
    }

    // Role Column Pill Renderer
    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (value instanceof Role) {
                Role role = (Role) value;
                return BadgeLabel.forRole(role);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

    // Status Column Pill Renderer
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (value instanceof Boolean) {
                boolean active = (Boolean) value;
                return BadgeLabel.forStatus(active);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
    }

        // Antigravity Dark Text Cell Renderer -> Light Text Cell Renderer
    private static class DarkTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? UITheme.CARD_BG : UITheme.BG_DARK);
                setForeground(col == 1 ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);
            } else {
                setBackground(UITheme.PRIMARY_LIGHT);
                setForeground(UITheme.PRIMARY);
            }
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }
    }
}
