package com.sset.erp.ui;

import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.FeePayment;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
import javax.swing.table.DefaultTableModel;

/**
 * Read-only fee balances and receipt history for one student.
 */
public class FeeHistoryDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String rollNumber;
    private final String feeId;
    private final FileFeeRepository repository = new FileFeeRepository();
    private final DefaultTableModel chargesModel = createModel(
            "Fee", "Amount", "Due Date", "Paid", "Balance", "Status");
    private final DefaultTableModel paymentsModel = createModel(
            "Receipt", "Fee", "Amount", "Method", "Paid At", "Received By");
    private final JLabel summary = new JLabel();

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
        setSize(860, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Fee Account: " + rollNumber, SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        JTable chargesTable = new JTable(chargesModel);
        chargesTable.setRowHeight(28);
        JTable paymentsTable = new JTable(paymentsModel);
        paymentsTable.setRowHeight(28);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Fee Charges", new JScrollPane(chargesTable));
        tabs.addTab("Payment Receipts", new JScrollPane(paymentsTable));
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
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
            BigDecimal totalOutstanding = BigDecimal.ZERO;
            for (FeeCharge charge : charges) {
                BigDecimal paid = repository.getPaidAmount(charge.getId());
                BigDecimal balance = charge.getAmount().subtract(paid);
                totalOutstanding = totalOutstanding.add(balance);
                String status = balance.signum() == 0 ? "Paid"
                        : paid.signum() > 0 ? "Partially paid" : "Outstanding";
                chargesModel.addRow(new Object[] {charge.getDescription(),
                        FeeManagementDialog.formatMoney(charge.getAmount()), charge.getDueDate(),
                        FeeManagementDialog.formatMoney(paid), FeeManagementDialog.formatMoney(balance), status});
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
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load fee history.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
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
