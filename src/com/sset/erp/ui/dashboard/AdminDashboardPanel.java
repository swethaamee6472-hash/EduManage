package com.sset.erp.ui.dashboard;

import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.AdminUser;
import com.sset.erp.model.Role;
import com.sset.erp.model.User;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.RegisterDialog;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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

        // Toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout(12, 0));
        toolbarPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("User Accounts Directory & Access Control");
        tableTitle.setFont(UITheme.FONT_HEADER);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter Role:");
        filterLabel.setFont(UITheme.FONT_BODY_BOLD);
        filterLabel.setForeground(UITheme.TEXT_SECONDARY);

        roleFilterComboBox = new JComboBox<>(new String[]{"All Roles", "ADMIN", "FACULTY", "STUDENT", "PARENT"});
        roleFilterComboBox.setFont(UITheme.FONT_BODY);
        roleFilterComboBox.setBackground(UITheme.INPUT_BG);
        roleFilterComboBox.setForeground(UITheme.TEXT_PRIMARY);
        roleFilterComboBox.addActionListener(e -> applyFilter());

        ModernButton btnAddUser = new ModernButton("+ Add New User", ModernButton.Variant.PRIMARY);
        btnAddUser.addActionListener(e -> openAddUserDialog());

        ModernButton btnToggleStatus = new ModernButton("Toggle Active / Deactivate", ModernButton.Variant.OUTLINE);
        btnToggleStatus.addActionListener(e -> toggleSelectedUser());

        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Variant.GHOST);
        btnRefresh.addActionListener(e -> refreshData());

        actionPanel.add(filterLabel);
        actionPanel.add(roleFilterComboBox);
        actionPanel.add(btnAddUser);
        actionPanel.add(btnToggleStatus);
        actionPanel.add(btnRefresh);

        toolbarPanel.add(tableTitle, BorderLayout.WEST);
        toolbarPanel.add(actionPanel, BorderLayout.EAST);

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
        header.setBackground(new Color(17, 23, 35));
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

    // Antigravity Dark Text Cell Renderer
    private static class DarkTextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? UITheme.CARD_BG : new Color(17, 23, 35));
                setForeground(col == 1 ? UITheme.ACCENT_CYAN : UITheme.TEXT_PRIMARY);
            } else {
                setBackground(UITheme.PRIMARY_LIGHT);
                setForeground(Color.WHITE);
            }
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }
    }
}
