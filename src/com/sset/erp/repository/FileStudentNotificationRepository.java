package com.sset.erp.repository;

import com.sset.erp.model.StudentNotification;
import com.sset.erp.model.NotificationType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

/**
 * File-backed student notification repository.
 */
public class FileStudentNotificationRepository implements StudentNotificationRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "notifications.tsv");
    private final Path storageFile;

    public FileStudentNotificationRepository() {
        this(DEFAULT_FILE);
    }

    public FileStudentNotificationRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Notification storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<StudentNotification> findAll() throws IOException {
        List<StudentNotification> notifications = readNotifications();
        notifications.sort(Comparator.comparing(StudentNotification::getCreatedAt).reversed());
        return notifications;
    }

    @Override
    public synchronized List<StudentNotification> findForStudent(String rollNumber) throws IOException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student roll number is required.");
        }
        List<StudentNotification> matches = new ArrayList<>();
        for (StudentNotification notification : readNotifications()) {
            if (notification.isForAllStudents()
                    || notification.getRecipientRollNumber().equalsIgnoreCase(rollNumber.trim())) {
                matches.add(notification);
            }
        }
        matches.sort(Comparator.comparing(StudentNotification::getCreatedAt).reversed());
        return matches;
    }

    @Override
    public synchronized StudentNotification save(StudentNotification notification) throws IOException {
        if (notification == null) {
            throw new IllegalArgumentException("Notification is required.");
        }
        List<StudentNotification> notifications = readNotifications();
        boolean replaced = false;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(notification.getId())) {
                notifications.set(i, notification);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            notifications.add(notification);
        }
        writeNotifications(notifications);
        return notification;
    }

    private List<StudentNotification> readNotifications() throws IOException {
        List<StudentNotification> notifications = new ArrayList<>();
        if (!Files.exists(storageFile)) {
            return notifications;
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                continue;
            }
            try {
                String[] fields = lines.get(i).split("\\t", -1);
                if (fields.length != 6 && fields.length != 7 && fields.length != 8) {
                    throw new IllegalArgumentException("Expected 6 legacy, 7 current, or 8 scheduled fields.");
                }
                NotificationType type = fields.length >= 7
                        ? NotificationType.valueOf(decode(fields[5])) : NotificationType.ANNOUNCEMENT;
                String dateField = fields.length >= 7 ? fields[6] : fields[5];
                notifications.add(new StudentNotification(decode(fields[0]), decode(fields[1]),
                        decode(fields[2]), decode(fields[3]), decode(fields[4]), type,
                        LocalDateTime.parse(decode(dateField))));
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException("Invalid notification data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return notifications;
    }

    private void writeNotifications(List<StudentNotification> notifications) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path tempFile = Files.createTempFile(parent, "notifications-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (StudentNotification notification : notifications) {
                lines.add(encode(notification.getId()) + "\t"
                        + encode(notification.getRecipientRollNumber()) + "\t"
                        + encode(notification.getTitle()) + "\t"
                        + encode(notification.getMessage()) + "\t"
                        + encode(notification.getSenderName()) + "\t"
                        + encode(notification.getType().name()) + "\t"
                        + encode(notification.getCreatedAt().toString()));
            }
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, storageFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, storageFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
