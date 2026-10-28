package com.sset.erp.ui;

import com.sset.erp.model.Subject;
import com.sset.erp.model.SubjectAllocation;
import com.sset.erp.model.TimetableEntry;
import com.sset.erp.repository.FileSubjectAllocationRepository;
import com.sset.erp.repository.FileSubjectRepository;
import com.sset.erp.repository.FileTimetableRepository;
import com.sset.erp.repository.SubjectAllocationRepository;
import com.sset.erp.repository.SubjectRepository;
import com.sset.erp.repository.TimetableRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
 * Student and Parent dialog providing weekly class schedule, today's lecture schedule, and enrolled course catalogue.
 */
public class StudentTimetableDialog extends JDialog {
    private final String department;
    private final int semester;
    private final String batch;
    private final String studentRollNumber;
    private final String studentName;

    private final TimetableRepository timetableRepository = new FileTimetableRepository();
    private final SubjectRepository subjectRepository = new FileSubjectRepository();
    private final SubjectAllocationRepository allocationRepository = new FileSubjectAllocationRepository();

    private final DefaultTableModel scheduleGridModel;
    private final JTable scheduleGridTable;

    private final DefaultTableModel courseTableModel;
    private final JTable courseTable;

    private final JPanel todaySchedulePanel = new JPanel();

    public StudentTimetableDialog(JFrame owner, String department, int semester, String batch,
                                  String studentRollNumber, String studentName) {
        super(owner, "Class Timetable & Enrolled Courses - " + (studentRollNumber != null ? studentRollNumber : batch), true);
        this.department = department;
        this.semester = semester;
        this.batch = batch;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;

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

        this.courseTableModel = new DefaultTableModel(
                new Object[]{"Course Code", "Subject Name", "Credits", "Type", "Assigned Faculty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        this.courseTable = new JTable(courseTableModel);

        initUI();
        loadWeeklyTimetable();
        loadTodaySchedule();
        loadEnrolledCourses();
    }

    private void initUI() {
        setSize(1140, 660);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 6, 20));

        JLabel heading = new JLabel("Academic Timetable & Course Schedule", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subHeading = new JLabel(studentName + " (" + studentRollNumber + ")  |  "
                + department + "  |  Semester " + semester + "  |  Batch: " + batch);
        subHeading.setFont(UITheme.FONT_SUBHEADER);
        subHeading.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(heading, BorderLayout.NORTH);
        headerPanel.add(subHeading, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);

        tabs.addTab("📅 Weekly Timetable Grid", createGridPanel());
        tabs.addTab("⏰ Today's Lectures", createTodayPanel());
        tabs.addTab("📚 Enrolled Subjects & Faculty", createCoursesPanel());

        add(tabs, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        ModernButton btnClose = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        btnClose.addActionListener(e -> dispose());
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        scheduleGridTable.setRowHeight(74);
        scheduleGridTable.setFont(UITheme.FONT_BODY);
        scheduleGridTable.setShowGrid(true);
        scheduleGridTable.setGridColor(UITheme.BORDER);
        scheduleGridTable.setCellSelectionEnabled(true);
        scheduleGridTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = scheduleGridTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setFont(UITheme.FONT_BODY_BOLD);

        scheduleGridTable.setDefaultRenderer(Object.class, new StudentScheduleRenderer());
        panel.add(new JScrollPane(scheduleGridTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTodayPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        todaySchedulePanel.setLayout(new BoxLayout(todaySchedulePanel, BoxLayout.Y_AXIS));
        todaySchedulePanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(todaySchedulePanel);
        scroll.setBorder(null);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        courseTable.setRowHeight(32);
        courseTable.setFont(UITheme.FONT_BODY);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadWeeklyTimetable() {
        scheduleGridModel.setRowCount(0);
        try {
            List<TimetableEntry> entries = timetableRepository.findByDepartmentSemesterAndBatch(department, semester, batch);
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
                                + "<span style='font-size:9px; color:#334155;'>" + e.getSubjectName() + "</span><br>"
                                + "<span style='font-size:9px; color:#475569;'>" + e.getFacultyName() + "</span><br>"
                                + "<span style='font-size:8px; color:#15803d; background:#dcfce7; padding:1px 3px;'>📍 " + e.getRoomNumber() + "</span>"
                                + "</div></html>";
                    } else {
                        row[p] = "<html><center><span style='color:#94a3b8; font-style:italic;'>[ Free ]</span></center></html>";
                    }
                }
                scheduleGridModel.addRow(row);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load timetable: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTodaySchedule() {
        todaySchedulePanel.removeAll();
        String todayDay = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        JLabel lblDay = new JLabel("Today's Schedule: " + todayDay + " (" + LocalDate.now() + ")");
        lblDay.setFont(UITheme.FONT_HEADER);
        lblDay.setForeground(UITheme.PRIMARY);
        lblDay.setAlignmentX(Component.LEFT_ALIGNMENT);
        todaySchedulePanel.add(lblDay);
        todaySchedulePanel.add(Box.createVerticalStrut(14));

        try {
            List<TimetableEntry> entries = timetableRepository.findByDepartmentSemesterAndBatch(department, semester, batch);
            List<TimetableEntry> todayEntries = entries.stream()
                    .filter(e -> e.getDayOfWeek().equalsIgnoreCase(todayDay))
                    .sorted((a, b) -> Integer.compare(a.getPeriodNumber(), b.getPeriodNumber()))
                    .toList();

            if (todayEntries.isEmpty()) {
                JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
                card.setBackground(UITheme.CARD_BG);
                card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel noClass = new JLabel("🎉 No regular lectures scheduled for today (" + todayDay + ").");
                noClass.setFont(UITheme.FONT_BODY_BOLD);
                noClass.setForeground(UITheme.TEXT_SECONDARY);
                card.add(noClass);
                todaySchedulePanel.add(card);
            } else {
                for (TimetableEntry entry : todayEntries) {
                    JPanel slotCard = new JPanel(new GridLayout(1, 4, 16, 0));
                    slotCard.setBackground(UITheme.CARD_BG);
                    slotCard.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(12, 16, 12, 16)
                    ));
                    slotCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
                    slotCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JLabel lblPeriod = new JLabel("Period " + entry.getPeriodNumber() + "\n(" + entry.getTimeSlot() + ")");
                    lblPeriod.setFont(UITheme.FONT_BODY_BOLD);
                    lblPeriod.setForeground(UITheme.PRIMARY);

                    JLabel lblSubject = new JLabel("<html><b>" + entry.getSubjectCode() + "</b><br>" + entry.getSubjectName() + "</html>");
                    lblSubject.setFont(UITheme.FONT_BODY);

                    JLabel lblFaculty = new JLabel("Instructor: " + entry.getFacultyName());
                    lblFaculty.setFont(UITheme.FONT_BODY);
                    lblFaculty.setForeground(UITheme.TEXT_SECONDARY);

                    JLabel lblRoom = new JLabel("Room: " + entry.getRoomNumber());
                    lblRoom.setFont(UITheme.FONT_BODY_BOLD);
                    lblRoom.setForeground(UITheme.SUCCESS);

                    slotCard.add(lblPeriod);
                    slotCard.add(lblSubject);
                    slotCard.add(lblFaculty);
                    slotCard.add(lblRoom);

                    todaySchedulePanel.add(slotCard);
                    todaySchedulePanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (IOException e) {
            JLabel err = new JLabel("Error loading schedule: " + e.getMessage());
            err.setForeground(UITheme.DANGER);
            todaySchedulePanel.add(err);
        }
        todaySchedulePanel.revalidate();
        todaySchedulePanel.repaint();
    }

    private void loadEnrolledCourses() {
        courseTableModel.setRowCount(0);
        try {
            List<Subject> subjects = subjectRepository.findByDepartmentAndSemester(department, semester);
            List<SubjectAllocation> allocs = allocationRepository.findByDepartmentAndSemester(department, semester);

            for (Subject s : subjects) {
                String facultyName = "To be assigned";
                for (SubjectAllocation a : allocs) {
                    if (a.getSubjectCode().equalsIgnoreCase(s.getCode())) {
                        facultyName = a.getFacultyName();
                        break;
                    }
                }
                courseTableModel.addRow(new Object[]{
                        s.getCode(), s.getName(), s.getCredits(), s.getType(), facultyName
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class StudentScheduleRenderer extends DefaultTableCellRenderer {
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
                if (isSelected) {
                    lbl.setBackground(UITheme.PRIMARY_LIGHT);
                } else {
                    lbl.setBackground(UITheme.CARD_BG);
                }
            }
            return lbl;
        }
    }
}
