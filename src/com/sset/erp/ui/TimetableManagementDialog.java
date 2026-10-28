package com.sset.erp.ui;

import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.Subject;
import com.sset.erp.model.SubjectAllocation;
import com.sset.erp.model.TimetableConflictException;
import com.sset.erp.model.TimetableEntry;
import com.sset.erp.repository.FileSubjectAllocationRepository;
import com.sset.erp.repository.FileSubjectRepository;
import com.sset.erp.repository.FileTimetableRepository;
import com.sset.erp.repository.MysqlUserRepository;
import com.sset.erp.repository.SubjectAllocationRepository;
import com.sset.erp.repository.SubjectRepository;
import com.sset.erp.repository.TimetableRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Administrator dialog for viewing, generating, editing, and resolving conflicts in weekly timetables.
 */
public class TimetableManagementDialog extends JDialog {
    private final TimetableRepository timetableRepository = new FileTimetableRepository();
    private final SubjectRepository subjectRepository = new FileSubjectRepository();
    private final SubjectAllocationRepository allocationRepository = new FileSubjectAllocationRepository();
    private final List<FacultyUser> facultyList;

    private final JComboBox<String> cmbDepartment = new JComboBox<>(new String[]{
            "Computer Science & Engineering",
            "Electronics & Communication Engineering",
            "Mechanical Engineering",
            "Civil Engineering"
    });
    private final JComboBox<Integer> cmbSemester = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
    private final JTextField txtBatch = new JTextField("2024-2028 (CS4)", 15);

    private final DefaultTableModel gridModel;
    private final JTable gridTable;

    // Cache of current displayed entries: day -> (period -> entry)
    private final Map<String, Map<Integer, TimetableEntry>> slotMap = new HashMap<>();

    public TimetableManagementDialog(JFrame owner) {
        super(owner, "Weekly Timetable & Schedule Management", true);
        this.facultyList = new MysqlUserRepository().findAll().stream()
                .filter(FacultyUser.class::isInstance)
                .map(FacultyUser.class::cast)
                .toList();

        cmbSemester.setSelectedItem(3);

        String[] cols = {
                "Day",
                "<html><b>Period 1</b><br><small>09:00 - 10:00</small></html>",
                "<html><b>Period 2</b><br><small>10:00 - 11:00</small></html>",
                "<html><b>Period 3</b><br><small>11:15 - 12:15</small></html>",
                "<html><b>Period 4</b><br><small>12:15 - 01:15</small></html>",
                "<html><b>Period 5</b><br><small>02:00 - 03:00</small></html>",
                "<html><b>Period 6</b><br><small>03:00 - 04:00</small></html>"
        };

        gridModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        gridTable = new JTable(gridModel);
        initUI();
        refreshTimetable();
    }

    private void initUI() {
        setSize(1180, 680);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(14, 14));

        // Top Header and Filters
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));

        JLabel heading = new JLabel("Weekly Timetable Schedule & Conflict Control", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        topPanel.add(heading, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.add(new JLabel("Department:"));
        filterBar.add(cmbDepartment);
        filterBar.add(new JLabel("Semester:"));
        filterBar.add(cmbSemester);
        filterBar.add(new JLabel("Batch:"));
        filterBar.add(txtBatch);

        ModernButton btnApply = new ModernButton("Load Schedule", ModernButton.Variant.PRIMARY);
        btnApply.addActionListener(e -> refreshTimetable());
        filterBar.add(btnApply);

        topPanel.add(filterBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center: Interactive Timetable Grid
        gridTable.setRowHeight(78);
        gridTable.setCellSelectionEnabled(true);
        gridTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gridTable.setFont(UITheme.FONT_BODY);
        gridTable.setShowGrid(true);
        gridTable.setGridColor(UITheme.BORDER);

        JTableHeader header = gridTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        header.setFont(UITheme.FONT_BODY_BOLD);

        gridTable.setDefaultRenderer(Object.class, new TimetableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(gridTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ModernButton btnAddEdit = new ModernButton("+ Assign / Edit Slot", ModernButton.Variant.PRIMARY);
        btnAddEdit.addActionListener(e -> openSlotEditor());
        ModernButton btnDelete = new ModernButton("🗑 Delete Selected Slot", ModernButton.Variant.DANGER);
        btnDelete.addActionListener(e -> deleteSelectedSlot());
        ModernButton btnSummary = new ModernButton("🖨 View Summary", ModernButton.Variant.OUTLINE);
        btnSummary.addActionListener(e -> showPrintableSummary());

        leftActions.add(btnAddEdit);
        leftActions.add(btnDelete);
        leftActions.add(btnSummary);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ModernButton btnClose = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        btnClose.addActionListener(e -> dispose());
        rightActions.add(btnClose);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        cmbDepartment.addActionListener(e -> refreshTimetable());
        cmbSemester.addActionListener(e -> refreshTimetable());
    }

    private void refreshTimetable() {
        gridModel.setRowCount(0);
        slotMap.clear();
        for (String day : TimetableEntry.DAYS_OF_WEEK) {
            slotMap.put(day, new HashMap<>());
        }

        String dept = (String) cmbDepartment.getSelectedItem();
        int sem = (Integer) cmbSemester.getSelectedItem();
        String batch = txtBatch.getText().trim();

        try {
            List<TimetableEntry> entries = timetableRepository.findByDepartmentSemesterAndBatch(dept, sem, batch);
            for (TimetableEntry entry : entries) {
                Map<Integer, TimetableEntry> daySlots = slotMap.get(entry.getDayOfWeek());
                if (daySlots != null) {
                    daySlots.put(entry.getPeriodNumber(), entry);
                }
            }

            for (String day : TimetableEntry.DAYS_OF_WEEK) {
                Object[] row = new Object[7];
                row[0] = day;
                Map<Integer, TimetableEntry> daySlots = slotMap.get(day);
                for (int p = 1; p <= 6; p++) {
                    TimetableEntry e = daySlots != null ? daySlots.get(p) : null;
                    if (e != null) {
                        row[p] = "<html><div style='padding:4px; text-align:center;'>"
                                + "<b style='color:#1a73e8;'>" + e.getSubjectCode() + "</b><br>"
                                + "<span style='font-size:9px; color:#475569;'>" + e.getSubjectName() + "</span><br>"
                                + "<span style='font-size:9px; color:#0f172a; font-weight:bold;'>" + e.getFacultyName() + "</span><br>"
                                + "<span style='font-size:8px; color:#15803d; background:#dcfce7; padding:1px 4px;'>📍 " + e.getRoomNumber() + "</span>"
                                + "</div></html>";
                    } else {
                        row[p] = "<html><center><span style='color:#94a3b8; font-style:italic;'>[ Free Period ]</span></center></html>";
                    }
                }
                gridModel.addRow(row);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load timetable: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSlotEditor() {
        int selectedRow = gridTable.getSelectedRow();
        int selectedCol = gridTable.getSelectedColumn();

        String initDay = "Monday";
        int initPeriod = 1;

        if (selectedRow >= 0) {
            initDay = (String) gridModel.getValueAt(selectedRow, 0);
        }
        if (selectedCol >= 1 && selectedCol <= 6) {
            initPeriod = selectedCol;
        }

        TimetableEntry existingEntry = null;
        Map<Integer, TimetableEntry> daySlots = slotMap.get(initDay);
        if (daySlots != null) {
            existingEntry = daySlots.get(initPeriod);
        }

        JDialog editor = new JDialog(this, "Schedule Timetable Slot", true);
        editor.setSize(520, 440);
        editor.setLocationRelativeTo(this);
        editor.setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbDay = new JComboBox<>(TimetableEntry.DAYS_OF_WEEK);
        cmbDay.setSelectedItem(initDay);

        JComboBox<Integer> cmbPeriod = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
        cmbPeriod.setSelectedItem(initPeriod);

        JComboBox<String> cmbSub = new JComboBox<>();
        try {
            int sem = (Integer) cmbSemester.getSelectedItem();
            String dept = (String) cmbDepartment.getSelectedItem();
            List<Subject> subjects = subjectRepository.findByDepartmentAndSemester(dept, sem);
            for (Subject s : subjects) {
                cmbSub.addItem(s.getCode() + " - " + s.getName());
            }
        } catch (IOException ignored) {}

        JComboBox<String> cmbFaculty = new JComboBox<>();
        for (FacultyUser f : facultyList) {
            cmbFaculty.addItem(f.getUsername() + " - " + f.getFullName());
        }

        JTextField txtRoom = new JTextField(existingEntry != null ? existingEntry.getRoomNumber() : "LH-302", 12);

        // Pre-fill if editing existing
        if (existingEntry != null) {
            for (int i = 0; i < cmbSub.getItemCount(); i++) {
                if (cmbSub.getItemAt(i).startsWith(existingEntry.getSubjectCode())) {
                    cmbSub.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cmbFaculty.getItemCount(); i++) {
                if (cmbFaculty.getItemAt(i).startsWith(existingEntry.getFacultyUsername())) {
                    cmbFaculty.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Auto-select allocated faculty when subject changes
        cmbSub.addActionListener(e -> {
            if (cmbSub.getSelectedIndex() >= 0) {
                String subStr = (String) cmbSub.getSelectedItem();
                String code = subStr.split(" - ")[0].trim();
                try {
                    List<SubjectAllocation> allocs = allocationRepository.findAll();
                    for (SubjectAllocation a : allocs) {
                        if (a.getSubjectCode().equalsIgnoreCase(code)) {
                            for (int i = 0; i < cmbFaculty.getItemCount(); i++) {
                                if (cmbFaculty.getItemAt(i).startsWith(a.getFacultyUsername())) {
                                    cmbFaculty.setSelectedIndex(i);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                } catch (IOException ignored) {}
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1; form.add(cmbDay, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Period:"), gbc);
        gbc.gridx = 1; form.add(cmbPeriod, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; form.add(cmbSub, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1; form.add(cmbFaculty, gbc);

        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Classroom / Lab:"), gbc);
        gbc.gridx = 1; form.add(txtRoom, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        ModernButton btnCancel = new ModernButton("Cancel", ModernButton.Variant.OUTLINE);
        btnCancel.addActionListener(e -> editor.dispose());

        TimetableEntry finalExisting = existingEntry;
        ModernButton btnSave = new ModernButton("Save Slot", ModernButton.Variant.PRIMARY);
        btnSave.addActionListener(e -> {
            if (cmbSub.getSelectedIndex() < 0 || cmbFaculty.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(editor, "Please select both a subject and instructor.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String day = (String) cmbDay.getSelectedItem();
            int period = (Integer) cmbPeriod.getSelectedItem();
            String subStr = (String) cmbSub.getSelectedItem();
            String[] subParts = subStr.split(" - ", 2);
            String subCode = subParts[0].trim();
            String subName = subParts.length > 1 ? subParts[1].trim() : subCode;

            String facStr = (String) cmbFaculty.getSelectedItem();
            String[] facParts = facStr.split(" - ", 2);
            String facUser = facParts[0].trim();
            String facName = facParts.length > 1 ? facParts[1].trim() : facUser;

            String room = txtRoom.getText().trim();
            if (room.isEmpty()) room = "LH-302";

            String dept = (String) cmbDepartment.getSelectedItem();
            int sem = (Integer) cmbSemester.getSelectedItem();
            String batch = txtBatch.getText().trim();

            String entryId = finalExisting != null ? finalExisting.getId() : java.util.UUID.randomUUID().toString();
            TimetableEntry entry = new TimetableEntry(
                    entryId, day, period, TimetableEntry.getTimeSlotForPeriod(period),
                    dept, sem, batch, subCode, subName, facUser, facName, room, java.time.LocalDateTime.now()
            );

            try {
                timetableRepository.save(entry);
                editor.dispose();
                refreshTimetable();
                JOptionPane.showMessageDialog(this, "Slot scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (TimetableConflictException conflictEx) {
                JOptionPane.showMessageDialog(editor,
                        "Scheduling Conflict Detected:\n\n" + conflictEx.getMessage()
                        + "\n\nPlease select another period, instructor, or room.",
                        "Schedule Conflict (" + conflictEx.getConflictType() + ")", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioEx) {
                JOptionPane.showMessageDialog(editor, "Failed to save: " + ioEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        editor.add(form, BorderLayout.CENTER);
        editor.add(btnRow, BorderLayout.SOUTH);
        editor.setVisible(true);
    }

    private void deleteSelectedSlot() {
        int r = gridTable.getSelectedRow();
        int c = gridTable.getSelectedColumn();
        if (r < 0 || c < 1 || c > 6) {
            JOptionPane.showMessageDialog(this, "Please click on a timetable cell to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String day = (String) gridModel.getValueAt(r, 0);
        int period = c;

        Map<Integer, TimetableEntry> daySlots = slotMap.get(day);
        TimetableEntry entry = daySlots != null ? daySlots.get(period) : null;
        if (entry == null) {
            JOptionPane.showMessageDialog(this, "This slot is already free.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove slot for " + entry.getSubjectCode() + " on " + day + " Period " + period + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                timetableRepository.delete(entry.getId());
                refreshTimetable();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete slot: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPrintableSummary() {
        StringBuilder sb = new StringBuilder();
        String dept = (String) cmbDepartment.getSelectedItem();
        int sem = (Integer) cmbSemester.getSelectedItem();
        String batch = txtBatch.getText().trim();

        sb.append("=========================================================================\n");
        sb.append("                       EDUMANAGE ERP TIMETABLE                           \n");
        sb.append("  Department: ").append(dept).append("\n");
        sb.append("  Semester:   ").append(sem).append("  |  Batch: ").append(batch).append("\n");
        sb.append("=========================================================================\n\n");

        for (String day : TimetableEntry.DAYS_OF_WEEK) {
            sb.append("--- ").append(day.toUpperCase()).append(" ---\n");
            Map<Integer, TimetableEntry> daySlots = slotMap.get(day);
            for (int p = 1; p <= 6; p++) {
                TimetableEntry e = daySlots != null ? daySlots.get(p) : null;
                String time = TimetableEntry.getTimeSlotForPeriod(p);
                if (e != null) {
                    sb.append(String.format("  Period %d (%s): %s [%s] - %s (Room: %s)\n",
                            p, time, e.getSubjectCode(), e.getSubjectName(), e.getFacultyName(), e.getRoomNumber()));
                } else {
                    sb.append(String.format("  Period %d (%s): [Free Period]\n", p, time));
                }
            }
            sb.append("\n");
        }

        javax.swing.JTextArea txt = new javax.swing.JTextArea(sb.toString(), 22, 60);
        txt.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        txt.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(txt), "Timetable Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class TimetableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
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
