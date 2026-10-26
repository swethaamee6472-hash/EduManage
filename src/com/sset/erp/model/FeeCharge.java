package com.sset.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A fee amount assigned to a student.
 */
public class FeeCharge {
    private final String id;
    private final String studentRollNumber;
    private final String description;
    private final BigDecimal amount;
    private final LocalDate dueDate;
    private final String createdBy;
    private final LocalDateTime createdAt;

    public FeeCharge(String id, String studentRollNumber, String description, BigDecimal amount,
                     LocalDate dueDate, String createdBy, LocalDateTime createdAt) {
        this.id = requireText(id, "Fee ID");
        this.studentRollNumber = requireText(studentRollNumber, "Student roll number");
        this.description = requireText(description, "Fee description");
        this.amount = requireAmount(amount);
        if (dueDate == null || createdAt == null) {
            throw new IllegalArgumentException("Fee due and created dates are required.");
        }
        this.dueDate = dueDate;
        this.createdBy = requireText(createdBy, "Created by");
        this.createdAt = createdAt;
    }

    public static FeeCharge create(String studentRollNumber, String description, BigDecimal amount,
                                   LocalDate dueDate, String createdBy) {
        return new FeeCharge(UUID.randomUUID().toString(), studentRollNumber, description, amount,
                dueDate, createdBy, LocalDateTime.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public static BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount must be positive and have at most two decimal places.");
        }
        return amount.setScale(2);
    }

    public String getId() {
        return id;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
