package com.sset.erp.ui;

import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.Subject;
import com.sset.erp.model.SubjectAllocation;
import com.sset.erp.repository.FileSubjectAllocationRepository;
import com.sset.erp.repository.FileSubjectRepository;
import com.sset.erp.repository.MysqlUserRepository;
import com.sset.erp.repository.SubjectAllocationRepository;
import com.sset.erp.repository.SubjectRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Administrative dialog for managing the academic course catalogue and allocating subjects to faculty.
 */
public class SubjectAllocationDialog extends JDialog {
    private final SubjectRepository subjectRepository = new FileSubjectRepository();
    private final SubjectAllocationRepository allocationRepository = new FileSubjectAllocationRepository();
    private final List<FacultyUser> facultyList;

    // Tab 1: Subject Catalogue
    private final DefaultTableModel subjectTableModel;
    private final JTable subjectTable;
    private final JTextField txtSubCode = new JTextField(10);
    private final JTextField txtSubName = new JTextField(20);
    private final JComboBox<String> cmbSubDept = new JComboBox<>(new String[]{
            "Computer Science & Engineering",
            "Electronics & Communication Engineering",
            "Mechanical Engineering",
            "Civil Engineering"
    });
    private final JComboBox<Integer> cmbSubSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
    private final JComboBox<Integer> cmbSubCredits = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
    private final JComboBox<String> cmbSubType = new JComboBox<>(new String[]{"Theory", "Lab", "Elective"});

    // Tab 2: Allocations
    private final DefaultTableModel allocTableModel;
    private final JTable allocTable;
    private final JComboBox<String> cmbAllocSubject = new JComboBox<>();
    private final JComboBox<String> cmbAllocFaculty = new JComboBox<>();
    private final JComboBox<Integer> cmbAllocSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
    private final JTextField txtAllocBatch = new JTextField("2024-2028 (CS4)", 14);
    private final JTextField txtAllocAcadYear = new JTextField("2026-2027", 10);

    // Tab 3: Class Advisor Assignment
    private final DefaultTableModel advisorTableModel;
    private final JTable advisorTable;
    private final JComboBox<String> cmbAdvisorFaculty = new JComboBox<>();
    private final JComboBox<String> cmbAdvisorDept = new JComboBox<>(new String[]{
            "Computer Science & Engineering",
            "Electronics & Communication Engineering",
            "Mechanical Engineering",
            "Civil Engineering"
    });
    private final JComboBox<Integer> cmbAdvisorSem = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
    private final JTextField txtAdvisorBatch = new JTextField("2024-2028 (CS4)", 14);

    public SubjectAllocationDialog(JFrame owner) {
        super(owner, "Subject & Faculty Allocation Management", true);
        this.facultyList = new MysqlUserRepository().findAll().stream()
                .filter(FacultyUser.class::isInstance)
                .map(FacultyUser.class::cast)
                .toList();

        this.subjectTableModel = new DefaultTableModel(
                new Object[]{"Course Code", "Subject Name", "Department", "Semester", "Credits", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.subjectTable = new JTable(subjectTableModel);

        this.allocTableModel = new DefaultTableModel(
                new Object[]{"ID", "Subject Code", "Subject Name", "Semester", "Batch", "Assigned Faculty", "Academic Year"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.allocTable = new JTable(allocTableModel);

        this.advisorTableModel = new DefaultTableModel(
                new Object[]{"Employee ID", "Faculty Name", "Department", "Designation", "Assigned Class Advisor Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.advisorTable = new JTable(advisorTableModel);

        initUI();
        refreshSubjects();
        refreshAllocations();
        refreshAdvisors();
    }

    private void initUI() {
        setSize(1000, 620);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Subject Catalogue & Faculty Allocation", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 20, 0, 20));
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        tabbedPane.addTab("📚 Subject Catalogue", createSubjectsPanel());
        tabbedPane.addTab("👨‍🏫 Faculty Subject Allocation", createAllocationsPanel());
        tabbedPane.addTab("🏛️ Class Advisor Assignment", createAdvisorPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        ModernButton btnClose = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        btnClose.addActionListener(e -> dispose());
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createSubjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectTable.setRowHeight(28);
        subjectTable.setFont(UITheme.FONT_BODY);
        panel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Subject"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; form.add(txtSubCode, gbc);
        gbc.gridx = 2; form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3; form.add(txtSubName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; form.add(cmbSubDept, gbc);
        gbc.gridx = 2; form.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3; form.add(cmbSubSem, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; form.add(cmbSubCredits, gbc);
        gbc.gridx = 2; form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 3; form.add(cmbSubType, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        ModernButton btnSave = new ModernButton("Save Subject", ModernButton.Variant.PRIMARY);
        btnSave.addActionListener(e -> saveSubject());
        ModernButton btnDelete = new ModernButton("Delete Subject", ModernButton.Variant.DANGER);
        btnDelete.addActionListener(e -> deleteSelectedSubject());
        ModernButton btnClear = new ModernButton("Clear Form", ModernButton.Variant.GHOST);
        btnClear.addActionListener(e -> clearSubjectForm());

        btnRow.add(btnClear);
        btnRow.add(btnDelete);
        btnRow.add(btnSave);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(btnRow, gbc);

        panel.add(form, BorderLayout.SOUTH);

        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && subjectTable.getSelectedRow() >= 0) {
                int r = subjectTable.getSelectedRow();
                txtSubCode.setText(String.valueOf(subjectTableModel.getValueAt(r, 0)));
                txtSubName.setText(String.valueOf(subjectTableModel.getValueAt(r, 1)));
                cmbSubDept.setSelectedItem(subjectTableModel.getValueAt(r, 2));
                cmbSubSem.setSelectedItem(subjectTableModel.getValueAt(r, 3));
                cmbSubCredits.setSelectedItem(subjectTableModel.getValueAt(r, 4));
                cmbSubType.setSelectedItem(subjectTableModel.getValueAt(r, 5));
            }
        });

        return panel;
    }

    private JPanel createAllocationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        allocTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allocTable.setRowHeight(28);
        allocTable.setFont(UITheme.FONT_BODY);
        panel.add(new JScrollPane(allocTable), BorderLayout.CENTER);

        // Populate faculty combo
        cmbAllocFaculty.removeAllItems();
        for (FacultyUser f : facultyList) {
            cmbAllocFaculty.addItem(f.getUsername() + " - " + f.getFullName() + " (" + f.getDepartment() + ")");
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Assign Subject to Faculty"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; form.add(cmbAllocSubject, gbc);
        gbc.gridx = 2; form.add(new JLabel("Faculty:"), gbc);
        gbc.gridx = 3; form.add(cmbAllocFaculty, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; form.add(cmbAllocSem, gbc);
        gbc.gridx = 2; form.add(new JLabel("Batch / Section:"), gbc);
        gbc.gridx = 3; form.add(txtAllocBatch, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Academic Year:"), gbc);
        gbc.gridx = 1; form.add(txtAllocAcadYear, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        ModernButton btnAllocate = new ModernButton("Allocate Subject", ModernButton.Variant.PRIMARY);
        btnAllocate.addActionListener(e -> saveAllocation());
        ModernButton btnRemove = new ModernButton("Revoke Allocation", ModernButton.Variant.DANGER);
        btnRemove.addActionListener(e -> deleteSelectedAllocation());

        btnRow.add(btnRemove);
        btnRow.add(btnAllocate);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        form.add(btnRow, gbc);

        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshSubjects() {
        subjectTableModel.setRowCount(0);
        cmbAllocSubject.removeAllItems();
        try {
            List<Subject> list = subjectRepository.findAll();
            for (Subject s : list) {
                subjectTableModel.addRow(new Object[]{
                        s.getCode(), s.getName(), s.getDepartment(), s.getSemester(), s.getCredits(), s.getType()
                });
                cmbAllocSubject.addItem(s.getCode() + " - " + s.getName());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllocations() {
        allocTableModel.setRowCount(0);
        try {
            List<SubjectAllocation> list = allocationRepository.findAll();
            for (SubjectAllocation a : list) {
                allocTableModel.addRow(new Object[]{
                        a.getId(), a.getSubjectCode(), a.getSubjectName(), a.getSemester(),
                        a.getBatch(), a.getFacultyName() + " (" + a.getFacultyUsername() + ")", a.getAcademicYear()
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load allocations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSubject() {
        try {
            String code = txtSubCode.getText().trim();
            String name = txtSubName.getText().trim();
            String dept = (String) cmbSubDept.getSelectedItem();
            int sem = (Integer) cmbSubSem.getSelectedItem();
            int credits = (Integer) cmbSubCredits.getSelectedItem();
            String type = (String) cmbSubType.getSelectedItem();

            Subject subject = new Subject(code, name, dept, sem, credits, type);
            subjectRepository.save(subject);
            refreshSubjects();
            clearSubjectForm();
            JOptionPane.showMessageDialog(this, "Subject '" + code + "' saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedSubject() {
        int row = subjectTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String code = (String) subjectTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete subject '" + code + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                subjectRepository.delete(code);
                refreshSubjects();
                clearSubjectForm();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearSubjectForm() {
        txtSubCode.setText("");
        txtSubName.setText("");
        subjectTable.clearSelection();
    }

    private void saveAllocation() {
        if (cmbAllocSubject.getSelectedIndex() < 0 || cmbAllocFaculty.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select both a subject and a faculty member.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String subStr = (String) cmbAllocSubject.getSelectedItem();
            String subCode = subStr.split(" - ")[0].trim();
            Subject subject = subjectRepository.findByCode(subCode).orElse(null);
            if (subject == null) {
                JOptionPane.showMessageDialog(this, "Selected subject not found in repository.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int facultyIdx = cmbAllocFaculty.getSelectedIndex();
            FacultyUser faculty = facultyList.get(facultyIdx);

            int sem = (Integer) cmbAllocSem.getSelectedItem();
            String batch = txtAllocBatch.getText().trim();
            String acadYear = txtAllocAcadYear.getText().trim();

            SubjectAllocation alloc = SubjectAllocation.create(
                    subject.getCode(), subject.getName(), subject.getDepartment(),
                    sem, batch, faculty.getUsername(), faculty.getFullName(), acadYear
            );

            allocationRepository.save(alloc);
            refreshAllocations();
            JOptionPane.showMessageDialog(this, "Allocated '" + subject.getCode() + "' to " + faculty.getFullName() + "!",
                    "Allocation Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save allocation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAllocation() {
        int row = allocTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an allocation record to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = (String) allocTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Revoke this faculty allocation?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                allocationRepository.delete(id);
                refreshAllocations();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to remove allocation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createAdvisorPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        advisorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        advisorTable.setRowHeight(28);
        advisorTable.setFont(UITheme.FONT_BODY);
        panel.add(new JScrollPane(advisorTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Assign Class Advisor (Tutor) to Class"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Select Faculty:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        form.add(cmbAdvisorFaculty, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Class Department:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        form.add(cmbAdvisorDept, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        form.add(cmbAdvisorSem, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Batch:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        form.add(txtAdvisorBatch, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        ModernButton btnAssign = new ModernButton("Assign Class Advisor", ModernButton.Variant.PRIMARY);
        btnAssign.addActionListener(e -> saveAdvisorAssignment());
        btnPanel.add(btnAssign);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAdvisors() {
        advisorTableModel.setRowCount(0);
        cmbAdvisorFaculty.removeAllItems();

        List<FacultyUser> currentFaculty = new MysqlUserRepository().findAll().stream()
                .filter(FacultyUser.class::isInstance)
                .map(FacultyUser.class::cast)
                .toList();

        for (FacultyUser f : currentFaculty) {
            cmbAdvisorFaculty.addItem(f.getFullName() + " (" + f.getEmployeeId() + ")");
            advisorTableModel.addRow(new Object[]{
                    f.getEmployeeId(),
                    f.getFullName(),
                    f.getDepartment(),
                    f.getDesignation(),
                    f.getAdvisorClassLabel()
            });
        }
    }

    private void saveAdvisorAssignment() {
        if (cmbAdvisorFaculty.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            MysqlUserRepository userRepo = new MysqlUserRepository();
            List<FacultyUser> currentFaculty = userRepo.findAll().stream()
                    .filter(FacultyUser.class::isInstance)
                    .map(FacultyUser.class::cast)
                    .toList();

            FacultyUser target = currentFaculty.get(cmbAdvisorFaculty.getSelectedIndex());
            String dept = (String) cmbAdvisorDept.getSelectedItem();
            int sem = (Integer) cmbAdvisorSem.getSelectedItem();
            String batch = txtAdvisorBatch.getText().trim();

            target.setAdvisorDepartment(dept);
            target.setAdvisorSemester(sem);
            target.setAdvisorBatch(batch);

            userRepo.save(target);
            refreshAdvisors();
            JOptionPane.showMessageDialog(this, "Successfully designated " + target.getFullName() + " as Class Advisor of Sem " + sem + " " + batch + "!",
                    "Advisor Assigned", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to assign advisor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
