package com.sset.erp.ui.dashboard;

import com.sset.erp.model.*;
import com.sset.erp.service.AuthService;
import com.sset.erp.session.SessionManager;
import com.sset.erp.ui.LoginFrame;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Main application window hosting role-specific dashboard views and the navigation bar.
 */
public class MainDashboardFrame extends JFrame {
    private final AuthService authService;
    private final User currentUser;

    public MainDashboardFrame(AuthService authService, User currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("EduManage - " + currentUser.getRole().getDisplayName() + " Portal");
        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        // Top Navigation Bar
        JPanel navBar = new JPanel(new BorderLayout(16, 0));
        navBar.setBackground(UITheme.CARD_BG);
        navBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Brand / Logo Left
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoBadge = new JLabel("🎓");
        logoBadge.setFont(new Font("SansSerif", Font.PLAIN, 26));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 1));
        titleBlock.setOpaque(false);

        JLabel brandTitle = new JLabel("EduManage");
        brandTitle.setFont(UITheme.FONT_TITLE);
        brandTitle.setForeground(UITheme.PRIMARY);

        JLabel projectSubtitle = new JLabel("Student Data Management System");
        projectSubtitle.setFont(UITheme.FONT_SMALL);
        projectSubtitle.setForeground(UITheme.TEXT_SECONDARY);

        titleBlock.add(brandTitle);
        titleBlock.add(projectSubtitle);

        brandPanel.add(logoBadge);
        brandPanel.add(titleBlock);

        // User profile & Logout Right
        JPanel userNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userNavPanel.setOpaque(false);

        JLabel userNameLabel = new JLabel(currentUser.getFullName());
        userNameLabel.setFont(UITheme.FONT_BODY_BOLD);
        userNameLabel.setForeground(UITheme.TEXT_PRIMARY);

        BadgeLabel roleBadge = BadgeLabel.forRole(currentUser.getRole());

        ModernButton btnLogout = new ModernButton("Sign Out", ModernButton.Variant.OUTLINE);
        btnLogout.addActionListener(e -> handleLogout());

        userNavPanel.add(userNameLabel);
        userNavPanel.add(roleBadge);
        userNavPanel.add(btnLogout);

        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(userNavPanel, BorderLayout.EAST);

        // Mount Dynamic Role Dashboard
        JPanel roleDashboardPanel;
        if (currentUser instanceof AdminUser) {
            roleDashboardPanel = new AdminDashboardPanel(authService, (AdminUser) currentUser, this);
        } else if (currentUser instanceof FacultyUser) {
            roleDashboardPanel = new FacultyDashboardPanel((FacultyUser) currentUser);
        } else if (currentUser instanceof StudentUser) {
            roleDashboardPanel = new StudentDashboardPanel(authService, (StudentUser) currentUser, this);
        } else if (currentUser instanceof ParentUser) {
            roleDashboardPanel = new ParentDashboardPanel((ParentUser) currentUser);
        } else {
            roleDashboardPanel = new JPanel();
            roleDashboardPanel.add(new JLabel("Welcome to Student ERP"));
        }

        add(navBar, BorderLayout.NORTH);
        add(roleDashboardPanel, BorderLayout.CENTER);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out of the system?",
            "Confirm Sign Out",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(authService).setVisible(true));
        }
    }
}
