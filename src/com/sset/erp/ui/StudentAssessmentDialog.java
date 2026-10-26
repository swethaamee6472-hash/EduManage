package com.sset.erp.ui;

import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.AcademicPerformanceSummary;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileAssessmentRepository;
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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only assessment results for the signed-in student.
 */
public class StudentAssessmentDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentAssessmentDialog(JFrame owner, StudentUser student) {
        super(owner, "My Marks, Grades & CGPA", true);
        setSize(900, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("My Marks, Grades & Academic Performance", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        try {
            List<AssessmentRecord> records = new FileAssessmentRepository()
                    .findByRollNumber(student.getRollNumber());
            DefaultTableModel model = new DefaultTableModel(
                    new Object[] {"Subject", "Assessment", "Score", "Maximum", "Result", "Grade",
                        "Credits", "Faculty", "Recorded"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (AssessmentRecord record : records) {
                model.addRow(new Object[] {
                        record.getSubject(), record.getAssessmentName(), formatNumber(record.getScore()),
                        formatNumber(record.getMaximumScore()), String.format("%.1f%%", record.getPercentage()),
                        record.getGrade(), record.getSubjectCredits(), record.getFacultyName(),
                        DATE_FORMAT.format(record.getRecordedAt())
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(28);
            DefaultTableModel subjectModel = new DefaultTableModel(
                    new Object[] {"Subject", "Overall Result", "Grade", "Grade Point", "Credits"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            AcademicPerformanceSummary summary = AcademicPerformanceSummary.from(records);
            for (AcademicPerformanceSummary.SubjectResult subject : summary.getSubjectResults()) {
                subjectModel.addRow(new Object[] {subject.getSubject(),
                        String.format("%.1f%%", subject.getPercentage()), subject.getGrade(),
                        subject.getGradePoint(), subject.getCredits()});
            }
            JTable subjectTable = new JTable(subjectModel);
            subjectTable.setRowHeight(28);
            javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
            tabs.addTab("Assessment Marks", new JScrollPane(table));
            tabs.addTab("Subject Grades", new JScrollPane(subjectTable));
            add(tabs, BorderLayout.CENTER);

            String average = records.isEmpty() ? "No marks recorded yet"
                    : String.format("CGPA: %.2f / 10  |  Credits: %d  |  Overall marks: %.1f%%",
                            summary.getCgpa(), summary.getTotalCredits(), summary.getOverallPercentage());
            JLabel summaryLabel = new JLabel(average);
            summaryLabel.setFont(UITheme.FONT_BODY_BOLD);
            summaryLabel.setForeground(UITheme.TEXT_PRIMARY);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
            footer.add(summaryLabel);
            add(footer, BorderLayout.SOUTH);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(owner,
                    "Unable to load assessment results.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }
}
