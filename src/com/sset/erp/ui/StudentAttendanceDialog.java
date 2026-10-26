package com.sset.erp.ui;

import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.StudentUser;
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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only attendance history for the signed-in student.
 */
public class StudentAttendanceDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentAttendanceDialog(JFrame owner, StudentUser student) {
        super(owner, "My Attendance", true);
        setSize(760, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("My Attendance History", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        try {
            List<AttendanceRecord> records = new FileAttendanceRepository()
                    .findByRollNumber(student.getRollNumber());
            DefaultTableModel model = new DefaultTableModel(
                    new Object[] {"Date & Time", "Subject", "Marked By", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (AttendanceRecord record : records) {
                model.addRow(new Object[] {
                        DATE_FORMAT.format(record.getRecordedAt()), record.getSubject(),
                        record.getFacultyName(), "Present"
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(28);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JLabel summary = new JLabel(records.size() + " attendance record"
                    + (records.size() == 1 ? "" : "s") + " recorded");
            summary.setFont(UITheme.FONT_BODY_BOLD);
            summary.setForeground(UITheme.TEXT_PRIMARY);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
            footer.add(summary);
            add(footer, BorderLayout.SOUTH);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(owner, "Unable to load attendance history.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}
