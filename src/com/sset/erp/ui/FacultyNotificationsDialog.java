package com.sset.erp.ui;

import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.NotificationType;
import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.StudentUser;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.repository.FileUserRepository;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Faculty tool for publishing general or student-specific notices.
 */
public class FacultyNotificationsDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FacultyUser faculty;
    private final List<StudentUser> students;
    private final FileStudentNotificationRepository repository = new FileStudentNotificationRepository();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"Type", "Title", "Recipient", "Message", "Published", "Notification ID"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public FacultyNotificationsDialog(JFrame owner, FacultyUser faculty) {
        super(owner, "Publish Student Notification", true);
        this.faculty = faculty;
        this.students = new FileUserRepository().findAll().stream()
                .filter(StudentUser.class::isInstance)
                .map(StudentUser.class::cast)
                .toList();
        initUI();
        refreshNotifications();
    }

    private void initUI() {
        setSize(920, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Student Notices", SwingConstants.LEFT);
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.removeColumn(table.getColumnModel().getColumn(5));
        add(new JScrollPane(table), BorderLayout.CENTER);

        ModernButton publish = new ModernButton("Publish Notice", ModernButton.Variant.PRIMARY);
        ModernButton refresh = new ModernButton("Refresh", ModernButton.Variant.OUTLINE);
        ModernButton close = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        publish.addActionListener(e -> publishNotification());
        refresh.addActionListener(e -> refreshNotifications());
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.add(publish);
        actions.add(refresh);
        actions.add(close);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshNotifications() {
        try {
            List<StudentNotification> notifications = repository.findAll();
            tableModel.setRowCount(0);
            for (StudentNotification notification : notifications) {
                String recipient = notification.isForAllStudents()
                        ? "All students" : notification.getRecipientRollNumber();
                tableModel.addRow(new Object[] {notification.getType().getDisplayName(),
                        notification.getTitle(), recipient, notification.getMessage(),
                        DATE_FORMAT.format(notification.getCreatedAt()),
                        notification.getId()});
            }
        } catch (IOException e) {
            showError("Unable to load notifications.", e);
        }
    }

    private void publishNotification() {
        JComboBox<String> recipientBox = new JComboBox<>(new DefaultComboBoxModel<>(recipientOptions()));
        JComboBox<NotificationType> typeBox = new JComboBox<>(new NotificationType[] {
                NotificationType.ANNOUNCEMENT, NotificationType.EVENT, NotificationType.EXAM
        });
        JTextField titleField = new JTextField();
        JTextArea messageField = new JTextArea(5, 28);
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Recipient"));
        form.add(recipientBox);
        form.add(new JLabel("Notification type"));
        form.add(typeBox);
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Message"));
        form.add(new JScrollPane(messageField));
        int maximumFormHeight = Math.max(160,
                GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 220);
        JScrollPane formScrollPane = new JScrollPane(form);
        formScrollPane.setPreferredSize(new Dimension(460,
                Math.min(form.getPreferredSize().height, maximumFormHeight)));

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, formScrollPane, "Publish Notice",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                int selected = recipientBox.getSelectedIndex();
                String recipient = selected == 0 ? StudentNotification.ALL_STUDENTS
                        : students.get(selected - 1).getRollNumber();
                NotificationType type = (NotificationType) typeBox.getSelectedItem();
                StudentNotification notification = StudentNotification.create(type, recipient,
                        titleField.getText(), messageField.getText(), faculty.getFullName());
                repository.save(notification);
                refreshNotifications();
                JOptionPane.showMessageDialog(this, "Notification published successfully.",
                        "Published", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Notification",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                showError("Unable to publish notification.", e);
                return;
            }
        }
    }

    private String[] recipientOptions() {
        String[] options = new String[students.size() + 1];
        options[0] = "All students";
        for (int i = 0; i < students.size(); i++) {
            StudentUser student = students.get(i);
            options[i + 1] = student.getRollNumber() + " - " + student.getFullName();
        }
        return options;
    }

    private void showError(String message, IOException error) {
        JOptionPane.showMessageDialog(this, message + "\n" + error.getMessage(),
                "Storage Error", JOptionPane.ERROR_MESSAGE);
    }
}
