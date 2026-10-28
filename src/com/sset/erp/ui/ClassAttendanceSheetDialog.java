package com.sset.erp.ui;

import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.NotificationType;
import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.model.SubjectAllocation;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Linways-style Hourly Class Attendance Sheet for Subject Faculty.
 * Enables quick multi-student attendance recording with instant visual feedback and notification dispatch.
 */
public class ClassAttendanceSheetDialog extends JDialog {
    private final FacultyUser facultyUser;
    private final SubjectAllocation allocation;
    private final List<StudentUser> students;
    private final Runnable onAttendanceSaved;

    private final FileAttendanceRepository attendanceRepo = new FileAttendanceRepository();
    private final FileStudentNotificationRepository notificationRepo = new FileStudentNotificationRepository();

    private final JTextField txtDate = new JTextField(LocalDate.now().toString(), 10);
    private final JComboBox<String> cmbPeriod = new JComboBox<>(new String[]{
            "Period 1 (09:00 - 10:00 AM)",
            "Period 2 (10:00 - 11:00 AM)",
            "Period 3 (11:15 - 12:15 PM)",
            "Period 4 (12:15 - 01:15 PM)",
            "Period 5 (02:00 - 03:00 PM)",
            "Period 6 (03:00 - 04:00 PM)"
    });

    private DefaultTableModel tableModel;
    private JTable studentTable;

    public ClassAttendanceSheetDialog(Window owner, FacultyUser facultyUser,
                                      SubjectAllocation allocation, List<StudentUser> classStudents,
                                      Runnable onAttendanceSaved) {
        super(owner, "Class Attendance Register - " + allocation.getSubjectCode(), ModalityType.APPLICATION_MODAL);
        this.facultyUser = facultyUser;
        this.allocation = allocation;
        this.students = new ArrayList<>(classStudents);
        this.onAttendanceSaved = onAttendanceSaved;
        initUI();
    }

    private void initUI() {
        setSize(920, 620);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout(14, 14));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(16, 20, 0, 20));

        ModernCard bannerCard = new ModernCard(16);
        bannerCard.setLayout(new BorderLayout(16, 8));
        bannerCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel leftMeta = new JPanel(new GridLayout(2, 1, 0, 4));
        leftMeta.setOpaque(false);

        JLabel lblTitle = new JLabel("📝 Hourly Attendance Entry: " + allocation.getSubjectCode() + " – " + allocation.getSubjectName());
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblTarget = new JLabel("Class: " + allocation.getDepartment() + " • Semester " + allocation.getSemester()
                + " • Batch " + allocation.getBatch() + "  |  Faculty: " + facultyUser.getFullName());
        lblTarget.setFont(UITheme.FONT_BODY);
        lblTarget.setForeground(UITheme.TEXT_SECONDARY);

        leftMeta.add(lblTitle);
        leftMeta.add(lblTarget);

        bannerCard.add(leftMeta, BorderLayout.WEST);
        topPanel.add(bannerCard);
        topPanel.add(Box.createVerticalStrut(10));

        // Controls bar: Date, Period, Select All, Deselect All
        JPanel controlsCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        controlsCard.setBackground(UITheme.CARD_BG);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        JLabel lblD = new JLabel("Session Date (YYYY-MM-DD):");
        lblD.setFont(UITheme.FONT_BODY_BOLD);
        lblD.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblP = new JLabel("Period / Hour:");
        lblP.setFont(UITheme.FONT_BODY_BOLD);
        lblP.setForeground(UITheme.TEXT_SECONDARY);

        ModernButton btnAllPresent = new ModernButton("Mark All Present", ModernButton.Variant.OUTLINE);
        ModernButton btnAllAbsent = new ModernButton("Mark All Absent", ModernButton.Variant.OUTLINE);

        btnAllPresent.addActionListener(e -> setAllAttendance(true));
        btnAllAbsent.addActionListener(e -> setAllAttendance(false));

        controlsCard.add(lblD);
        controlsCard.add(txtDate);
        controlsCard.add(Box.createHorizontalStrut(10));
        controlsCard.add(lblP);
        controlsCard.add(cmbPeriod);
        controlsCard.add(Box.createHorizontalStrut(10));
        controlsCard.add(btnAllPresent);
        controlsCard.add(btnAllAbsent);

        topPanel.add(controlsCard);
        add(topPanel, BorderLayout.NORTH);

        // Center Table: Checkbox, Roll No, Student Name, Prior Attendance %
        tableModel = new DefaultTableModel(
                new Object[]{"Present", "Roll Number", "Student Full Name", "Email", "Class / Batch"}, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0; // Only checkbox is editable
            }
        };

        for (StudentUser stu : students) {
            tableModel.addRow(new Object[]{
                    Boolean.TRUE, // Default to Present
                    stu.getRollNumber(),
                    stu.getFullName(),
                    stu.getEmail(),
                    "Sem " + stu.getSemester() + " (" + stu.getBatch() + ")"
            });
        }

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(32);
        studentTable.setFont(UITheme.FONT_BODY);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        studentTable.getColumnModel().getColumn(0).setMaxWidth(90);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(1).setCellRenderer(center);
        studentTable.getColumnModel().getColumn(4).setCellRenderer(center);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(0, 20, 0, 20));
        tableWrapper.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // Footer Submit Action Bar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JLabel countSummary = new JLabel("Total Students: " + students.size());
        countSummary.setFont(UITheme.FONT_BODY_BOLD);
        countSummary.setForeground(UITheme.TEXT_SECONDARY);

        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Variant.OUTLINE);
        ModernButton btnSubmit = new ModernButton("Save & Dispatch Attendance", ModernButton.Variant.PRIMARY);

        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> submitAttendance());

        footer.add(countSummary);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(btnCancel);
        footer.add(btnSubmit);
        add(footer, BorderLayout.SOUTH);
    }

    private void setAllAttendance(boolean present) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(present, i, 0);
        }
    }

    private void submitAttendance() {
        String dateStr = txtDate.getText().trim();
        LocalDate recordDate;
        try {
            recordDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullSubject = allocation.getSubjectCode() + " – " + allocation.getSubjectName();
        int presentCount = 0;
        int absentCount = 0;
        int skippedCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isPresent = (Boolean) tableModel.getValueAt(i, 0);
            String roll = (String) tableModel.getValueAt(i, 1);
            String name = (String) tableModel.getValueAt(i, 2);

            if (Boolean.TRUE.equals(isPresent)) {
                try {
                    LocalDateTime stamp = LocalDateTime.of(recordDate, LocalTime.now());
                    AttendanceRecord requested = new AttendanceRecord(
                            java.util.UUID.randomUUID().toString(),
                            roll,
                            fullSubject,
                            facultyUser.getFullName(),
                            stamp
                    );
                    AttendanceRecord saved = attendanceRepo.save(requested);
                    if (requested.getId().equals(saved.getId())) {
                        presentCount++;
                        // Send notification
                        try {
                            StudentNotification notice = StudentNotification.create(
                                    NotificationType.ATTENDANCE,
                                    roll,
                                    "Attendance Recorded: " + fullSubject,
                                    "You were marked Present for " + fullSubject + " (" + cmbPeriod.getSelectedItem() + ") on " + recordDate + ".",
                                    facultyUser.getFullName()
                            );
                            notificationRepo.save(notice);
                        } catch (Exception ignored) {}
                    } else {
                        skippedCount++; // already marked today
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error saving attendance for " + name + ": " + e.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                absentCount++;
            }
        }

        String summary = "✅ Attendance recording completed for " + allocation.getSubjectCode() + "!\n\n"
                + "• Target Class: " + allocation.getDepartment() + " (Sem " + allocation.getSemester() + " " + allocation.getBatch() + ")\n"
                + "• Date: " + recordDate + " | " + cmbPeriod.getSelectedItem() + "\n"
                + "• Present Recorded: " + presentCount + "\n"
                + "• Absent: " + absentCount + "\n"
                + (skippedCount > 0 ? "• Note: " + skippedCount + " student(s) already had attendance logged today.\n" : "");

        JOptionPane.showMessageDialog(this, summary, "Attendance Submission Successful", JOptionPane.INFORMATION_MESSAGE);

        if (onAttendanceSaved != null) {
            onAttendanceSaved.run();
        }
        dispose();
    }
}

