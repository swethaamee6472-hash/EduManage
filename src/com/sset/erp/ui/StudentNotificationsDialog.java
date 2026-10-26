package com.sset.erp.ui;

import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.ui.components.ModernButton;
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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Student inbox for general and individually targeted notices.
 */
public class StudentNotificationsDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int LIVE_REFRESH_DELAY_MS = 3000;

    private final StudentUser student;
    private final FileStudentNotificationRepository repository = new FileStudentNotificationRepository();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"Type", "Title", "From", "Published", "Notification ID"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextArea messageArea = new JTextArea();
    private final JLabel summary = new JLabel();
    private final Timer refreshTimer;
    private boolean initialLoad = true;

    public StudentNotificationsDialog(JFrame owner, StudentUser student) {
        super(owner, "Student Notifications", true);
        this.student = student;
        initUI();
        refreshTimer = new Timer(LIVE_REFRESH_DELAY_MS, e -> refreshNotifications());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                refreshTimer.start();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refreshTimer.stop();
            }
        });
        refreshNotifications();
    }

    private void initUI() {
        setSize(850, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Notifications & Notices", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(this::showSelectedNotification);
        table.removeColumn(table.getColumnModel().getColumn(4));

        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(UITheme.FONT_BODY);
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createTitledBorder("Message"));
        messageScroll.setPreferredSize(new java.awt.Dimension(100, 130));

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(messageScroll, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        add(content, BorderLayout.CENTER);

        summary.setFont(UITheme.FONT_BODY_BOLD);
        summary.setForeground(UITheme.TEXT_PRIMARY);
        ModernButton refresh = new ModernButton("Refresh", ModernButton.Variant.OUTLINE);
        ModernButton close = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        refresh.addActionListener(e -> refreshNotifications());
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        footer.add(summary, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(refresh);
        buttons.add(close);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshNotifications() {
        try {
            List<StudentNotification> notifications = repository.findForStudent(student.getRollNumber());
            String selectedId = null;
            if (table.getSelectedRow() >= 0) {
                int selectedModelRow = table.convertRowIndexToModel(table.getSelectedRow());
                selectedId = tableModel.getValueAt(selectedModelRow, 4).toString();
            }
            java.util.Set<String> existingIds = new java.util.HashSet<>();
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                existingIds.add(tableModel.getValueAt(row, 4).toString());
            }
            int newCount = 0;
            tableModel.setRowCount(0);
            for (StudentNotification notification : notifications) {
                if (!initialLoad && !existingIds.contains(notification.getId())) {
                    newCount++;
                }
                tableModel.addRow(new Object[] {
                        notification.getType().getDisplayName(), notification.getTitle(),
                        notification.getSenderName(), DATE_FORMAT.format(notification.getCreatedAt()),
                        notification.getId()
                });
            }
            summary.setText(notifications.size() + " notification"
                    + (notifications.size() == 1 ? "" : "s")
                    + (newCount > 0 ? " • " + newCount + " new update" + (newCount == 1 ? "" : "s") : ""));
            messageArea.setText(notifications.isEmpty()
                    ? "No notifications yet." : "Select a notification to read its message.");
            initialLoad = false;
            if (!notifications.isEmpty()) {
                int rowToSelect = 0;
                if (selectedId != null) {
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        if (selectedId.equals(tableModel.getValueAt(row, 4))) {
                            rowToSelect = table.convertRowIndexToView(row);
                            break;
                        }
                    }
                }
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load notifications.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedNotification(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        String id = tableModel.getValueAt(modelRow, 4).toString();
        try {
            for (StudentNotification notification : repository.findForStudent(student.getRollNumber())) {
                if (notification.getId().equals(id)) {
                    messageArea.setText(notification.getType().getDisplayName()
                            + "\nPublished: " + DATE_FORMAT.format(notification.getCreatedAt())
                            + "\nFrom: " + notification.getSenderName()
                            + "\n\n" + notification.getMessage());
                    messageArea.setCaretPosition(0);
                    return;
                }
            }
            messageArea.setText("This notification is no longer available. Refresh the inbox.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load the selected notification.\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
