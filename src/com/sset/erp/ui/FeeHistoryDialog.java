package com.sset.erp.ui;

import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.FeePayment;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only fee balances and receipt history for one student.
 * Shows a dedicated "Pending Fees" tab listing all unsettled charges with
 * amounts due, due dates, overdue status, and a total outstanding summary.
 */
public class FeeHistoryDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final String rollNumber;
    private final String feeId;
    private final FileFeeRepository repository = new FileFeeRepository();

    // Tab 0 – Pending Fees
    private final DefaultTableModel pendingModel = createModel(
            "Fee Description", "Total Amount", "Paid So Far", "Balance Due", "Due Date", "Status", "Days Overdue");

    // Tab 1 – All Fee Charges
    private final DefaultTableModel chargesModel = createModel(
            "Fee", "Amount", "Due Date", "Paid", "Balance", "Status");

    // Tab 2 – Payment Receipts
    private final DefaultTableModel paymentsModel = createModel(
            "Receipt", "Fee", "Amount", "Method", "Paid At", "Received By");

    private final JLabel summary = new JLabel();
    private final JLabel pendingSummary = new JLabel();

    public FeeHistoryDialog(Window owner, String rollNumber, String title) {
        this(owner, rollNumber, title, null);
    }

    public FeeHistoryDialog(Window owner, String rollNumber, String title, String feeId) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student roll number is required.");
        }
        this.rollNumber = rollNumber.trim();
        this.feeId = feeId;
        initUI();
        refreshHistory();
    }

    private void initUI() {
        setSize(920, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Fee Account: " + rollNumber, SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        // --- Pending Fees Tab ---
        JTable pendingTable = new JTable(pendingModel);
        pendingTable.setRowHeight(30);
        pendingTable.setDefaultRenderer(Object.class, new PendingStatusRenderer());
        JPanel pendingPanel = new JPanel(new BorderLayout(0, 6));
        pendingPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        pendingSummary.setFont(UITheme.FONT_BODY_BOLD);
        pendingSummary.setForeground(UITheme.DANGER);
        pendingSummary.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));
        pendingPanel.add(pendingSummary, BorderLayout.SOUTH);

        // --- All Charges Tab ---
        JTable chargesTable = new JTable(chargesModel);
        chargesTable.setRowHeight(28);

        // --- Payments Tab ---
        JTable paymentsTable = new JTable(paymentsModel);
        paymentsTable.setRowHeight(28);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("⚠️ Pending Fees", pendingPanel);
        tabs.addTab("📋 All Charges", new JScrollPane(chargesTable));
        tabs.addTab("🧾 Payment Receipts", new JScrollPane(paymentsTable));
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        tabs.setSelectedIndex(0); // default to Pending Fees
        add(tabs, BorderLayout.CENTER);

        summary.setFont(UITheme.FONT_BODY_BOLD);
        summary.setForeground(UITheme.TEXT_PRIMARY);
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        bottom.add(summary, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        com.sset.erp.ui.components.ModernButton refresh =
                new com.sset.erp.ui.components.ModernButton("Refresh",
                        com.sset.erp.ui.components.ModernButton.Variant.OUTLINE);
        com.sset.erp.ui.components.ModernButton close =
                new com.sset.erp.ui.components.ModernButton("Close",
                        com.sset.erp.ui.components.ModernButton.Variant.OUTLINE);
        refresh.addActionListener(e -> refreshHistory());
        close.addActionListener(e -> dispose());
        actions.add(refresh);
        actions.add(close);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void refreshHistory() {
        try {
            List<FeeCharge> charges = repository.findChargesByStudent(rollNumber);
            if (feeId != null) {
                charges = charges.stream().filter(charge -> charge.getId().equals(feeId)).toList();
            }
            List<FeePayment> payments = repository.findPaymentsByStudent(rollNumber);
            if (feeId != null) {
                payments = payments.stream().filter(payment -> payment.getFeeId().equals(feeId)).toList();
            }

            chargesModel.setRowCount(0);
            pendingModel.setRowCount(0);

            BigDecimal totalOutstanding = BigDecimal.ZERO;
            List<FeeCharge> pendingCharges = new ArrayList<>();

            for (FeeCharge charge : charges) {
                BigDecimal paid = repository.getPaidAmount(charge.getId());
                BigDecimal balance = charge.getAmount().subtract(paid);
                totalOutstanding = totalOutstanding.add(balance);

                String status = balance.signum() == 0 ? "Paid"
                        : paid.signum() > 0 ? "Partially paid" : "Outstanding";

                // All charges tab
                chargesModel.addRow(new Object[] {charge.getDescription(),
                        FeeManagementDialog.formatMoney(charge.getAmount()), charge.getDueDate(),
                        FeeManagementDialog.formatMoney(paid), FeeManagementDialog.formatMoney(balance), status});

                // Pending tab: only show unsettled charges
                if (balance.signum() > 0) {
                    pendingCharges.add(charge);
                    LocalDate dueDate = charge.getDueDate();
                    long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                    String pendingStatus = paid.signum() > 0 ? "Partially paid"
                            : daysOverdue > 0 ? "Overdue" : "Unpaid";
                    String overdueText = daysOverdue > 0 ? "+" + daysOverdue + " days" : "-";
                    pendingModel.addRow(new Object[] {
                            charge.getDescription(),
                            FeeManagementDialog.formatMoney(charge.getAmount()),
                            FeeManagementDialog.formatMoney(paid),
                            FeeManagementDialog.formatMoney(balance),
                            DATE_ONLY.format(dueDate),
                            pendingStatus,
                            overdueText
                    });
                }
            }

            paymentsModel.setRowCount(0);
            for (FeePayment payment : payments) {
                String description = charges.stream()
                        .filter(charge -> charge.getId().equals(payment.getFeeId()))
                        .map(FeeCharge::getDescription).findFirst().orElse("Fee");
                paymentsModel.addRow(new Object[] {payment.getReceiptNumber(), description,
                        FeeManagementDialog.formatMoney(payment.getAmount()), payment.getPaymentMethod(),
                        DATE_FORMAT.format(payment.getPaidAt()), payment.getReceivedBy()});
            }

            summary.setText(FeeManagementDialog.formatMoney(totalOutstanding) + " total outstanding"
                    + "  |  " + charges.size() + " fee charge" + (charges.size() == 1 ? "" : "s")
                    + "  |  " + payments.size() + " payment" + (payments.size() == 1 ? "" : "s"));

            if (pendingCharges.isEmpty()) {
                pendingSummary.setText("✅ No pending fees — all charges are settled.");
                pendingSummary.setForeground(UITheme.SUCCESS);
            } else {
                pendingSummary.setText("⚠️  Total Pending: " + FeeManagementDialog.formatMoney(totalOutstanding)
                        + " across " + pendingCharges.size() + " charge" + (pendingCharges.size() == 1 ? "" : "s"));
                pendingSummary.setForeground(UITheme.DANGER);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load fee history.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Colors rows in the Pending Fees table by status. */
    private static class PendingStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object statusObj = table.getModel().getValueAt(row, 5); // Status column = index 5
                String status = statusObj == null ? "" : statusObj.toString();
                Color bg;
                Color fg;
                switch (status) {
                    case "Overdue" -> { bg = UITheme.DANGER_BG; fg = UITheme.DANGER; }
                    case "Partially paid" -> { bg = new Color(219, 234, 254); fg = UITheme.INFO; }
                    default -> { bg = UITheme.WARNING_BG; fg = UITheme.WARNING; }
                }
                setBackground(bg);
                setForeground(fg);
            }
            return this;
        }
    }

    private static DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
