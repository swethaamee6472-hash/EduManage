package com.sset.erp.ui;

import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.SubjectAllocation;
import com.sset.erp.model.TimetableEntry;
import com.sset.erp.repository.FileSubjectAllocationRepository;
import com.sset.erp.repository.FileTimetableRepository;
import com.sset.erp.repository.SubjectAllocationRepository;
import com.sset.erp.repository.TimetableRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Faculty view showing personalized weekly teaching periods and allocated subjects.
 */
public class FacultyTimetableDialog extends JDialog {
    private final FacultyUser facultyUser;
    private final TimetableRepository timetableRepository = new FileTimetableRepository();
    private final SubjectAllocationRepository allocationRepository = new FileSubjectAllocationRepository();

    private final DefaultTableModel scheduleGridModel;
    private final JTable scheduleGridTable;

    private final DefaultTableModel allocTableModel;
    private final JTable allocTable;

    public FacultyTimetableDialog(JFrame owner, FacultyUser facultyUser) {
        super(owner, "Faculty Teaching Timetable & Allocated Courses", true);
        this.facultyUser = facultyUser;

        String[] cols = {
                "Day",
                "<html><b>Period 1</b><br><small>09:00 - 10:00</small></html>",
                "<html><b>Period 2</b><br><small>10:00 - 11:00</small></html>",
                "<html><b>Period 3</b><br><small>11:15 - 12:15</small></html>",
                "<html><b>Period 4</b><br><small>12:15 - 01:15</small></html>",
                "<html><b>Period 5</b><br><small>02:00 - 03:00</small></html>",
                "<html><b>Period 6</b><br><small>03:00 - 04:00</small></html>"
        };
        this.scheduleGridModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.scheduleGridTable = new JTable(scheduleGridModel);

        this.allocTableModel = new DefaultTableModel(
                new Object[]{"Course Code", "Course Name", "Department", "Semester", "Batch", "Academic Year"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.allocTable = new JTable(allocTableModel);

        initUI();
        loadTeachingSchedule();
        loadAllocatedSubjects();
    }

    private void initUI() {
        setSize(1120, 640);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));

        JLabel heading = new JLabel("Teaching Timetable & Subject Allocation", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subHeading = new JLabel("Instructor: " + facultyUser.getFullName() + " (" + facultyUser.getEmployeeId() + ")  |  " + facultyUser.getDepartment());
        subHeading.setFont(UITheme.FONT_SUBHEADER);
        subHeading.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(subHeading, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);

        tabs.addTab("📅 Weekly Teaching Schedule", createSchedulePanel());
        tabs.addTab("📖 My Allocated Subjects", createAllocPanel());

        add(tabs, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        ModernButton btnClose = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        btnClose.addActionListener(e -> dispose());
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        scheduleGridTable.setRowHeight(74);
        scheduleGridTable.setCellSelectionEnabled(true);
        scheduleGridTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleGridTable.setShowGrid(true);
        scheduleGridTable.setGridColor(UITheme.BORDER);

        JTableHeader header = scheduleGridTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setFont(UITheme.FONT_BODY_BOLD);

        scheduleGridTable.setDefaultRenderer(Object.class, new FacultyScheduleRenderer());
        panel.add(new JScrollPane(scheduleGridTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAllocPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        allocTable.setRowHeight(32);
        allocTable.setFont(UITheme.FONT_BODY);
        allocTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(allocTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadTeachingSchedule() {
        scheduleGridModel.setRowCount(0);
        try {
            List<TimetableEntry> entries = timetableRepository.findByFaculty(facultyUser.getUsername());

            Map<String, Map<Integer, TimetableEntry>> dayMap = new HashMap<>();
            for (String day : TimetableEntry.DAYS_OF_WEEK) {
                dayMap.put(day, new HashMap<>());
            }
            for (TimetableEntry e : entries) {
                Map<Integer, TimetableEntry> pMap = dayMap.get(e.getDayOfWeek());
                if (pMap != null) {
                    pMap.put(e.getPeriodNumber(), e);
                }
            }

            for (String day : TimetableEntry.DAYS_OF_WEEK) {
                Object[] row = new Object[7];
                row[0] = day;
                Map<Integer, TimetableEntry> pMap = dayMap.get(day);
                for (int p = 1; p <= 6; p++) {
                    TimetableEntry e = pMap != null ? pMap.get(p) : null;
                    if (e != null) {
                        row[p] = "<html><div style='padding:3px; text-align:center;'>"
                                + "<b style='color:#1a73e8;'>" + e.getSubjectCode() + "</b><br>"
                                + "<span style='font-size:9px; color:#334155;'>Sem " + e.getSemester() + " (" + e.getBatch() + ")</span><br>"
                                + "<span style='font-size:9px; color:#15803d; font-weight:bold;'>📍 " + e.getRoomNumber() + "</span>"
                                + "</div></html>";
                    } else {
                        row[p] = "<html><center><span style='color:#94a3b8; font-style:italic;'>[ Free ]</span></center></html>";
                    }
                }
                scheduleGridModel.addRow(row);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load faculty schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllocatedSubjects() {
        allocTableModel.setRowCount(0);
        try {
            List<SubjectAllocation> list = allocationRepository.findByFaculty(facultyUser.getUsername());
            for (SubjectAllocation a : list) {
                allocTableModel.addRow(new Object[]{
                        a.getSubjectCode(), a.getSubjectName(), a.getDepartment(),
                        a.getSemester(), a.getBatch(), a.getAcademicYear()
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load allocations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class FacultyScheduleRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (column == 0) {
                lbl.setFont(UITheme.FONT_BODY_BOLD);
                lbl.setBackground(UITheme.BG_DARK);
                lbl.setForeground(UITheme.TEXT_PRIMARY);
            } else {
                String str = String.valueOf(value);
                if (str.contains("[ Free ]")) {
                    lbl.setBackground(UITheme.CARD_BG);
                } else {
                    lbl.setBackground(new java.awt.Color(238, 242, 255)); // Soft indigo highlight for assigned teaching periods
                }
            }
            return lbl;
        }
    }
}
