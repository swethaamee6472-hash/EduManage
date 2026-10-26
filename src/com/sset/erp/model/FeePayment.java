package com.sset.erp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An immutable payment entry and its receipt reference.
 */
public class FeePayment {
    private final String receiptNumber;
    private final String feeId;
    private final String studentRollNumber;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String receivedBy;
    private final LocalDateTime paidAt;

    public FeePayment(String receiptNumber, String feeId, String studentRollNumber, BigDecimal amount,
                      String paymentMethod, String receivedBy, LocalDateTime paidAt) {
        this.receiptNumber = requireText(receiptNumber, "Receipt number");
        this.feeId = requireText(feeId, "Fee ID");
        this.studentRollNumber = requireText(studentRollNumber, "Student roll number");
        this.amount = FeeCharge.requireAmount(amount);
        this.paymentMethod = requireText(paymentMethod, "Payment method");
        this.receivedBy = requireText(receivedBy, "Received by");
        if (paidAt == null) {
            throw new IllegalArgumentException("Payment date is required.");
        }
        this.paidAt = paidAt;
    }

    public static FeePayment create(String feeId, String studentRollNumber, BigDecimal amount,
                                    String paymentMethod, String receivedBy) {
        String receipt = "RCPT-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        return new FeePayment(receipt, feeId, studentRollNumber, amount,
                paymentMethod, receivedBy, LocalDateTime.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value.trim();
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getFeeId() {
        return feeId;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
