package com.sset.erp.ui;

import com.sset.erp.model.StudentUser;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernTextField;
import com.sset.erp.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditProfileDialog extends JDialog {
    private final StudentUser studentUser;
    private final AuthService authService;
    private final Runnable onSuccess;

    private ModernTextField txtFullName;
    private ModernTextField txtEmail;
    private ModernTextField txtDepartment;
    private JComboBox<Integer> cmbSemester;
    private ModernTextField txtBatch;

    public EditProfileDialog(Frame parent, StudentUser studentUser, AuthService authService, Runnable onSuccess) {
        super(parent, "Edit Student Profile", true);
        this.studentUser = studentUser;
        this.authService = authService;
        this.onSuccess = onSuccess;
        initUI();
    }

    private void initUI() {
        setSize(480, 560);
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

        JLabel title = new JLabel("Edit Profile");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Update your personal and academic information");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        header.add(title);
        header.add(sub);

        // Body Form (Scrollable)
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 24, 16, 24));

        txtFullName = new ModernTextField("Full Name");
        txtFullName.setText(studentUser.getFullName());

        txtEmail = new ModernTextField("Email Address");
        txtEmail.setText(studentUser.getEmail());

        txtDepartment = new ModernTextField("Department");
        txtDepartment.setText(studentUser.getDepartment());

        cmbSemester = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cmbSemester.setSelectedItem(studentUser.getSemester());
        cmbSemester.setFont(UITheme.FONT_BODY);
        cmbSemester.setBackground(UITheme.INPUT_BG);
        cmbSemester.setForeground(UITheme.TEXT_PRIMARY);
        cmbSemester.setPreferredSize(new Dimension(0, 40));

        txtBatch = new ModernTextField("Batch");
        txtBatch.setText(studentUser.getBatch());

        form.add(createFieldBlock("Full Name", txtFullName));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Email Address", txtEmail));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Department", txtDepartment));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Semester", cmbSemester));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Academic Batch", txtBatch));
        form.add(Box.createVerticalStrut(14));

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

        ModernButton btnSubmit = new ModernButton("Save Changes", ModernButton.Variant.PRIMARY);
        btnSubmit.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSubmit);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
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

    private void handleSave() {
        if (txtFullName.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name and Email are required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        studentUser.setFullName(txtFullName.getText().trim());
        studentUser.setEmail(txtEmail.getText().trim());
        studentUser.setDepartment(txtDepartment.getText().trim());
        studentUser.setSemester((Integer) cmbSemester.getSelectedItem());
        studentUser.setBatch(txtBatch.getText().trim());

        if (authService.updateUser(studentUser)) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) {
                onSuccess.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
