package com.sset.erp.ui;

import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.FeePayment;
import com.sset.erp.model.NotificationType;
import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Administrator interface for assigning charges and recording student payments.
 */
public class FeeManagementDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String adminName;
    private final List<StudentUser> students;
    private final FileFeeRepository repository = new FileFeeRepository();
    private final FileStudentNotificationRepository notificationRepository =
            new FileStudentNotificationRepository();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"Student", "Roll Number", "Fee", "Amount", "Due Date", "Paid", "Balance", "Status", "Fee ID"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public FeeManagementDialog(java.awt.Frame owner, String adminName, List<StudentUser> students) {
        super(owner, "Fee Management", true);
        this.adminName = adminName;
        this.students = List.copyOf(students);
        initUI();
        refreshFees();
    }

    private void initUI() {
        setSize(1080, 590);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Student Fee Ledger", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(8));
        add(new JScrollPane(table), BorderLayout.CENTER);

        ModernButton addCharge = new ModernButton("Add Fee Charge", ModernButton.Variant.PRIMARY);
        ModernButton recordPayment = new ModernButton("Record Payment", ModernButton.Variant.OUTLINE);
        ModernButton viewHistory = new ModernButton("Payment History", ModernButton.Variant.OUTLINE);
        ModernButton refresh = new ModernButton("Refresh", ModernButton.Variant.OUTLINE);
        ModernButton close = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        addCharge.addActionListener(e -> createCharge());
        recordPayment.addActionListener(e -> recordSelectedPayment());
        viewHistory.addActionListener(e -> showSelectedHistory());
        refresh.addActionListener(e -> refreshFees());
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        actions.add(addCharge);
        actions.add(recordPayment);
        actions.add(viewHistory);
        actions.add(refresh);
        actions.add(close);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshFees() {
        try {
            tableModel.setRowCount(0);
            for (FeeCharge charge : repository.findCharges()) {
                BigDecimal paid = repository.getPaidAmount(charge.getId());
                BigDecimal balance = charge.getAmount().subtract(paid);
                String studentName = students.stream()
                        .filter(student -> student.getRollNumber().equalsIgnoreCase(charge.getStudentRollNumber()))
                        .map(StudentUser::getFullName).findFirst().orElse("Unknown student");
                String status = balance.signum() == 0 ? "Paid"
                        : paid.signum() > 0 ? "Partially paid"
                        : charge.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Unpaid";
                tableModel.addRow(new Object[] {studentName, charge.getStudentRollNumber(),
                        charge.getDescription(), formatMoney(charge.getAmount()), charge.getDueDate(),
                        formatMoney(paid), formatMoney(balance), status, charge.getId()});
            }
        } catch (IOException e) {
            showError("Unable to load fee records.", e);
        }
    }

    private void createCharge() {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add a student account before creating a fee charge.",
                    "No Students", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<StudentUser> studentBox = new JComboBox<>(
                new DefaultComboBoxModel<>(students.toArray(new StudentUser[0])));
        studentBox.setRenderer((list, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getRollNumber() + " - " + value.getFullName());
            if (selected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField dueDateField = new JTextField(LocalDate.now().plusDays(30).toString());
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Student"));
        form.add(studentBox);
        form.add(new JLabel("Fee description"));
        form.add(descriptionField);
        form.add(new JLabel("Amount (INR)"));
        form.add(amountField);
        form.add(new JLabel("Due date (yyyy-MM-dd)"));
        form.add(dueDateField);

        while (JOptionPane.showConfirmDialog(this, form, "Create Student Fee Charge",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                StudentUser student = (StudentUser) studentBox.getSelectedItem();
                if (student == null) {
                    throw new IllegalArgumentException("Select a student.");
                }
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
                repository.saveCharge(FeeCharge.create(student.getRollNumber(), descriptionField.getText(),
                        amount, dueDate, adminName));
                refreshFees();
                return;
            } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount and due date.",
                        "Invalid Fee", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Fee",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                showError("Unable to save fee charge.", e);
                return;
            }
        }
    }

    private void recordSelectedPayment() {
        Optional<FeeCharge> selected = selectedCharge();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a fee charge first.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FeeCharge charge = selected.get();
        try {
            BigDecimal balance = charge.getAmount().subtract(repository.getPaidAmount(charge.getId()));
            if (balance.signum() <= 0) {
                JOptionPane.showMessageDialog(this, "This fee is already fully paid.", "No Balance",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JTextField amountField = new JTextField(balance.toPlainString());
            JComboBox<String> methodBox = new JComboBox<>(new String[] {
                    "Cash", "Bank Transfer", "UPI", "Card", "Other"
            });
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Outstanding balance"));
            form.add(new JLabel(formatMoney(balance)));
            form.add(new JLabel("Payment amount (INR)"));
            form.add(amountField);
            form.add(new JLabel("Payment method"));
            form.add(methodBox);
            if (JOptionPane.showConfirmDialog(this, form, "Record Student Payment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            FeePayment payment = repository.recordPayment(charge.getId(),
                    new BigDecimal(amountField.getText().trim()),
                    methodBox.getSelectedItem().toString(), adminName);
            refreshFees();
            showReceipt(charge, payment);
            try {
                notificationRepository.save(StudentNotification.create(NotificationType.FEE,
                        payment.getStudentRollNumber(), "Fee payment received",
                        "Payment of " + formatMoney(payment.getAmount()) + " was recorded for "
                                + charge.getDescription() + ". Receipt: " + payment.getReceiptNumber(),
                        adminName));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Payment was recorded, but the student notification could not be sent.\n" + e.getMessage(),
                        "Notification Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage() == null
                    ? "Enter a valid payment amount." : e.getMessage(), "Invalid Payment",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            showError("Unable to record payment.", e);
        }
    }

    private Optional<FeeCharge> selectedCharge() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return Optional.empty();
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String id = tableModel.getValueAt(modelRow, 8).toString();
        try {
            return repository.findCharges().stream().filter(charge -> charge.getId().equals(id)).findFirst();
        } catch (IOException e) {
            showError("Unable to load selected fee charge.", e);
            return Optional.empty();
        }
    }

    private void showSelectedHistory() {
        Optional<FeeCharge> selected = selectedCharge();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a fee charge first.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new FeeHistoryDialog(this, selected.get().getStudentRollNumber(),
                "Payments: " + selected.get().getDescription(), selected.get().getId()).setVisible(true);
    }

    private void showReceipt(FeeCharge charge, FeePayment payment) {
        String receipt = "EDUMANAGE PAYMENT RECEIPT\n"
                + "Receipt: " + payment.getReceiptNumber() + "\n"
                + "Student: " + payment.getStudentRollNumber() + "\n"
                + "Fee: " + charge.getDescription() + "\n"
                + "Amount: " + formatMoney(payment.getAmount()) + "\n"
                + "Method: " + payment.getPaymentMethod() + "\n"
                + "Paid at: " + DATE_FORMAT.format(payment.getPaidAt()) + "\n"
                + "Received by: " + payment.getReceivedBy();
        Object[] options = {"Save Receipt", "Close"};
        if (JOptionPane.showOptionDialog(this, receipt, "Payment Receipt",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]) == 0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Payment Receipt");
            chooser.setSelectedFile(new File(payment.getReceiptNumber() + ".txt"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(chooser.getSelectedFile().toPath(), receipt, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    showError("Unable to save payment receipt.", e);
                }
            }
        }
    }

    private void showError(String message, IOException error) {
        JOptionPane.showMessageDialog(this, message + "\n" + error.getMessage(),
                "Fee Storage Error", JOptionPane.ERROR_MESSAGE);
    }

    static String formatMoney(BigDecimal amount) {
        return "INR " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
