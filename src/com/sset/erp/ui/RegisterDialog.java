package com.sset.erp.ui;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.Role;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernPasswordField;
import com.sset.erp.ui.components.ModernTextField;
import com.sset.erp.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Registration Dialog for creating new user accounts.
 * Dynamically adapts role-specific fields using modern Swing layouts.
 */
public class RegisterDialog extends JDialog {
    private final AuthService authService;

    private ModernTextField txtUsername;
    private ModernPasswordField txtPassword;
    private ModernTextField txtFullName;
    private ModernTextField txtEmail;
    private JComboBox<Role> cmbRole;

    // Role Specific Fields
    private JPanel roleSpecificContainer;
    private CardLayout cardLayout;

    // Student fields
    private ModernTextField txtRollNumber;
    private ModernTextField txtStudentDept;
    private JComboBox<Integer> cmbSemester;
    private ModernTextField txtBatch;

    // Faculty fields
    private ModernTextField txtEmployeeId;
    private ModernTextField txtFacultyDept;
    private ModernTextField txtDesignation;

    // Parent fields
    private ModernTextField txtWardRollNo;
    private ModernTextField txtEmergencyContact;

    // Admin fields
    private ModernTextField txtAdminLevel;

    public RegisterDialog(Frame parent, AuthService authService) {
        super(parent, "Register New ERP Account", true);
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setSize(560, 680);
        setResizable(false);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("Create User Account");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("EduManage • Authentication & Role Management");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        header.add(title);
        header.add(sub);

        // Body Form (Scrollable)
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Common Fields
        txtFullName = new ModernTextField("e.g. Ragendu M");
        txtUsername = new ModernTextField("e.g. ragendu_m");
        txtEmail = new ModernTextField("e.g. ragendu@edumanage.edu");
        txtPassword = new ModernPasswordField("Minimum 6 characters");

        cmbRole = new JComboBox<>(Role.values());
        cmbRole.setSelectedItem(Role.STUDENT);
        cmbRole.setFont(UITheme.FONT_BODY);
        cmbRole.setBackground(UITheme.INPUT_BG);
        cmbRole.setForeground(UITheme.TEXT_PRIMARY);
        cmbRole.setPreferredSize(new Dimension(0, 40));
        cmbRole.addActionListener(e -> updateRoleCard());

        form.add(createFieldBlock("Account Role", cmbRole));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Full Name", txtFullName));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Username", txtUsername));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Email Address", txtEmail));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Password", txtPassword));
        form.add(Box.createVerticalStrut(14));

        // Role-Specific Card Panel
        cardLayout = new CardLayout();
        roleSpecificContainer = new JPanel(cardLayout);
        roleSpecificContainer.setOpaque(false);

        roleSpecificContainer.add(createStudentForm(), Role.STUDENT.name());
        roleSpecificContainer.add(createFacultyForm(), Role.FACULTY.name());
        roleSpecificContainer.add(createParentForm(), Role.PARENT.name());
        roleSpecificContainer.add(createAdminForm(), Role.ADMIN.name());

        form.add(roleSpecificContainer);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Bottom Action Bar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Variant.GHOST);
        btnCancel.addActionListener(e -> dispose());

        ModernButton btnSubmit = new ModernButton("Create Account", ModernButton.Variant.PRIMARY);
        btnSubmit.addActionListener(e -> handleRegister());

        footer.add(btnCancel);
        footer.add(btnSubmit);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        updateRoleCard();
    }

    private JPanel createStudentForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        txtRollNumber = new ModernTextField("e.g. SSET24CS099");
        txtStudentDept = new ModernTextField("e.g. Computer Science & Engineering");
        txtStudentDept.setText("Computer Science & Engineering");

        cmbSemester = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cmbSemester.setSelectedItem(3);
        cmbSemester.setFont(UITheme.FONT_BODY);
        cmbSemester.setBackground(UITheme.INPUT_BG);
        cmbSemester.setForeground(UITheme.TEXT_PRIMARY);
        cmbSemester.setPreferredSize(new Dimension(0, 40));

        txtBatch = new ModernTextField("e.g. 2024-2028 (CS4)");
        txtBatch.setText("2024-2028 (CS4)");

        p.add(createFieldBlock("Student Roll Number", txtRollNumber));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Department", txtStudentDept));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Semester", cmbSemester));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Academic Batch", txtBatch));

        return p;
    }

    private JPanel createFacultyForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        txtEmployeeId = new ModernTextField("e.g. EMP-CS-205");
        txtFacultyDept = new ModernTextField("e.g. Computer Science & Engineering");
        txtFacultyDept.setText("Computer Science & Engineering");
        txtDesignation = new ModernTextField("e.g. Assistant Professor");
        txtDesignation.setText("Assistant Professor");

        p.add(createFieldBlock("Employee ID", txtEmployeeId));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Department", txtFacultyDept));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Designation", txtDesignation));

        return p;
    }

    private JPanel createParentForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        txtWardRollNo = new ModernTextField("e.g. SSET24CS042");
        txtEmergencyContact = new ModernTextField("e.g. +91 98470 54321");

        p.add(createFieldBlock("Ward's Student Roll Number", txtWardRollNo));
        p.add(Box.createVerticalStrut(10));
        p.add(createFieldBlock("Emergency Contact Number", txtEmergencyContact));

        return p;
    }

    private JPanel createAdminForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        txtAdminLevel = new ModernTextField("e.g. Department Administrator");
        txtAdminLevel.setText("System Administrator");

        p.add(createFieldBlock("Administrative Level / Role Title", txtAdminLevel));

        return p;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_SECONDARY);

        block.add(label, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    private void updateRoleCard() {
        Role selectedRole = (Role) cmbRole.getSelectedItem();
        if (selectedRole != null) {
            cardLayout.show(roleSpecificContainer, selectedRole.name());
        }
    }

    private void handleRegister() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setRole((Role) cmbRole.getSelectedItem());
        dto.setFullName(txtFullName.getText());
        dto.setUsername(txtUsername.getText());
        dto.setEmail(txtEmail.getText());
        dto.setPassword(new String(txtPassword.getPassword()));

        if (dto.getRole() == Role.STUDENT) {
            dto.setRollNumber(txtRollNumber.getText());
            dto.setDepartment(txtStudentDept.getText());
            dto.setSemester((Integer) cmbSemester.getSelectedItem());
            dto.setBatch(txtBatch.getText());
        } else if (dto.getRole() == Role.FACULTY) {
            dto.setEmployeeId(txtEmployeeId.getText());
            dto.setDepartment(txtFacultyDept.getText());
            dto.setDesignation(txtDesignation.getText());
        } else if (dto.getRole() == Role.PARENT) {
            dto.setStudentRollNumber(txtWardRollNo.getText());
            dto.setEmergencyContact(txtEmergencyContact.getText());
        } else if (dto.getRole() == Role.ADMIN) {
            dto.setAdminLevel(txtAdminLevel.getText());
        }

        AuthResult result = authService.register(dto);
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Registration Incomplete", JOptionPane.ERROR_MESSAGE);
        }
    }
}
