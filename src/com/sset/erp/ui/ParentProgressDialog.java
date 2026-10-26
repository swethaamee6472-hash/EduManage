package com.sset.erp.ui;

import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.AcademicPerformanceSummary;
import com.sset.erp.model.ParentUser;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only attendance and marks view for a parent's linked student.
 */
public class ParentProgressDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ParentUser parent;
    private final DefaultTableModel attendanceModel = new DefaultTableModel(
            new Object[] {"Date & Time", "Subject", "Marked By"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel assessmentModel = new DefaultTableModel(
            new Object[] {"Subject", "Assessment", "Score", "Maximum", "Result", "Grade",
                "Credits", "Faculty", "Recorded"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel attendanceSummary = new JLabel();
    private final JLabel academicSummary = new JLabel();

    public ParentProgressDialog(JFrame owner, ParentUser parent) {
        super(owner, "Ward Attendance & Academic Progress", true);
        this.parent = parent;
        initUI();
        refreshProgress();
    }

    private void initUI() {
        setSize(950, 580);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Progress for Ward: " + parent.getStudentRollNumber(), SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        JTable attendanceTable = new JTable(attendanceModel);
        attendanceTable.setRowHeight(28);
        JTable assessmentTable = new JTable(assessmentModel);
        assessmentTable.setRowHeight(28);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Attendance", new JScrollPane(attendanceTable));
        tabs.addTab("Marks, Grades & CGPA", new JScrollPane(assessmentTable));
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        add(tabs, BorderLayout.CENTER);

        attendanceSummary.setFont(UITheme.FONT_BODY_BOLD);
        attendanceSummary.setForeground(UITheme.TEXT_PRIMARY);
        academicSummary.setFont(UITheme.FONT_BODY_BOLD);
        academicSummary.setForeground(UITheme.TEXT_PRIMARY);
        JPanel summaries = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        summaries.add(attendanceSummary);
        summaries.add(academicSummary);

        com.sset.erp.ui.components.ModernButton refresh =
                new com.sset.erp.ui.components.ModernButton("Refresh", com.sset.erp.ui.components.ModernButton.Variant.OUTLINE);
        com.sset.erp.ui.components.ModernButton close =
                new com.sset.erp.ui.components.ModernButton("Close", com.sset.erp.ui.components.ModernButton.Variant.OUTLINE);
        refresh.addActionListener(e -> refreshProgress());
        close.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        footer.add(summaries, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(refresh);
        actions.add(close);
        footer.add(actions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshProgress() {
        try {
            List<AttendanceRecord> attendance = new FileAttendanceRepository()
                    .findByRollNumber(parent.getStudentRollNumber());
            List<AssessmentRecord> assessments = new FileAssessmentRepository()
                    .findByRollNumber(parent.getStudentRollNumber());

            attendanceModel.setRowCount(0);
            for (AttendanceRecord record : attendance) {
                attendanceModel.addRow(new Object[] {
                        DATE_FORMAT.format(record.getRecordedAt()), record.getSubject(), record.getFacultyName()
                });
            }

            assessmentModel.setRowCount(0);
            for (AssessmentRecord record : assessments) {
                assessmentModel.addRow(new Object[] {
                        record.getSubject(), record.getAssessmentName(), formatNumber(record.getScore()),
                        formatNumber(record.getMaximumScore()), String.format("%.1f%%", record.getPercentage()),
                        record.getGrade(), record.getSubjectCredits(), record.getFacultyName(),
                        DATE_FORMAT.format(record.getRecordedAt())
                });
            }

            AcademicPerformanceSummary academic = AcademicPerformanceSummary.from(assessments);
            attendanceSummary.setText(attendance.size() + " attendance record"
                    + (attendance.size() == 1 ? "" : "s")
                    + " recorded. Total scheduled classes are not tracked, so an attendance percentage is unavailable.");
            academicSummary.setText(assessments.isEmpty()
                    ? assessments.size() + " assessment" + (assessments.size() == 1 ? "" : "s")
                            + "; no marks recorded yet."
                    : String.format("CGPA: %.2f / 10 across %d credits | Overall marks: %.1f%%.",
                            academic.getCgpa(), academic.getTotalCredits(), academic.getOverallPercentage()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load ward progress.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }
}
