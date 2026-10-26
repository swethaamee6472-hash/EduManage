package com.sset.erp.ui;

import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.NotificationType;
import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.repository.FileUserRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Faculty interface for creating, editing, and removing student assessment results.
 */
public class AssessmentManagementDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FacultyUser facultyUser;
    private final FileAssessmentRepository repository = new FileAssessmentRepository();
    private final FileStudentNotificationRepository notificationRepository = new FileStudentNotificationRepository();
    private final List<StudentUser> students;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public AssessmentManagementDialog(JFrame owner, FacultyUser facultyUser) {
        super(owner, "Marks, Grade & CGPA Management", true);
        this.facultyUser = facultyUser;
        this.students = new FileUserRepository().findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .toList();
        this.tableModel = new DefaultTableModel(
                new Object[] {"Student", "Roll Number", "Subject", "Assessment", "Score", "Maximum",
                    "Credits", "Result", "Grade", "Grade Point", "Recorded", "Record ID"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        initUI();
        refreshRecords();
    }

    private void initUI() {
        setSize(1050, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Student Marks, Grades & CGPA", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(11));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        ModernButton addButton = new ModernButton("Add Marks", ModernButton.Variant.PRIMARY);
        ModernButton editButton = new ModernButton("Edit Selected", ModernButton.Variant.OUTLINE);
        ModernButton deleteButton = new ModernButton("Delete Selected", ModernButton.Variant.OUTLINE);
        ModernButton refreshButton = new ModernButton("Refresh", ModernButton.Variant.OUTLINE);
        ModernButton closeButton = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        addButton.addActionListener(e -> editRecord(null));
        editButton.addActionListener(e -> editSelectedRecord());
        deleteButton.addActionListener(e -> deleteSelectedRecord());
        refreshButton.addActionListener(e -> refreshRecords());
        closeButton.addActionListener(e -> dispose());
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        actions.add(closeButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshRecords() {
        try {
            tableModel.setRowCount(0);
            for (AssessmentRecord record : repository.findAll()) {
                String studentName = students.stream()
                        .filter(student -> student.getRollNumber().equalsIgnoreCase(record.getRollNumber()))
                        .map(StudentUser::getFullName)
                        .findFirst()
                        .orElse("Unknown student");
                tableModel.addRow(new Object[] {
                        studentName, record.getRollNumber(), record.getSubject(), record.getAssessmentName(),
                        formatNumber(record.getScore()), formatNumber(record.getMaximumScore()),
                        record.getSubjectCredits(), String.format("%.1f%%", record.getPercentage()),
                        record.getGrade(), record.getGradePoint(),
                        DATE_FORMAT.format(record.getRecordedAt()),
                        record.getId()
                });
            }
        } catch (IOException e) {
            showError("Unable to load assessment results.", e);
        }
    }

    private void editSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an assessment to edit.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        try {
            Optional<AssessmentRecord> selected = repository.findById(tableModel.getValueAt(modelRow, 11).toString());
            if (selected.isPresent()) {
                editRecord(selected.get());
            } else {
                JOptionPane.showMessageDialog(this, "The selected assessment no longer exists. Refresh and try again.",
                        "Assessment Not Found", JOptionPane.WARNING_MESSAGE);
                refreshRecords();
            }
        } catch (IOException e) {
            showError("Unable to load the selected assessment.", e);
        }
    }

    private void editRecord(AssessmentRecord existing) {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add a student account before entering assessment marks.",
                    "No Students Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AssessmentRecord updated = showEditor(existing);
        if (updated == null) {
            return;
        }
        try {
            AssessmentRecord saved = repository.save(updated);
            refreshRecords();
            try {
                StudentNotification notification = StudentNotification.create(NotificationType.EXAM,
                        saved.getRollNumber(), "Assessment updated: " + saved.getAssessmentName(),
                        saved.getSubject() + " result: " + formatNumber(saved.getScore()) + " / "
                                + formatNumber(saved.getMaximumScore()) + " ("
                                + String.format("%.1f%%, Grade %s", saved.getPercentage(), saved.getGrade()) + ").",
                        facultyUser.getFullName());
                notificationRepository.save(notification);
            } catch (IOException e) {
                showError("Marks were saved, but the student notification could not be sent.", e);
            }
        } catch (IOException e) {
            showError("Unable to save assessment marks.", e);
        }
    }

    private AssessmentRecord showEditor(AssessmentRecord existing) {
        JComboBox<StudentUser> studentBox = new JComboBox<>(new DefaultComboBoxModel<>(students.toArray(new StudentUser[0])));
        studentBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getRollNumber() + " - " + value.getFullName());
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        JTextField subjectField = new JTextField(existing == null ? "" : existing.getSubject());
        JTextField assessmentField = new JTextField(existing == null ? "" : existing.getAssessmentName());
        JTextField scoreField = new JTextField(existing == null ? "" : formatNumber(existing.getScore()));
        JTextField maximumField = new JTextField(existing == null ? "100" : formatNumber(existing.getMaximumScore()));
        JTextField creditsField = new JTextField(existing == null ? "3" : Integer.toString(existing.getSubjectCredits()));

        if (existing != null) {
            students.stream()
                    .filter(student -> student.getRollNumber().equalsIgnoreCase(existing.getRollNumber()))
                    .findFirst()
                    .ifPresent(studentBox::setSelectedItem);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.add(new JLabel("Student"));
        form.add(studentBox);
        form.add(new JLabel("Subject"));
        form.add(subjectField);
        form.add(new JLabel("Assessment"));
        form.add(assessmentField);
        form.add(new JLabel("Score"));
        form.add(scoreField);
        form.add(new JLabel("Maximum score"));
        form.add(maximumField);
        form.add(new JLabel("Subject credits"));
        form.add(creditsField);
        form.add(new JLabel("CGPA credit rule"));
        form.add(new JLabel("Keep credits consistent for every assessment in this subject."));
        form.add(new JLabel("Grade scale"));
        form.add(new JLabel("S: 90+ | A+: 85+ | A: 80+ | B+: 70+ | B: 60+ | C: 50+ | P: 40+ | F: below 40"));

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form,
                    existing == null ? "Add Assessment Marks" : "Edit Assessment Marks",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                StudentUser student = (StudentUser) studentBox.getSelectedItem();
                if (student == null) {
                    throw new IllegalArgumentException("Select a student.");
                }
                double score = Double.parseDouble(scoreField.getText().trim());
                double maximum = Double.parseDouble(maximumField.getText().trim());
                int credits = Integer.parseInt(creditsField.getText().trim());
                if (existing == null) {
                    return AssessmentRecord.create(student.getRollNumber(), subjectField.getText(),
                            assessmentField.getText(), score, maximum, credits, facultyUser.getFullName());
                }
                return new AssessmentRecord(existing.getId(), student.getRollNumber(), subjectField.getText(),
                        assessmentField.getText(), score, maximum, credits, facultyUser.getFullName(),
                        existing.getRecordedAt());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Score, maximum score, and whole-number credits must be valid.",
                        "Invalid Marks", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Assessment",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an assessment to delete.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String rollNumber = tableModel.getValueAt(modelRow, 1).toString();
        String subject = tableModel.getValueAt(modelRow, 2).toString();
        String assessmentName = tableModel.getValueAt(modelRow, 3).toString();
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + assessmentName + " for " + rollNumber + " in " + subject + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Optional<AssessmentRecord> selected = repository.findById(tableModel.getValueAt(modelRow, 11).toString());
            if (selected.isPresent()) {
                repository.deleteById(selected.get().getId());
                refreshRecords();
            } else {
                JOptionPane.showMessageDialog(this, "The selected assessment no longer exists. Refresh and try again.",
                        "Assessment Not Found", JOptionPane.WARNING_MESSAGE);
                refreshRecords();
            }
        } catch (IOException e) {
            showError("Unable to delete assessment marks.", e);
        }
    }

    private void showError(String message, IOException error) {
        JOptionPane.showMessageDialog(this, message + "\n" + error.getMessage(), "Storage Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }
}
