package com.sset.erp.ui;

import com.sset.erp.model.AcademicPerformanceSummary;
import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.NotificationType;
import com.sset.erp.model.ParentUser;
import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.repository.MysqlUserRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * Linways-style comprehensive Student Dossier dialog for Class Advisors.
 * Displays subject-wise attendance breakdown, internal marks/grades, and guardian contacts.
 */
public class ClassStudentDossierDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final StudentUser student;
    private final FileAttendanceRepository attendanceRepo = new FileAttendanceRepository();
    private final FileAssessmentRepository assessmentRepo = new FileAssessmentRepository();
    private final MysqlUserRepository userRepo = new MysqlUserRepository();
    private final FileFeeRepository feeRepo = new FileFeeRepository();
    private final FileStudentNotificationRepository notifRepo = new FileStudentNotificationRepository();

    public ClassStudentDossierDialog(Window owner, StudentUser student) {
        super(owner, "Student Academic Dossier - " + student.getFullName() + " (" + student.getRollNumber() + ")", ModalityType.APPLICATION_MODAL);
        this.student = student;
        initUI();
    }

    private void initUI() {
        setSize(980, 690);
        setMinimumSize(new Dimension(840, 560));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout(16, 16));

        // Top Student Profile Header Card
        add(createHeaderCard(), BorderLayout.NORTH);

        // Center Tabs: Attendance Breakdown, Academic Marks, Fee Dues, Guardian & Contact
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEADER);
        tabs.setBackground(UITheme.CARD_BG);

        tabs.addTab("📊 Subject-wise Attendance", createAttendancePanel());
        tabs.addTab("📝 Internal Marks & CGPA", createAssessmentsPanel());
        tabs.addTab("💳 Fee Clearance & Dues", createFeesPanel());
        tabs.addTab("👪 Guardian & Contacts", createGuardianPanel());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerWrapper.add(tabs, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer Action Bar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        ModernButton btnClose = new ModernButton("Close Dossier", ModernButton.Variant.OUTLINE);
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createHeaderCard() {
        ModernCard headerCard = new ModernCard(16);
        headerCard.setLayout(new BorderLayout(20, 12));
        headerCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        JLabel avatarLabel = new JLabel("🎓");
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 42));

        JPanel meta = new JPanel(new GridLayout(3, 1, 0, 2));
        meta.setOpaque(false);

        JLabel nameLabel = new JLabel(student.getFullName());
        nameLabel.setFont(UITheme.FONT_TITLE);
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel classLabel = new JLabel("Roll No: " + student.getRollNumber()
                + "  •  Sem " + student.getSemester() + " " + student.getDepartment()
                + "  •  Batch " + student.getBatch());
        classLabel.setFont(UITheme.FONT_SUBHEADER);
        classLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel emailLabel = new JLabel("Email: " + student.getEmail()
                + "  •  Account Status: " + (student.isActive() ? "Active" : "Disabled"));
        emailLabel.setFont(UITheme.FONT_BODY);
        emailLabel.setForeground(UITheme.TEXT_MUTED);

        meta.add(nameLabel);
        meta.add(classLabel);
        meta.add(emailLabel);

        left.add(avatarLabel);
        left.add(meta);

        // Right side: Overall Attendance & CGPA Badges
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 8));
        right.setOpaque(false);

        double overallAttendance = calculateOverallAttendance();
        double cgpa = calculateCGPA();

        JLabel lblAttBadge = new JLabel(String.format("Attendance: %.1f%%", overallAttendance), SwingConstants.CENTER);
        lblAttBadge.setOpaque(true);
        lblAttBadge.setFont(UITheme.FONT_BODY_BOLD);
        if (overallAttendance >= 75.0) {
            lblAttBadge.setBackground(UITheme.SUCCESS_BG);
            lblAttBadge.setForeground(UITheme.SUCCESS);
        } else {
            lblAttBadge.setBackground(UITheme.DANGER_BG);
            lblAttBadge.setForeground(UITheme.DANGER);
        }
        lblAttBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lblAttBadge.getForeground(), 1, true),
                new EmptyBorder(6, 14, 6, 14)));

        JLabel lblCgpaBadge = new JLabel(String.format("CGPA: %.2f / 10.0", cgpa), SwingConstants.CENTER);
        lblCgpaBadge.setOpaque(true);
        lblCgpaBadge.setFont(UITheme.FONT_BODY_BOLD);
        lblCgpaBadge.setBackground(UITheme.PRIMARY_LIGHT);
        lblCgpaBadge.setForeground(UITheme.PRIMARY);
        lblCgpaBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 1, true),
                new EmptyBorder(6, 14, 6, 14)));

        right.add(lblAttBadge);
        right.add(lblCgpaBadge);

        headerCard.add(left, BorderLayout.CENTER);
        headerCard.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 20, 0, 20));
        wrapper.add(headerCard, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAttendancePanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Course / Subject", "Attended Sessions", "Total Class Sessions", "Attendance %", "Linways Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BODY);

        // Load attendance
        try {
            List<AttendanceRecord> studentAtt = attendanceRepo.findByRollNumber(student.getRollNumber());
            List<AttendanceRecord> allAtt = attendanceRepo.findAll();

            // Group all records by subject to count total sessions conducted across the class
            Map<String, Set<String>> classSessionsBySubject = new HashMap<>();
            for (AttendanceRecord r : allAtt) {
                String sub = cleanSubjectName(r.getSubject());
                String sessionKey = r.getRecordedAt().toLocalDate() + "::" + r.getFacultyName();
                classSessionsBySubject.computeIfAbsent(sub, k -> new HashSet<>()).add(sessionKey);
            }

            // Count student's attendance per subject
            Map<String, Integer> studentCountBySubject = new HashMap<>();
            for (AttendanceRecord r : studentAtt) {
                String sub = cleanSubjectName(r.getSubject());
                studentCountBySubject.put(sub, studentCountBySubject.getOrDefault(sub, 0) + 1);
            }

            // Union of subjects
            Set<String> allSubjects = new TreeSet<>(classSessionsBySubject.keySet());
            allSubjects.addAll(studentCountBySubject.keySet());

            if (allSubjects.isEmpty()) {
                model.addRow(new Object[]{"No attendance records found for this student.", "-", "-", "-", "N/A"});
            } else {
                for (String sub : allSubjects) {
                    int attended = studentCountBySubject.getOrDefault(sub, 0);
                    int totalSessions = Math.max(attended, classSessionsBySubject.getOrDefault(sub, Collections.emptySet()).size());
                    if (totalSessions == 0) totalSessions = attended > 0 ? attended : 1;

                    double pct = (attended * 100.0) / totalSessions;
                    String status = pct >= 75.0 ? "Eligible (>=75%)" : (pct >= 65.0 ? "Condonation Required (65-74%)" : "Attendance Shortage (<65%)");
                    model.addRow(new Object[]{sub, attended, totalSessions, String.format("%.1f%%", pct), status});
                }
            }
        } catch (IOException e) {
            model.addRow(new Object[]{"Error loading attendance records: " + e.getMessage(), "", "", "", ""});
        }

        // Center numeric columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        // Custom status renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasFoc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(v);
                if (val.startsWith("Eligible")) {
                    l.setForeground(UITheme.SUCCESS);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else if (val.contains("Condonation")) {
                    l.setForeground(UITheme.WARNING);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                } else if (val.contains("Shortage")) {
                    l.setForeground(UITheme.DANGER);
                    l.setFont(UITheme.FONT_BODY_BOLD);
                }
                return l;
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel createAssessmentsPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Subject", "Assessment", "Score", "Maximum", "Percentage", "Grade", "Grade Point", "Credits", "Evaluator"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BODY);

        try {
            List<AssessmentRecord> records = assessmentRepo.findByRollNumber(student.getRollNumber());
            if (records.isEmpty()) {
                model.addRow(new Object[]{"No assessment marks recorded yet.", "-", "-", "-", "-", "-", "-", "-", "-"});
            } else {
                for (AssessmentRecord r : records) {
                    model.addRow(new Object[]{
                            r.getSubject(),
                            r.getAssessmentName(),
                            String.format("%.1f", r.getScore()),
                            String.format("%.1f", r.getMaximumScore()),
                            String.format("%.1f%%", r.getPercentage()),
                            r.getGrade(),
                            r.getGradePoint(),
                            r.getSubjectCredits(),
                            r.getFacultyName()
                    });
                }
            }
        } catch (IOException e) {
            model.addRow(new Object[]{"Error loading assessments: " + e.getMessage(), "", "", "", "", "", "", "", ""});
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);
        table.getColumnModel().getColumn(7).setCellRenderer(center);

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel createGuardianPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        ParentUser parent = userRepo.findAll().stream()
                .filter(ParentUser.class::isInstance)
                .map(ParentUser.class::cast)
                .filter(pUser -> pUser.getStudentRollNumber().equalsIgnoreCase(student.getRollNumber()))
                .findFirst()
                .orElse(null);

        JPanel details = new JPanel(new GridLayout(0, 2, 16, 16));
        details.setOpaque(false);

        addDetailRow(details, "Student Full Name:", student.getFullName());
        addDetailRow(details, "Roll Number:", student.getRollNumber());
        addDetailRow(details, "Student Email:", student.getEmail());
        addDetailRow(details, "Department:", student.getDepartment());
        addDetailRow(details, "Semester & Batch:", "Semester " + student.getSemester() + " • " + student.getBatch());

        if (parent != null) {
            addDetailRow(details, "Parent / Guardian Name:", parent.getFullName());
            addDetailRow(details, "Parent Email:", parent.getEmail());
            addDetailRow(details, "Emergency Phone Contact:", parent.getEmergencyContact());
            addDetailRow(details, "Parent Portal Account:", parent.getUsername() + " (" + (parent.isActive() ? "Active" : "Disabled") + ")");
        } else {
            addDetailRow(details, "Parent / Guardian Record:", "No linked parent account found for " + student.getRollNumber());
            addDetailRow(details, "Emergency Contact:", "Institutional Records Desk");
        }

        p.add(details, BorderLayout.NORTH);
        return p;
    }

    private JPanel createFeesPanel() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Fee Description", "Total Amount", "Paid", "Balance Due", "Due Date", "Status", "Overdue Days"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);

        BigDecimal totalPending = BigDecimal.ZERO;
        int pendingCount = 0;
        int totalCharges = 0;

        try {
            List<FeeCharge> charges = feeRepo.findChargesByStudent(student.getRollNumber());
            totalCharges = charges.size();
            for (FeeCharge c : charges) {
                BigDecimal paid = feeRepo.getPaidAmount(c.getId());
                BigDecimal balance = c.getAmount().subtract(paid);
                String status = balance.signum() == 0 ? "Paid"
                        : paid.signum() > 0 ? "Partially paid"
                        : c.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Unpaid";

                long overdueDays = ChronoUnit.DAYS.between(c.getDueDate(), LocalDate.now());
                String overdueStr = overdueDays > 0 ? "+" + overdueDays + " days" : "-";

                if (balance.signum() > 0) {
                    totalPending = totalPending.add(balance);
                    pendingCount++;
                }

                model.addRow(new Object[]{
                        c.getDescription(),
                        FeeManagementDialog.formatMoney(c.getAmount()),
                        FeeManagementDialog.formatMoney(paid),
                        FeeManagementDialog.formatMoney(balance),
                        c.getDueDate().toString(),
                        status,
                        overdueStr
                });
            }
        } catch (IOException e) {
            // ignore
        }

        // Color table rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    Object stObj = tbl.getModel().getValueAt(row, 5);
                    String st = stObj == null ? "" : stObj.toString();
                    if ("Overdue".equals(st)) {
                        setBackground(UITheme.DANGER_BG);
                        setForeground(UITheme.DANGER);
                    } else if ("Partially paid".equals(st)) {
                        setBackground(new Color(219, 234, 254));
                        setForeground(UITheme.INFO);
                    } else if ("Unpaid".equals(st)) {
                        setBackground(UITheme.WARNING_BG);
                        setForeground(UITheme.WARNING);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(UITheme.SUCCESS);
                    }
                }
                return this;
            }
        });

        // Top clearance banner
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(pendingCount > 0 ? UITheme.DANGER : UITheme.SUCCESS, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        banner.setBackground(pendingCount > 0 ? UITheme.DANGER_BG : UITheme.SUCCESS_BG);

        JLabel lblStatus = new JLabel(pendingCount > 0
                ? "⚠️ Fee Dues Pending: INR " + totalPending.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                + " across " + pendingCount + " unsettled fee" + (pendingCount == 1 ? "" : "s")
                : "✅ Fee Clearance Approved — All " + totalCharges + " charges settled. Eligible for Exam Hall Ticket.");
        lblStatus.setFont(UITheme.FONT_SUBHEADER);
        lblStatus.setForeground(pendingCount > 0 ? UITheme.DANGER : UITheme.SUCCESS);
        banner.add(lblStatus, BorderLayout.CENTER);

        if (pendingCount > 0) {
            ModernButton btnRemind = new ModernButton("Send Fee Reminder", ModernButton.Variant.OUTLINE);
            final BigDecimal finalPending = totalPending;
            final int finalCount = pendingCount;
            btnRemind.addActionListener(e -> {
                try {
                    notifRepo.save(StudentNotification.create(
                            NotificationType.FEE,
                            student.getRollNumber(),
                            "Advisory Notice: Pending Fee Clearance",
                            "You have " + finalCount + " unsettled fee charge(s) totaling INR "
                                    + finalPending.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                                    + ". Please clear your dues before the upcoming semester examinations.",
                            "Class Advisor"
                    ));
                    JOptionPane.showMessageDialog(this,
                            "Fee clearance reminder sent to " + student.getFullName() + ".",
                            "Reminder Sent", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Unable to dispatch notification: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            banner.add(btnRemind, BorderLayout.EAST);
        }

        p.add(banner, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        return p;
    }

    private void addDetailRow(JPanel p, String label, String value) {
        JLabel lblK = new JLabel(label);
        lblK.setFont(UITheme.FONT_BODY_BOLD);
        lblK.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblV = new JLabel(value);
        lblV.setFont(UITheme.FONT_BODY);
        lblV.setForeground(UITheme.TEXT_PRIMARY);

        p.add(lblK);
        p.add(lblV);
    }

    private double calculateOverallAttendance() {
        try {
            List<AttendanceRecord> records = attendanceRepo.findByRollNumber(student.getRollNumber());
            List<AttendanceRecord> all = attendanceRepo.findAll();
            if (records.isEmpty()) return 0.0;

            Map<String, Set<String>> classSessionsBySubject = new HashMap<>();
            for (AttendanceRecord r : all) {
                String sub = cleanSubjectName(r.getSubject());
                String sessionKey = r.getRecordedAt().toLocalDate() + "::" + r.getFacultyName();
                classSessionsBySubject.computeIfAbsent(sub, k -> new HashSet<>()).add(sessionKey);
            }

            int totalAttended = records.size();
            int totalConducted = 0;
            for (Set<String> sessions : classSessionsBySubject.values()) {
                totalConducted += sessions.size();
            }
            if (totalConducted < totalAttended) totalConducted = totalAttended;
            return totalConducted > 0 ? (totalAttended * 100.0) / totalConducted : 100.0;
        } catch (IOException e) {
            return 0.0;
        }
    }

    private double calculateCGPA() {
        try {
            List<AssessmentRecord> records = assessmentRepo.findByRollNumber(student.getRollNumber());
            if (records.isEmpty()) return 0.0;
            return AcademicPerformanceSummary.from(records).getCgpa();
        } catch (Exception e) {
            return 0.0;
        }
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
}

