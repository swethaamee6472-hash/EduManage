package com.sset.erp.ui;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernPasswordField;
import com.sset.erp.ui.components.ModernTextField;
import com.sset.erp.ui.dashboard.MainDashboardFrame;
import com.sset.erp.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Simplified Single-Frame Login Window for EduManage ERP.
 * Follows standard Swing JFrame inheritance with a compact, centered single-window layout.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService;

    private ModernTextField txtUsername;
    private ModernPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        // Frame Configuration
        setTitle("EduManage - Login");
        setSize(440, 560);
        setMinimumSize(new Dimension(400, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Container Panel with clean padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(32, 36, 32, 36));

        // Header Title
        JLabel lblHeader = new JLabel("EduManage Portal");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblHeader.setForeground(UITheme.TEXT_PRIMARY);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubHeader = new JLabel("Sign in to your account");
        lblSubHeader.setFont(UITheme.FONT_BODY);
        lblSubHeader.setForeground(UITheme.TEXT_SECONDARY);
        lblSubHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Quick Demo Accounts Bar
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        demoPanel.setOpaque(false);
        demoPanel.setMaximumSize(new Dimension(380, 45));

        JLabel demoLbl = new JLabel("Demo:");
        demoLbl.setFont(UITheme.FONT_SMALL);
        demoLbl.setForeground(UITheme.TEXT_MUTED);
        demoPanel.add(demoLbl);

        demoPanel.add(createDemoButton("Admin", "admin", "admin123"));
        demoPanel.add(createDemoButton("Faculty", "faculty_cs", "faculty123"));
        demoPanel.add(createDemoButton("Student", "student_cs", "student123"));
        demoPanel.add(createDemoButton("Parent", "parent_cs", "parent123"));
        demoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form Fields
        txtUsername = new ModernTextField("Enter username");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtPassword = new ModernPasswordField("Enter password");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.DANGER);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        ModernButton btnLogin = new ModernButton("Sign In", ModernButton.Variant.PRIMARY);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        ModernButton btnRegister = new ModernButton("Create New Account", ModernButton.Variant.OUTLINE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> openRegisterDialog());

        // Press Enter to submit
        txtUsername.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());

        // Assemble Components into Frame
        mainPanel.add(lblHeader);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(lblSubHeader);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(demoPanel);
        mainPanel.add(Box.createVerticalStrut(16));

        mainPanel.add(createFieldLabel("Username"));
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(12));

        mainPanel.add(createFieldLabel("Password"));
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(lblStatus);
        mainPanel.add(Box.createVerticalStrut(12));

        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(btnRegister);

        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton createDemoButton(String label, String username, String password) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BADGE);
        btn.setForeground(UITheme.PRIMARY);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(3, 8, 3, 8)
        ));
        btn.addActionListener(e -> {
            txtUsername.setText(username);
            txtPassword.setText(password);
            lblStatus.setText(" ");
        });
        return btn;
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        lblStatus.setText(" ");
        AuthResult result = authService.login(username, password);

        if (result.isSuccess()) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainDashboardFrame dashboard = new MainDashboardFrame(authService, result.getUser());
                dashboard.setVisible(true);
            });
        } else {
            lblStatus.setText("⚠️ " + result.getMessage());
            txtPassword.setText("");
        }
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this, authService);
        dialog.setVisible(true);
    }
}
