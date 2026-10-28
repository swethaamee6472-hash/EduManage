package com.sset.erp.ui.dashboard;

import com.sset.erp.model.*;
import com.sset.erp.repository.*;
import com.sset.erp.ui.*;
import com.sset.erp.ui.components.BadgeLabel;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.ui.components.ModernTextField;
import com.sset.erp.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

/**
 * Linways-style Faculty Dashboard Panel.
 * Gives teachers full access to:
 *  1. "🏛️ My Advisory Class": View and mentor their designated class (student roster, overall attendance, CGPA, dossiers).
 *  2. "📚 Subject Classes": Select and manage subjects taught in other classes and their own class (enrolled students, attendance sheets, marks).
 *  3. "⚡ Academic Tools & Profile": Direct access to QR scanners, grade management, timetable, notices.
 */
public class FacultyDashboardPanel extends JPanel {
    private final FacultyUser facultyUser;

    private final MysqlUserRepository userRepo = new MysqlUserRepository();
    private final FileSubjectAllocationRepository allocRepo = new FileSubjectAllocationRepository();
    private final FileAttendanceRepository attendanceRepo = new FileAttendanceRepository();
    private final FileAssessmentRepository assessmentRepo = new FileAssessmentRepository();
    private final FileStudentNotificationRepository notificationRepo = new FileStudentNotificationRepository();

    // Cache of allocations for this faculty
    private List<SubjectAllocation> myAllocations = new ArrayList<>();
    private SubjectAllocation currentSelectedAllocation = null;

    // UI Components for Tab 1 (My Advisory Class)
    private DefaultTableModel ownClassTableModel;
    private JTable ownClassTable;
    private JLabel lblOwnClassStrength;
    private JLabel lblOwnClassAvgAtt;
    private JLabel lblOwnClassAvgCgpa;
    private JLabel lblOwnClassShortage;
    private JTextField txtOwnClassSearch;
    private JComboBox<String> cmbOwnClassFilter;

    // UI Components for Tab 2 (Subject Classes)
    private JComboBox<SubjectAllocationComboItem> cmbSubjectSelector;
    private DefaultTableModel subjectClassTableModel;
    private JTable subjectClassTable;
    private JLabel lblSubCourseTitle;
    private JLabel lblSubTargetClass;
    private JLabel lblSubAllocationBadge;
    private JLabel lblSubEnrolledCount;
    private JLabel lblSubSessionsHeld;
    private JLabel lblSubAvgAtt;
    private JLabel lblSubAsmtCount;
    private JTextField txtSubClassSearch;

    public FacultyDashboardPanel(FacultyUser facultyUser) {
        this.facultyUser = facultyUser;
        loadAllocations();
        initUI();
    }

    private void loadAllocations() {
        try {
            myAllocations = allocRepo.findByFaculty(facultyUser.getUsername());
            if (myAllocations.isEmpty()) {
                // Check if any allocations exist in repository
                List<SubjectAllocation> all = allocRepo.findAll();
                for (SubjectAllocation a : all) {
                    if (a.getFacultyUsername().equalsIgnoreCase(facultyUser.getUsername())
                            || a.getFacultyName().equalsIgnoreCase(facultyUser.getFullName())) {
                        myAllocations.add(a);
                    }
                }
            }
        } catch (IOException e) {
            myAllocations = new ArrayList<>();
        }
        if (!myAllocations.isEmpty() && currentSelectedAllocation == null) {
            currentSelectedAllocation = myAllocations.get(0);
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 24, 24, 24));

        // Top Welcome Card with Linways Roles
        add(createWelcomeCard(), BorderLayout.NORTH);

        // Center Linways Tabbed Navigation
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(UITheme.FONT_SUBHEADER);
        mainTabs.setBackground(UITheme.CARD_BG);

        mainTabs.addTab("🏛️ My Advisory Class", createOwnClassPanel());
        mainTabs.addTab("📚 Subject Classes (Courses in Other Classes)", createSubjectClassesPanel());
        mainTabs.addTab("⚡ Academic Tools & Profile", createToolsAndProfilePanel());

        add(mainTabs, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Top Welcome Card
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createWelcomeCard() {
        ModernCard welcomeCard = new ModernCard(16);
        welcomeCard.setLayout(new BorderLayout(16, 10));
        welcomeCard.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        headerLeft.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome, " + facultyUser.getFullName());
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT_PRIMARY);

        String advisorSummary = facultyUser.isClassAdvisor()
                ? "Class Advisor: Sem " + facultyUser.getAdvisorSemester() + " " + facultyUser.getAdvisorBatch() + " (" + facultyUser.getAdvisorDepartment() + ")"
                : "No advisory class assigned";

        JLabel lblSub = new JLabel("Department of " + facultyUser.getDepartment() + " • "
                + facultyUser.getDesignation() + "  |  " + advisorSummary);
        lblSub.setFont(UITheme.FONT_SUBHEADER);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        headerLeft.add(lblWelcome);
        headerLeft.add(lblSub);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        headerRight.add(BadgeLabel.forRole(Role.FACULTY));
        if (facultyUser.isClassAdvisor()) {
            JLabel lblAdvBadge = new JLabel("🏛️ Class Advisor");
            lblAdvBadge.setFont(UITheme.FONT_BADGE);
            lblAdvBadge.setForeground(new Color(30, 64, 175));
            lblAdvBadge.setBackground(new Color(219, 234, 254));
            lblAdvBadge.setOpaque(true);
            lblAdvBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                    new EmptyBorder(4, 10, 4, 10)));
            headerRight.add(lblAdvBadge);
        }
        headerRight.add(BadgeLabel.forStatus(facultyUser.isActive()));

        welcomeCard.add(headerLeft, BorderLayout.CENTER);
        welcomeCard.add(headerRight, BorderLayout.SOUTH);
        return welcomeCard;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 1: My Advisory Class (Class Advisor / Tutor View)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createOwnClassPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Advisory Class Header Banner with Action Buttons
        ModernCard classHeaderCard = new ModernCard(16);
        classHeaderCard.setLayout(new BorderLayout(14, 10));
        classHeaderCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setOpaque(false);

        String classTitle = "🏛️ Advisory Class: Semester " + facultyUser.getAdvisorSemester() + " "
                + facultyUser.getAdvisorBatch() + " • " + facultyUser.getAdvisorDepartment();
        JLabel lblClassTitle = new JLabel(classTitle);
        lblClassTitle.setFont(UITheme.FONT_HEADER);
        lblClassTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblAdvisorMeta = new JLabel("Class Advisor: " + facultyUser.getFullName()
                + " (" + facultyUser.getEmployeeId() + ")  |  Linways Class In-Charge Portal");
        lblAdvisorMeta.setFont(UITheme.FONT_BODY);
        lblAdvisorMeta.setForeground(UITheme.TEXT_SECONDARY);

        bannerText.add(lblClassTitle);
        bannerText.add(lblAdvisorMeta);

        JPanel classActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        classActions.setOpaque(false);

        ModernButton btnTimetable = new ModernButton("📅 Class Timetable", ModernButton.Variant.OUTLINE);
        ModernButton btnAnnounce = new ModernButton("📢 Announce to Class", ModernButton.Variant.OUTLINE);
        ModernButton btnRefresh = new ModernButton("Refresh", ModernButton.Variant.GHOST);

        btnTimetable.addActionListener(e -> openClassTimetable(
                facultyUser.getAdvisorDepartment(),
                facultyUser.getAdvisorSemester(),
                facultyUser.getAdvisorBatch()));
        btnAnnounce.addActionListener(e -> announceToClass(
                facultyUser.getAdvisorDepartment(),
                facultyUser.getAdvisorSemester(),
                facultyUser.getAdvisorBatch()));
        btnRefresh.addActionListener(e -> refreshOwnClassData());

        classActions.add(btnTimetable);
        classActions.add(btnAnnounce);
        classActions.add(btnRefresh);

        // Banner text stacked above the action buttons so long labels can never overlap them
        classHeaderCard.add(bannerText, BorderLayout.NORTH);
        classHeaderCard.add(classActions, BorderLayout.SOUTH);

        // KPI Summary Cards: Strength, Avg Attendance %, Avg CGPA, Attendance Warnings (<75%)
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        statsGrid.setOpaque(false);

        lblOwnClassStrength = new JLabel("0");
        lblOwnClassAvgAtt = new JLabel("0.0%");
        lblOwnClassAvgCgpa = new JLabel("0.0");
        lblOwnClassShortage = new JLabel("0");

        statsGrid.add(createKpiCard("Class Strength", lblOwnClassStrength, new Color(79, 70, 229), "👥"));
        statsGrid.add(createKpiCard("Class Attendance Rate", lblOwnClassAvgAtt, UITheme.SUCCESS, "📈"));
        statsGrid.add(createKpiCard("Average Class CGPA", lblOwnClassAvgCgpa, UITheme.PRIMARY, "⭐"));
        statsGrid.add(createKpiCard("Shortage Alerts (<75%)", lblOwnClassShortage, UITheme.DANGER, "⚠️"));

        JPanel topGroup = new JPanel(new BorderLayout(0, 12));
        topGroup.setOpaque(false);
        topGroup.add(classHeaderCard, BorderLayout.NORTH);
        topGroup.add(statsGrid, BorderLayout.CENTER);

        panel.add(topGroup, BorderLayout.NORTH);

        // Center: Students Directory Table & Filter Toolbar
        ModernCard rosterCard = new ModernCard(16);
        rosterCard.setLayout(new BorderLayout(10, 10));
        rosterCard.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel tableToolbar = new JPanel(new BorderLayout(10, 6));
        tableToolbar.setOpaque(false);

        JLabel lblRoster = new JLabel("Class Student Directory & Overall Performance");
        lblRoster.setFont(UITheme.FONT_SUBHEADER);
        lblRoster.setForeground(UITheme.TEXT_PRIMARY);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        filterBar.setOpaque(false);

        txtOwnClassSearch = new JTextField(14);
        txtOwnClassSearch.putClientProperty("JTextField.placeholderText", "Search name / roll...");
        txtOwnClassSearch.setFont(UITheme.FONT_BODY);
        txtOwnClassSearch.setPreferredSize(new Dimension(170, 42));
        txtOwnClassSearch.addActionListener(e -> refreshOwnClassData());

        cmbOwnClassFilter = new JComboBox<>(new String[]{
                "All Students",
                "Attendance Shortage (< 75%)",
                "Good Standing (>= 75%)",
                "High CGPA (>= 8.0)"
        });
        cmbOwnClassFilter.setFont(UITheme.FONT_BODY);
        cmbOwnClassFilter.setPreferredSize(new Dimension(230, 42));
        cmbOwnClassFilter.addActionListener(e -> refreshOwnClassData());

        ModernButton btnSearch = new ModernButton("Filter", ModernButton.Variant.OUTLINE);
        btnSearch.addActionListener(e -> refreshOwnClassData());

        ModernButton btnViewDossier = new ModernButton("View Student Dossier", ModernButton.Variant.PRIMARY);
        btnViewDossier.addActionListener(e -> openSelectedStudentDossier(ownClassTable));

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtOwnClassSearch);
        filterBar.add(cmbOwnClassFilter);
        filterBar.add(btnSearch);
        filterBar.add(btnViewDossier);

        tableToolbar.add(lblRoster, BorderLayout.NORTH);
        tableToolbar.add(filterBar, BorderLayout.CENTER);

        ownClassTableModel = new DefaultTableModel(
                new Object[]{"Roll Number", "Student Name", "Email", "Sessions Attended", "Overall Attendance %", "Status", "CGPA", "User ID"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ownClassTable = new JTable(ownClassTableModel);
        ownClassTable.setRowHeight(32);
        ownClassTable.setFont(UITheme.FONT_BODY);
        ownClassTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ownClassTable.setAutoCreateRowSorter(true);
        ownClassTable.removeColumn(ownClassTable.getColumnModel().getColumn(7)); // Hide User ID column

        // Double click to open dossier
        ownClassTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedStudentDossier(ownClassTable);
                }
            }
        });

        // Center alignment
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        ownClassTable.getColumnModel().getColumn(0).setCellRenderer(center);
        ownClassTable.getColumnModel().getColumn(3).setCellRenderer(center);
        ownClassTable.getColumnModel().getColumn(4).setCellRenderer(center);
        ownClassTable.getColumnModel().getColumn(6).setCellRenderer(center);

        // Status renderer with color badges
        ownClassTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFoc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(v);
                if (val.contains("Eligible") || val.contains("Good")) {
                    l.setForeground(UITheme.SUCCESS);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else if (val.contains("Shortage")) {
                    l.setForeground(UITheme.DANGER);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                }
                return l;
            }
        });

        rosterCard.add(tableToolbar, BorderLayout.NORTH);
        rosterCard.add(new JScrollPane(ownClassTable), BorderLayout.CENTER);

        panel.add(rosterCard, BorderLayout.CENTER);

        refreshOwnClassData();
        return panel;
    }

    private void refreshOwnClassData() {
        if (ownClassTableModel == null) return;
        ownClassTableModel.setRowCount(0);

        String dept = facultyUser.getAdvisorDepartment();
        int sem = facultyUser.getAdvisorSemester();
        String batch = facultyUser.getAdvisorBatch();

        List<StudentUser> classStudents = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(s -> (dept == null || dept.isBlank() || s.getDepartment().equalsIgnoreCase(dept.trim())))
                .filter(s -> (sem <= 0 || s.getSemester() == sem))
                .filter(s -> (batch == null || batch.isBlank() || batch.equalsIgnoreCase("All Batches")
                        || s.getBatch().equalsIgnoreCase(batch.trim()) || s.getBatch().equalsIgnoreCase("All Batches")))
                .sorted(Comparator.comparing(StudentUser::getRollNumber))
                .toList();

        lblOwnClassStrength.setText(String.valueOf(classStudents.size()));

        if (classStudents.isEmpty()) {
            lblOwnClassAvgAtt.setText("0.0%");
            lblOwnClassAvgCgpa.setText("0.0");
            lblOwnClassShortage.setText("0");
            return;
        }

        String filterText = txtOwnClassSearch != null ? txtOwnClassSearch.getText().trim().toLowerCase() : "";
        String filterMode = cmbOwnClassFilter != null ? (String) cmbOwnClassFilter.getSelectedItem() : "All Students";

        double sumAttendance = 0;
        double sumCgpa = 0;
        int shortageCount = 0;
        int evaluatedStudents = 0;

        List<AttendanceRecord> allAttendance;
        List<AssessmentRecord> allAssessments;
        try {
            allAttendance = attendanceRepo.findAll();
            allAssessments = assessmentRepo.findAll();
        } catch (IOException e) {
            allAttendance = Collections.emptyList();
            allAssessments = Collections.emptyList();
        }

        // Compute total class sessions conducted
        Map<String, Set<String>> classSessionsBySubject = new HashMap<>();
        for (AttendanceRecord r : allAttendance) {
            String sessionKey = r.getRecordedAt().toLocalDate() + "::" + r.getFacultyName();
            classSessionsBySubject.computeIfAbsent(cleanSubjectName(r.getSubject()), k -> new HashSet<>()).add(sessionKey);
        }
        int totalConductedSessions = 0;
        for (Set<String> set : classSessionsBySubject.values()) {
            totalConductedSessions += set.size();
        }

        for (StudentUser stu : classStudents) {
            // Calculate student attendance
            int attended = 0;
            for (AttendanceRecord r : allAttendance) {
                if (r.getRollNumber().equalsIgnoreCase(stu.getRollNumber())) {
                    attended++;
                }
            }

            int studentBaseConducted = Math.max(attended, totalConductedSessions);
            if (studentBaseConducted == 0) studentBaseConducted = attended > 0 ? attended : 1;
            double attPct = (attended * 100.0) / studentBaseConducted;
            sumAttendance += attPct;

            if (attPct < 75.0) {
                shortageCount++;
            }

            // Calculate student CGPA
            List<AssessmentRecord> stuAsmts = new ArrayList<>();
            for (AssessmentRecord r : allAssessments) {
                if (r.getRollNumber().equalsIgnoreCase(stu.getRollNumber())) {
                    stuAsmts.add(r);
                }
            }
            double cgpa = stuAsmts.isEmpty() ? 0.0 : AcademicPerformanceSummary.from(stuAsmts).getCgpa();
            if (cgpa > 0) {
                sumCgpa += cgpa;
                evaluatedStudents++;
            }

            // Filtering
            boolean matchesSearch = filterText.isEmpty()
                    || stu.getFullName().toLowerCase().contains(filterText)
                    || stu.getRollNumber().toLowerCase().contains(filterText)
                    || stu.getEmail().toLowerCase().contains(filterText);

            boolean matchesCategory = true;
            if ("Attendance Shortage (< 75%)".equals(filterMode)) {
                matchesCategory = attPct < 75.0;
            } else if ("Good Standing (>= 75%)".equals(filterMode)) {
                matchesCategory = attPct >= 75.0;
            } else if ("High CGPA (>= 8.0)".equals(filterMode)) {
                matchesCategory = cgpa >= 8.0;
            }

            if (matchesSearch && matchesCategory) {
                String statusStr = attPct >= 75.0 ? "Eligible (>=75%)" : "⚠️ Shortage (<75%)";
                ownClassTableModel.addRow(new Object[]{
                        stu.getRollNumber(),
                        stu.getFullName(),
                        stu.getEmail(),
                        attended + " / " + studentBaseConducted,
                        String.format("%.1f%%", attPct),
                        statusStr,
                        String.format("%.2f", cgpa),
                        stu.getId()
                });
            }
        }

        double avgAtt = sumAttendance / classStudents.size();
        double avgCgpa = evaluatedStudents > 0 ? sumCgpa / evaluatedStudents : 0.0;

        lblOwnClassAvgAtt.setText(String.format("%.1f%%", avgAtt));
        lblOwnClassAvgCgpa.setText(String.format("%.2f", avgCgpa));
        lblOwnClassShortage.setText(String.valueOf(shortageCount));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 2: Subject Classes (Courses in Other Classes & Own Class)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createSubjectClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Selector Bar: Linways Course & Class Dropdown
        ModernCard selectorCard = new ModernCard(16);
        selectorCard.setLayout(new BorderLayout(14, 8));
        selectorCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel selectorTop = new JPanel(new BorderLayout(12, 0));
        selectorTop.setOpaque(false);

        JLabel lblSel = new JLabel("Select Course & Class You Teach:");
        lblSel.setFont(UITheme.FONT_BODY_BOLD);
        lblSel.setForeground(UITheme.TEXT_PRIMARY);

        ModernButton btnReloadAlloc = new ModernButton("Refresh Allocations", ModernButton.Variant.OUTLINE);
        btnReloadAlloc.addActionListener(e -> {
            loadAllocations();
            populateSubjectSelector();
            refreshSubjectClassView();
        });

        selectorTop.add(lblSel, BorderLayout.WEST);
        selectorTop.add(btnReloadAlloc, BorderLayout.EAST);

        cmbSubjectSelector = new JComboBox<>();
        cmbSubjectSelector.setFont(UITheme.FONT_BODY);
        cmbSubjectSelector.setPreferredSize(new Dimension(540, 42));
        populateSubjectSelector();

        cmbSubjectSelector.addActionListener(e -> {
            SubjectAllocationComboItem selected = (SubjectAllocationComboItem) cmbSubjectSelector.getSelectedItem();
            if (selected != null) {
                currentSelectedAllocation = selected.allocation;
                refreshSubjectClassView();
            }
        });

        // Label row on top, full-width combo below, so they never overlap
        selectorCard.add(selectorTop, BorderLayout.NORTH);
        selectorCard.add(cmbSubjectSelector, BorderLayout.CENTER);

        // Subject Class Banner Card
        ModernCard courseHeaderCard = new ModernCard(16);
        courseHeaderCard.setLayout(new BorderLayout(16, 10));
        courseHeaderCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel headerDetails = new JPanel(new GridLayout(3, 1, 0, 3));
        headerDetails.setOpaque(false);

        lblSubCourseTitle = new JLabel("Course Code & Title");
        lblSubCourseTitle.setFont(UITheme.FONT_HEADER);
        lblSubCourseTitle.setForeground(UITheme.TEXT_PRIMARY);

        lblSubTargetClass = new JLabel("Target Class: Department • Semester • Batch");
        lblSubTargetClass.setFont(UITheme.FONT_SUBHEADER);
        lblSubTargetClass.setForeground(UITheme.TEXT_SECONDARY);

        lblSubAllocationBadge = new JLabel("Tag: Course Type");
        lblSubAllocationBadge.setFont(UITheme.FONT_SMALL);
        lblSubAllocationBadge.setForeground(UITheme.TEXT_MUTED);

        headerDetails.add(lblSubCourseTitle);
        headerDetails.add(lblSubTargetClass);
        headerDetails.add(lblSubAllocationBadge);

        // Course Action Buttons Toolbar
        JPanel subActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        subActions.setOpaque(false);

        ModernButton btnTakeAttendanceSheet = new ModernButton("📝 Attendance Sheet", ModernButton.Variant.PRIMARY);
        ModernButton btnScanSubjectQR = new ModernButton("📷 Scan QR", ModernButton.Variant.OUTLINE);
        ModernButton btnEnterSubjectMarks = new ModernButton("📊 Enter Marks", ModernButton.Variant.OUTLINE);
        ModernButton btnNotifyClass = new ModernButton("📢 Notify Class", ModernButton.Variant.OUTLINE);
        ModernButton btnClassTimetable = new ModernButton("📅 Timetable", ModernButton.Variant.OUTLINE);

        btnTakeAttendanceSheet.addActionListener(e -> openAttendanceSheetForCurrentSubject());
        btnScanSubjectQR.addActionListener(e -> scanQRForCurrentSubject());
        btnEnterSubjectMarks.addActionListener(e -> openMarksForCurrentSubject());
        btnNotifyClass.addActionListener(e -> notifyCurrentSubjectClass());
        btnClassTimetable.addActionListener(e -> {
            if (currentSelectedAllocation != null) {
                openClassTimetable(
                        currentSelectedAllocation.getDepartment(),
                        currentSelectedAllocation.getSemester(),
                        currentSelectedAllocation.getBatch());
            }
        });

        subActions.add(btnTakeAttendanceSheet);
        subActions.add(btnScanSubjectQR);
        subActions.add(btnEnterSubjectMarks);
        subActions.add(btnNotifyClass);
        subActions.add(btnClassTimetable);

        // Course details stacked above the action toolbar so long text never overlaps buttons
        courseHeaderCard.add(headerDetails, BorderLayout.NORTH);
        courseHeaderCard.add(subActions, BorderLayout.SOUTH);

        // KPI Summary Cards for Subject
        JPanel subKpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        subKpiGrid.setOpaque(false);

        lblSubEnrolledCount = new JLabel("0");
        lblSubSessionsHeld = new JLabel("0");
        lblSubAvgAtt = new JLabel("0.0%");
        lblSubAsmtCount = new JLabel("0");

        subKpiGrid.add(createKpiCard("Students in this Class", lblSubEnrolledCount, new Color(79, 70, 229), "👥"));
        subKpiGrid.add(createKpiCard("Sessions Conducted", lblSubSessionsHeld, UITheme.PRIMARY, "📅"));
        subKpiGrid.add(createKpiCard("Subject Attendance Rate", lblSubAvgAtt, UITheme.SUCCESS, "📊"));
        subKpiGrid.add(createKpiCard("Marks Evaluated", lblSubAsmtCount, new Color(168, 85, 247), "📝"));

        JPanel topGroup = new JPanel(new BorderLayout(0, 10));
        topGroup.setOpaque(false);
        topGroup.add(selectorCard, BorderLayout.NORTH);

        JPanel bannerAndKpi = new JPanel(new BorderLayout(0, 10));
        bannerAndKpi.setOpaque(false);
        bannerAndKpi.add(courseHeaderCard, BorderLayout.NORTH);
        bannerAndKpi.add(subKpiGrid, BorderLayout.CENTER);

        topGroup.add(bannerAndKpi, BorderLayout.CENTER);
        panel.add(topGroup, BorderLayout.NORTH);

        // Center Table: Enrolled Students in this class taking this subject
        ModernCard enrolledCard = new ModernCard(16);
        enrolledCard.setLayout(new BorderLayout(10, 10));
        enrolledCard.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel tableToolbar = new JPanel(new BorderLayout(10, 6));
        tableToolbar.setOpaque(false);

        JLabel lblEnrolledTitle = new JLabel("Enrolled Class Roster & Subject Attendance");
        lblEnrolledTitle.setFont(UITheme.FONT_SUBHEADER);
        lblEnrolledTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        searchBar.setOpaque(false);

        txtSubClassSearch = new JTextField(14);
        txtSubClassSearch.putClientProperty("JTextField.placeholderText", "Search student...");
        txtSubClassSearch.setFont(UITheme.FONT_BODY);
        txtSubClassSearch.setPreferredSize(new Dimension(170, 42));
        txtSubClassSearch.addActionListener(e -> refreshSubjectClassView());

        ModernButton btnFilter = new ModernButton("Search", ModernButton.Variant.OUTLINE);
        btnFilter.addActionListener(e -> refreshSubjectClassView());

        ModernButton btnDossier = new ModernButton("View Dossier", ModernButton.Variant.OUTLINE);
        btnDossier.addActionListener(e -> openSelectedStudentDossier(subjectClassTable));

        searchBar.add(new JLabel("Search:"));
        searchBar.add(txtSubClassSearch);
        searchBar.add(btnFilter);
        searchBar.add(btnDossier);

        tableToolbar.add(lblEnrolledTitle, BorderLayout.NORTH);
        tableToolbar.add(searchBar, BorderLayout.CENTER);

        subjectClassTableModel = new DefaultTableModel(
                new Object[]{"Roll Number", "Student Name", "Class / Batch", "Subject Attended", "Subject Attendance %", "Internal Marks", "Grade", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        subjectClassTable = new JTable(subjectClassTableModel);
        subjectClassTable.setRowHeight(32);
        subjectClassTable.setFont(UITheme.FONT_BODY);
        subjectClassTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectClassTable.setAutoCreateRowSorter(true);

        subjectClassTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedStudentDossier(subjectClassTable);
                }
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        subjectClassTable.getColumnModel().getColumn(0).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(2).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(3).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(4).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(5).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(6).setCellRenderer(center);
        subjectClassTable.getColumnModel().getColumn(7).setCellRenderer(center);

        enrolledCard.add(tableToolbar, BorderLayout.NORTH);
        enrolledCard.add(new JScrollPane(subjectClassTable), BorderLayout.CENTER);

        panel.add(enrolledCard, BorderLayout.CENTER);

        refreshSubjectClassView();
        return panel;
    }

    private void populateSubjectSelector() {
        cmbSubjectSelector.removeAllItems();
        if (myAllocations.isEmpty()) {
            return;
        }
        for (SubjectAllocation a : myAllocations) {
            boolean isOwnClass = isAllocationForOwnClass(a);
            String prefix = isOwnClass ? "📌 [My Advisory Class] " : "🌐 [Other Class Course] ";
            String label = prefix + a.getSubjectCode() + " - " + a.getSubjectName()
                    + "  (" + a.getDepartment() + " • Sem " + a.getSemester() + " " + a.getBatch() + ")";
            cmbSubjectSelector.addItem(new SubjectAllocationComboItem(a, label));
        }
        if (currentSelectedAllocation != null) {
            for (int i = 0; i < cmbSubjectSelector.getItemCount(); i++) {
                if (cmbSubjectSelector.getItemAt(i).allocation.getId().equals(currentSelectedAllocation.getId())) {
                    cmbSubjectSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private boolean isAllocationForOwnClass(SubjectAllocation a) {
        if (!facultyUser.isClassAdvisor() || a == null) return false;
        boolean deptMatch = a.getDepartment().equalsIgnoreCase(facultyUser.getAdvisorDepartment().trim());
        boolean semMatch = a.getSemester() == facultyUser.getAdvisorSemester();
        boolean batchMatch = a.getBatch().equalsIgnoreCase("All Batches")
                || facultyUser.getAdvisorBatch().equalsIgnoreCase("All Batches")
                || a.getBatch().equalsIgnoreCase(facultyUser.getAdvisorBatch().trim());
        return deptMatch && semMatch && batchMatch;
    }

    private void refreshSubjectClassView() {
        if (currentSelectedAllocation == null) {
            if (lblSubCourseTitle != null) {
                lblSubCourseTitle.setText("No course allocations found");
                lblSubTargetClass.setText("Please request administrator to allocate subjects.");
            }
            return;
        }

        boolean isOwnClass = isAllocationForOwnClass(currentSelectedAllocation);

        lblSubCourseTitle.setText(currentSelectedAllocation.getSubjectCode() + " – " + currentSelectedAllocation.getSubjectName());
        lblSubTargetClass.setText("Class: " + currentSelectedAllocation.getDepartment() + " • Semester "
                + currentSelectedAllocation.getSemester() + " • Batch " + currentSelectedAllocation.getBatch()
                + "  |  Academic Year: " + currentSelectedAllocation.getAcademicYear());

        if (isOwnClass) {
            lblSubAllocationBadge.setText("📌 Advisory Class Course (You are the Class Advisor of this class)");
            lblSubAllocationBadge.setForeground(new Color(21, 128, 61));
        } else {
            lblSubAllocationBadge.setText("🌐 Other Class Course (You are the Course Faculty teaching in another class)");
            lblSubAllocationBadge.setForeground(new Color(30, 64, 175));
        }

        // Query students belonging to THIS target class
        String dept = currentSelectedAllocation.getDepartment();
        int sem = currentSelectedAllocation.getSemester();
        String batch = currentSelectedAllocation.getBatch();

        List<StudentUser> enrolledStudents = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(s -> (dept == null || dept.isBlank() || s.getDepartment().equalsIgnoreCase(dept.trim())))
                .filter(s -> (sem <= 0 || s.getSemester() == sem))
                .filter(s -> (batch == null || batch.isBlank() || batch.equalsIgnoreCase("All Batches")
                        || s.getBatch().equalsIgnoreCase(batch.trim()) || s.getBatch().equalsIgnoreCase("All Batches")))
                .sorted(Comparator.comparing(StudentUser::getRollNumber))
                .toList();

        lblSubEnrolledCount.setText(String.valueOf(enrolledStudents.size()));
        subjectClassTableModel.setRowCount(0);

        String search = txtSubClassSearch != null ? txtSubClassSearch.getText().trim().toLowerCase() : "";

        // Query attendance records for this subject
        List<AttendanceRecord> allAtt;
        List<AssessmentRecord> allAsmt;
        try {
            allAtt = attendanceRepo.findAll();
            allAsmt = assessmentRepo.findAll();
        } catch (IOException e) {
            allAtt = Collections.emptyList();
            allAsmt = Collections.emptyList();
        }

        String subCode = currentSelectedAllocation.getSubjectCode().toUpperCase();
        String subName = currentSelectedAllocation.getSubjectName().toLowerCase();

        // Count sessions conducted for this subject
        Set<String> subjectSessions = new HashSet<>();
        for (AttendanceRecord r : allAtt) {
            String rSub = r.getSubject() == null ? "" : r.getSubject();
            if (rSub.toUpperCase().contains(subCode) || rSub.toLowerCase().contains(subName)) {
                subjectSessions.add(r.getRecordedAt().toLocalDate() + "::" + r.getFacultyName());
            }
        }
        int sessionsHeld = subjectSessions.size();
        lblSubSessionsHeld.setText(String.valueOf(sessionsHeld));

        double totalSubjectAttPct = 0;
        int asmtCount = 0;

        for (StudentUser stu : enrolledStudents) {
            // Count this student's attendance in this subject
            int studentAttended = 0;
            for (AttendanceRecord r : allAtt) {
                if (r.getRollNumber().equalsIgnoreCase(stu.getRollNumber())) {
                    String rSub = r.getSubject() == null ? "" : r.getSubject();
                    if (rSub.toUpperCase().contains(subCode) || rSub.toLowerCase().contains(subName)) {
                        studentAttended++;
                    }
                }
            }

            int baseConducted = Math.max(studentAttended, sessionsHeld);
            if (baseConducted == 0) baseConducted = studentAttended > 0 ? studentAttended : 1;
            double attPct = (studentAttended * 100.0) / baseConducted;
            totalSubjectAttPct += attPct;

            // Find internal marks for this student in this subject
            String latestMark = "—";
            String latestGrade = "—";
            for (AssessmentRecord r : allAsmt) {
                if (r.getRollNumber().equalsIgnoreCase(stu.getRollNumber())) {
                    String rSub = r.getSubject() == null ? "" : r.getSubject();
                    if (rSub.toUpperCase().contains(subCode) || rSub.toLowerCase().contains(subName)) {
                        latestMark = String.format("%.1f / %.1f", r.getScore(), r.getMaximumScore());
                        latestGrade = r.getGrade();
                        asmtCount++;
                    }
                }
            }

            boolean matches = search.isEmpty()
                    || stu.getFullName().toLowerCase().contains(search)
                    || stu.getRollNumber().toLowerCase().contains(search);

            if (matches) {
                String status = attPct >= 75.0 ? "Eligible" : "Shortage (<75%)";
                subjectClassTableModel.addRow(new Object[]{
                        stu.getRollNumber(),
                        stu.getFullName(),
                        "Sem " + stu.getSemester() + " (" + stu.getBatch() + ")",
                        studentAttended + " / " + baseConducted,
                        String.format("%.1f%%", attPct),
                        latestMark,
                        latestGrade,
                        status
                });
            }
        }

        double avgAtt = enrolledStudents.isEmpty() ? 0.0 : totalSubjectAttPct / enrolledStudents.size();
        lblSubAvgAtt.setText(String.format("%.1f%%", avgAtt));
        lblSubAsmtCount.setText(String.valueOf(asmtCount));
    }

    private void openAttendanceSheetForCurrentSubject() {
        if (currentSelectedAllocation == null) {
            JOptionPane.showMessageDialog(this, "Please select an allocated course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dept = currentSelectedAllocation.getDepartment();
        int sem = currentSelectedAllocation.getSemester();
        String batch = currentSelectedAllocation.getBatch();

        List<StudentUser> enrolled = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(s -> (dept == null || dept.isBlank() || s.getDepartment().equalsIgnoreCase(dept.trim())))
                .filter(s -> (sem <= 0 || s.getSemester() == sem))
                .filter(s -> (batch == null || batch.isBlank() || batch.equalsIgnoreCase("All Batches")
                        || s.getBatch().equalsIgnoreCase(batch.trim()) || s.getBatch().equalsIgnoreCase("All Batches")))
                .sorted(Comparator.comparing(StudentUser::getRollNumber))
                .toList();

        if (enrolled.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found enrolled in " + currentSelectedAllocation.getDepartment()
                    + " Sem " + currentSelectedAllocation.getSemester() + " (" + currentSelectedAllocation.getBatch() + ")",
                    "No Enrolled Students", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ClassAttendanceSheetDialog dialog = new ClassAttendanceSheetDialog(
                SwingUtilities.getWindowAncestor(this),
                facultyUser,
                currentSelectedAllocation,
                enrolled,
                () -> {
                    refreshSubjectClassView();
                    refreshOwnClassData();
                }
        );
        dialog.setVisible(true);
    }

    private void scanQRForCurrentSubject() {
        if (currentSelectedAllocation == null) {
            scanStudentQR(); // Fallback to general scanner
            return;
        }
        LiveWebcamQRScanner scanner = new LiveWebcamQRScanner(decodedText -> {
            if (decodedText != null && !decodedText.isBlank()) {
                if (decodedText.startsWith("STUDENT_ID:")) {
                    String roll = decodedText.substring("STUDENT_ID:".length()).trim();
                    markAttendanceForRollAndSubject(roll, currentSelectedAllocation);
                } else {
                    JOptionPane.showMessageDialog(FacultyDashboardPanel.this,
                            "Invalid QR Code! This is not a valid Student QR.",
                            "Verification Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        scanner.setVisible(true);
    }

    private void markAttendanceForRollAndSubject(String rollNumber, SubjectAllocation allocation) {
        StudentUser student = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(candidate -> candidate.getRollNumber().equalsIgnoreCase(rollNumber))
                .findFirst()
                .orElse(null);

        if (student == null) {
            JOptionPane.showMessageDialog(this, "No student account matches roll number: " + rollNumber,
                    "Student Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if student belongs to this class
        boolean sameClass = student.getDepartment().equalsIgnoreCase(allocation.getDepartment())
                && student.getSemester() == allocation.getSemester();

        if (!sameClass) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "⚠️ Student " + student.getFullName() + " (" + student.getRollNumber() + ")\n"
                            + "belongs to " + student.getDepartment() + " (Sem " + student.getSemester() + " " + student.getBatch() + "),\n"
                            + "but selected course is for " + allocation.getDepartment() + " (Sem " + allocation.getSemester() + " " + allocation.getBatch() + ").\n\n"
                            + "Do you still want to record attendance for " + allocation.getSubjectCode() + "?",
                    "Cross-Class Verification", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String fullSubject = allocation.getSubjectCode() + " – " + allocation.getSubjectName();
        try {
            AttendanceRecord requested = AttendanceRecord.create(
                    student.getRollNumber(), fullSubject, facultyUser.getFullName());
            AttendanceRecord saved = attendanceRepo.save(requested);
            boolean newlyRecorded = requested.getId().equals(saved.getId());

            if (newlyRecorded) {
                try {
                    StudentNotification notification = StudentNotification.create(
                            NotificationType.ATTENDANCE,
                            student.getRollNumber(),
                            "Attendance Recorded: " + saved.getSubject(),
                            "You were marked Present for " + saved.getSubject() + " on "
                                    + saved.getRecordedAt().toLocalDate() + ".",
                            facultyUser.getFullName());
                    notificationRepo.save(notification);
                } catch (IOException ignored) {}
            }

            String msg = newlyRecorded
                    ? "✅ Attendance recorded for " + student.getFullName() + " (" + student.getRollNumber() + ")\n"
                    + "Subject: " + fullSubject + "\n"
                    + "Class: " + allocation.getDepartment() + " Sem " + allocation.getSemester()
                    : "⚠️ Attendance already recorded for " + student.getRollNumber() + " in " + saved.getSubject() + " today.";
            JOptionPane.showMessageDialog(this, msg, "Attendance Recorded", JOptionPane.INFORMATION_MESSAGE);

            refreshSubjectClassView();
            refreshOwnClassData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMarksForCurrentSubject() {
        String sub = currentSelectedAllocation != null
                ? currentSelectedAllocation.getSubjectCode() + " – " + currentSelectedAllocation.getSubjectName()
                : null;
        AssessmentManagementDialog dialog = new AssessmentManagementDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), facultyUser, sub);
        dialog.setVisible(true);
        refreshSubjectClassView();
        refreshOwnClassData();
    }

    private void notifyCurrentSubjectClass() {
        if (currentSelectedAllocation == null) return;
        String defaultMsg = "Class announcement for " + currentSelectedAllocation.getSubjectCode() + " (" + currentSelectedAllocation.getDepartment() + " Sem " + currentSelectedAllocation.getSemester() + "): ";
        String msg = JOptionPane.showInputDialog(this, "Enter announcement for students of this class:", defaultMsg);
        if (msg != null && !msg.isBlank()) {
            try {
                // Find students in this class
                String dept = currentSelectedAllocation.getDepartment();
                int sem = currentSelectedAllocation.getSemester();
                String batch = currentSelectedAllocation.getBatch();

                List<StudentUser> enrolled = userRepo.findAll().stream()
                        .filter(StudentUser.class::isInstance)
                        .map(StudentUser.class::cast)
                        .filter(s -> (dept == null || dept.isBlank() || s.getDepartment().equalsIgnoreCase(dept.trim())))
                        .filter(s -> (sem <= 0 || s.getSemester() == sem))
                        .filter(s -> (batch == null || batch.isBlank() || batch.equalsIgnoreCase("All Batches")
                                || s.getBatch().equalsIgnoreCase(batch.trim()) || s.getBatch().equalsIgnoreCase("All Batches")))
                        .toList();

                for (StudentUser stu : enrolled) {
                    StudentNotification n = StudentNotification.create(
                            NotificationType.ANNOUNCEMENT,
                            stu.getRollNumber(),
                            currentSelectedAllocation.getSubjectCode() + " Notice",
                            msg.trim(),
                            facultyUser.getFullName()
                    );
                    notificationRepo.save(n);
                }
                JOptionPane.showMessageDialog(this, "Announcement dispatched to " + enrolled.size() + " student(s) in "
                        + currentSelectedAllocation.getDepartment() + " Sem " + currentSelectedAllocation.getSemester() + "!",
                        "Dispatched", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to send notices: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 3: Academic Tools & Profile
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createToolsAndProfilePanel() {
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        contentGrid.setOpaque(false);
        contentGrid.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Left: Profile Card
        ModernCard profileCard = new ModernCard(20);
        profileCard.setLayout(new BorderLayout(12, 16));
        profileCard.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel profileTitle = new JLabel("Faculty Profile & Credentials");
        profileTitle.setFont(UITheme.FONT_HEADER);
        profileTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsPanel.setOpaque(false);

        for (Map.Entry<String, String> entry : facultyUser.getProfileDetails().entrySet()) {
            JLabel keyLabel = new JLabel(entry.getKey());
            keyLabel.setFont(UITheme.FONT_BODY_BOLD);
            keyLabel.setForeground(UITheme.TEXT_SECONDARY);

            JLabel valLabel = new JLabel(entry.getValue());
            valLabel.setFont(UITheme.FONT_BODY);
            valLabel.setForeground(UITheme.TEXT_PRIMARY);

            detailsPanel.add(keyLabel);
            detailsPanel.add(valLabel);
        }

        profileCard.add(profileTitle, BorderLayout.NORTH);
        profileCard.add(detailsPanel, BorderLayout.CENTER);

        // Right: Academic Workflow & Module Launcher
        ModernCard workflowCard = new ModernCard(20);
        workflowCard.setLayout(new BorderLayout(12, 16));
        workflowCard.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel workflowTitle = new JLabel("Academic Modules & Quick Tools");
        workflowTitle.setFont(UITheme.FONT_HEADER);
        workflowTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel modulesList = new JPanel(new GridLayout(4, 1, 0, 12));
        modulesList.setOpaque(false);

        modulesList.add(createModuleCard("Scan QR Attendance", "Scan a student's QR Code pass directly", "Module 4", "📷", this::scanStudentQR));
        modulesList.add(createModuleCard("Marks, Grade & CGPA Management",
                "Enter marks, course credits and calculate student CGPA", "Module 5", "📝",
                this::openAssessmentManager));
        modulesList.add(createModuleCard("Student Notifications", "Publish notices to students", "Module 6", "🔔", this::openNotifications));
        modulesList.add(createModuleCard("Teaching Timetable & Courses", "View weekly assigned periods & schedule", "Module 11", "📅", this::openTimetable));

        workflowCard.add(workflowTitle, BorderLayout.NORTH);
        workflowCard.add(modulesList, BorderLayout.CENTER);

        contentGrid.add(profileCard);
        contentGrid.add(workflowCard);
        return contentGrid;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods & Dialog Openers
    // ─────────────────────────────────────────────────────────────────────────
    private void openSelectedStudentDossier(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the list first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String roll = (String) table.getModel().getValueAt(modelRow, 0);

        StudentUser stu = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(s -> s.getRollNumber().equalsIgnoreCase(roll))
                .findFirst()
                .orElse(null);

        if (stu != null) {
            ClassStudentDossierDialog dossier = new ClassStudentDossierDialog(
                    SwingUtilities.getWindowAncestor(this), stu);
            dossier.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Could not load records for roll number: " + roll, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openClassTimetable(String dept, int sem, String batch) {
        StudentTimetableDialog dialog = new StudentTimetableDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), dept, sem, batch,
                "Class Timetable: Sem " + sem + " " + batch, "Class Timetable");
        dialog.setVisible(true);
    }

    private void announceToClass(String dept, int sem, String batch) {
        String msg = JOptionPane.showInputDialog(this,
                "Enter notification message for Advisory Class (" + dept + " Sem " + sem + " " + batch + "):",
                "Class Announcement", JOptionPane.QUESTION_MESSAGE);
        if (msg != null && !msg.isBlank()) {
            List<StudentUser> classStudents = userRepo.findAll().stream()
                    .filter(StudentUser.class::isInstance)
                    .map(StudentUser.class::cast)
                    .filter(s -> (dept == null || dept.isBlank() || s.getDepartment().equalsIgnoreCase(dept.trim())))
                    .filter(s -> (sem <= 0 || s.getSemester() == sem))
                    .filter(s -> (batch == null || batch.isBlank() || batch.equalsIgnoreCase("All Batches")
                            || s.getBatch().equalsIgnoreCase(batch.trim()) || s.getBatch().equalsIgnoreCase("All Batches")))
                    .toList();

            try {
                for (StudentUser stu : classStudents) {
                    StudentNotification n = StudentNotification.create(
                            NotificationType.ANNOUNCEMENT,
                            stu.getRollNumber(),
                            "Notice from Class Advisor (" + facultyUser.getFullName() + ")",
                            msg.trim(),
                            facultyUser.getFullName()
                    );
                    notificationRepo.save(n);
                }
                JOptionPane.showMessageDialog(this, "Announcement dispatched to " + classStudents.size() + " students of your advisory class!",
                        "Announcement Sent", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error sending announcement: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openTimetable() {
        FacultyTimetableDialog dialog = new FacultyTimetableDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), facultyUser);
        dialog.setVisible(true);
    }

    private void openAssessmentManager() {
        AssessmentManagementDialog dialog = new AssessmentManagementDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), facultyUser);
        dialog.setVisible(true);
        refreshSubjectClassView();
        refreshOwnClassData();
    }

    private void openNotifications() {
        FacultyNotificationsDialog dialog = new FacultyNotificationsDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), facultyUser);
        dialog.setVisible(true);
    }

    private void scanStudentQR() {
        LiveWebcamQRScanner scanner = new LiveWebcamQRScanner(decodedText -> {
            if (decodedText != null && !decodedText.isBlank()) {
                if (decodedText.startsWith("STUDENT_ID:")) {
                    markStudentAttendance(decodedText.substring("STUDENT_ID:".length()).trim());
                } else {
                    JOptionPane.showMessageDialog(FacultyDashboardPanel.this,
                            "Invalid QR Code! This is not a valid Student QR.",
                            "Verification Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        scanner.setVisible(true);
    }

    private static final LocalTime[][] PERIOD_BOUNDS = {
            { LocalTime.of( 9,  0), LocalTime.of(10,  0) }, // Period 1
            { LocalTime.of(10,  0), LocalTime.of(11,  0) }, // Period 2
            { LocalTime.of(11, 15), LocalTime.of(12, 15) }, // Period 3
            { LocalTime.of(12, 15), LocalTime.of(13, 15) }, // Period 4
            { LocalTime.of(14,  0), LocalTime.of(15,  0) }, // Period 5
            { LocalTime.of(15,  0), LocalTime.of(16,  0) }, // Period 6
    };

    private static int currentPeriodNumber() {
        LocalTime now = LocalTime.now();
        for (int i = 0; i < PERIOD_BOUNDS.length; i++) {
            if (!now.isBefore(PERIOD_BOUNDS[i][0]) && now.isBefore(PERIOD_BOUNDS[i][1])) {
                return i + 1;
            }
        }
        return -1;
    }

    private TimetableEntry detectCurrentPeriodEntry() {
        int period = currentPeriodNumber();
        if (period < 1) return null;
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String dayName = switch (dow) {
            case MONDAY    -> "Monday";
            case TUESDAY   -> "Tuesday";
            case WEDNESDAY -> "Wednesday";
            case THURSDAY  -> "Thursday";
            case FRIDAY    -> "Friday";
            default        -> null;
        };
        if (dayName == null) return null;

        try {
            List<TimetableEntry> entries = new FileTimetableRepository()
                    .findByFaculty(facultyUser.getUsername());
            for (TimetableEntry e : entries) {
                if (e.getDayOfWeek().equalsIgnoreCase(dayName) && e.getPeriodNumber() == period) {
                    return e;
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    private List<String> todaysSubjects() {
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String dayName = switch (dow) {
            case MONDAY    -> "Monday";
            case TUESDAY   -> "Tuesday";
            case WEDNESDAY -> "Wednesday";
            case THURSDAY  -> "Thursday";
            case FRIDAY    -> "Friday";
            default        -> null;
        };
        List<String> subjects = new ArrayList<>();
        if (dayName == null) return subjects;
        try {
            List<TimetableEntry> entries = new FileTimetableRepository()
                    .findByFaculty(facultyUser.getUsername());
            for (TimetableEntry e : entries) {
                if (e.getDayOfWeek().equalsIgnoreCase(dayName)) {
                    String label = e.getSubjectCode() + " – " + e.getSubjectName()
                            + " (P" + e.getPeriodNumber() + ")";
                    if (!subjects.contains(label)) {
                        subjects.add(label);
                    }
                }
            }
        } catch (IOException ignored) {}
        return subjects;
    }

    private void markStudentAttendance(String rollNumber) {
        StudentUser student = userRepo.findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .filter(candidate -> candidate.getRollNumber().equalsIgnoreCase(rollNumber))
                .findFirst()
                .orElse(null);
        if (student == null) {
            JOptionPane.showMessageDialog(this, "No student account matches roll number: " + rollNumber,
                    "Student Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String subject = null;
        TimetableEntry currentEntry = detectCurrentPeriodEntry();

        if (currentEntry != null) {
            subject = currentEntry.getSubjectCode() + " – " + currentEntry.getSubjectName();
        } else {
            List<String> todayList = todaysSubjects();
            if (!todayList.isEmpty()) {
                String[] options = todayList.toArray(new String[0]);
                Object chosen = JOptionPane.showInputDialog(
                        this,
                        "<html><b>No period is active right now.</b><br>"
                                + "Select the subject for which you want to mark attendance:</html>",
                        "Select Subject",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (chosen == null) return;
                subject = chosen.toString();
            } else if (currentSelectedAllocation != null) {
                subject = currentSelectedAllocation.getSubjectCode() + " – " + currentSelectedAllocation.getSubjectName();
            } else {
                subject = JOptionPane.showInputDialog(
                        this,
                        "No timetable found for today.\nEnter subject name manually:",
                        "Enter Subject",
                        JOptionPane.QUESTION_MESSAGE);
                if (subject == null || subject.trim().isEmpty()) return;
                subject = subject.trim();
            }
        }

        try {
            AttendanceRecord requested = AttendanceRecord.create(
                    student.getRollNumber(), subject, facultyUser.getFullName());
            AttendanceRecord saved = attendanceRepo.save(requested);
            boolean newlyRecorded = requested.getId().equals(saved.getId());
            if (newlyRecorded) {
                try {
                    StudentNotification notification = StudentNotification.create(
                            NotificationType.ATTENDANCE,
                            student.getRollNumber(),
                            "Attendance recorded: " + saved.getSubject(),
                            "You were marked present for " + saved.getSubject() + " on "
                                    + saved.getRecordedAt().toLocalDate() + ".",
                            facultyUser.getFullName());
                    notificationRepo.save(notification);
                } catch (IOException ignored) {}
            }
            String message = newlyRecorded
                    ? "✅ Attendance marked for " + student.getFullName() + " (" + student.getRollNumber() + ")\n"
                    + "Subject: " + subject + "\n"
                    + "Time: " + LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                    : "⚠️ Attendance already recorded for " + student.getRollNumber() + " in " + saved.getSubject() + " today.";
            JOptionPane.showMessageDialog(this, message, "Attendance", JOptionPane.INFORMATION_MESSAGE);

            refreshOwnClassData();
            refreshSubjectClassView();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to save attendance.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(10, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createModuleCard(String title, String desc, String badge, String icon, Runnable action) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.CARD_BG_HOVER);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("SansSerif", Font.PLAIN, 22));

        JPanel textP = new JPanel(new GridLayout(2, 1, 0, 2));
        textP.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_SUBHEADER);
        lblT.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblD = new JLabel(desc);
        lblD.setFont(UITheme.FONT_SMALL);
        lblD.setForeground(UITheme.TEXT_SECONDARY);

        textP.add(lblT);
        textP.add(lblD);

        ModernButton btn = new ModernButton("Open", ModernButton.Variant.OUTLINE);
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });

        p.add(lblIcon, BorderLayout.WEST);
        p.add(textP, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);

        return p;
    }

    private static String cleanSubjectName(String sub) {
        if (sub == null) return "Unknown";
        int dash = sub.indexOf("–");
        if (dash == -1) dash = sub.indexOf("-");
        if (dash != -1) {
            return sub.substring(0, dash).trim();
        }
        return sub.trim();
    }

    private static class SubjectAllocationComboItem {
        final SubjectAllocation allocation;
        final String display;

        SubjectAllocationComboItem(SubjectAllocation allocation, String display) {
            this.allocation = allocation;
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
