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
 * Modern Login Window for the Student Data Management ERP System.
 * Features institutional branding, demo quick-fill buttons, and responsive inputs.
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
        setTitle("EduManage - Student Data Management System");
        setSize(960, 620);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        // Left Panel: Institution Brand Banner
        JPanel brandPanel = createBrandPanel();

        // Right Panel: Authentication Form
        JPanel formPanel = createFormPanel();

        add(brandPanel);
        add(formPanel);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityRendering(g2);
                // Subtle gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(30, 41, 59)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(48, 40, 48, 40));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel logo = new JLabel("🎓");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 46));

        JLabel brandName = new JLabel("EduManage");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 32));
        brandName.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Student Data Management System");
        brandSub.setFont(new Font("SansSerif", Font.BOLD, 15));
        brandSub.setForeground(new Color(199, 210, 254));

        JLabel project = new JLabel("<html><br>"
                + "<font size='5' color='#818cf8'>Centralized College ERP</font><br>"
                + "<font size='3' color='#cbd5e1'>Secure Multi-Role Administrative & Academic Platform</font></html>");
        project.setFont(UITheme.FONT_BODY);
        project.setForeground(Color.WHITE);

        JPanel features = new JPanel(new GridLayout(4, 1, 0, 8));
        features.setOpaque(false);
        features.setBorder(new EmptyBorder(24, 0, 0, 0));

        features.add(createFeatureItem("🔐 Multi-Role Access Control (Admin, Faculty, Student, Parent)"));
        features.add(createFeatureItem("📱 Smart QR Attendance & Digital Student ID"));
        features.add(createFeatureItem("📊 Automated Marks, Grade & CGPA Engine"));
        features.add(createFeatureItem("🤖 AI Chatbot, Placement & Academic Analytics"));

        content.add(logo);
        content.add(Box.createVerticalStrut(12));
        content.add(brandName);
        content.add(brandSub);
        content.add(Box.createVerticalStrut(16));
        content.add(project);
        content.add(features);

        JLabel footer = new JLabel("EduManage ERP Platform • System v1.0");
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(new Color(100, 116, 139));

        panel.add(content, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel createFeatureItem(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(new Color(226, 232, 240));
        return l;
    }

    private JPanel createFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(UITheme.BG_LIGHT);
        container.setBorder(new EmptyBorder(36, 44, 36, 44));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Sign in with your credentials or click a demo account");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);

        // Quick-Fill Demo Pills (Viva/Presentation Helper)
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        demoBar.setOpaque(false);
        demoBar.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel demoLbl = new JLabel("Quick Demo:");
        demoLbl.setFont(UITheme.FONT_SMALL);
        demoLbl.setForeground(UITheme.TEXT_MUTED);
        demoBar.add(demoLbl);

        demoBar.add(createDemoButton("Admin", "admin", "admin123"));
        demoBar.add(createDemoButton("Faculty", "faculty_cs", "faculty123"));
        demoBar.add(createDemoButton("Student", "student_cs", "student123"));
        demoBar.add(createDemoButton("Parent", "parent_cs", "parent123"));

        // Inputs
        txtUsername = new ModernTextField("Enter institutional username");
        txtPassword = new ModernPasswordField("Enter account password");

        // Status / Error Alert Label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.DANGER);

        // Buttons
        ModernButton btnLogin = new ModernButton("Sign In to Portal", ModernButton.Variant.PRIMARY);
        btnLogin.addActionListener(e -> handleLogin());

        ModernButton btnRegister = new ModernButton("Register New Account", ModernButton.Variant.OUTLINE);
        btnRegister.addActionListener(e -> openRegisterDialog());

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(10));
        form.add(demoBar);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(12));

        form.add(createFieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(6));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(14));

        form.add(btnLogin);
        form.add(Box.createVerticalStrut(10));
        form.add(btnRegister);

        // Enter key listener for instant login
        txtUsername.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());

        container.add(form, BorderLayout.CENTER);
        return container;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JButton createDemoButton(String label, String username, String password) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BADGE);
        btn.setForeground(UITheme.ACCENT_CYAN);
        btn.setBackground(new Color(24, 32, 48));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(49, 134, 255, 100), 1),
            new EmptyBorder(5, 12, 5, 12)
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
