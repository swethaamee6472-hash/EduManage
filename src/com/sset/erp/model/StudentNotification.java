package com.sset.erp.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A notice visible to every student or one targeted student.
 */
public class StudentNotification {
    public static final String ALL_STUDENTS = "*";

    private final String id;
    private final String recipientRollNumber;
    private final String title;
    private final String message;
    private final String senderName;
    private final NotificationType type;
    private final LocalDateTime createdAt;

    public StudentNotification(String id, String recipientRollNumber, String title, String message,
                               String senderName, LocalDateTime createdAt) {
        this(id, recipientRollNumber, title, message, senderName, NotificationType.ANNOUNCEMENT, createdAt);
    }

    public StudentNotification(String id, String recipientRollNumber, String title, String message,
                               String senderName, NotificationType type, LocalDateTime createdAt) {
        this.id = requireText(id, "Notification ID");
        this.recipientRollNumber = requireText(recipientRollNumber, "Recipient");
        this.title = requireText(title, "Title");
        this.message = requireText(message, "Message");
        this.senderName = requireText(senderName, "Sender");
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Notification date is required.");
        }
        this.type = type;
        this.createdAt = createdAt;
    }

    public static StudentNotification create(String recipientRollNumber, String title, String message,
                                             String senderName) {
        return create(NotificationType.ANNOUNCEMENT, recipientRollNumber, title, message, senderName);
    }

    public static StudentNotification create(NotificationType type, String recipientRollNumber,
                                             String title, String message, String senderName) {
        return new StudentNotification(UUID.randomUUID().toString(), recipientRollNumber,
                title, message, senderName, type, LocalDateTime.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getRecipientRollNumber() {
        return recipientRollNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isForAllStudents() {
        return ALL_STUDENTS.equals(recipientRollNumber);
    }
}
