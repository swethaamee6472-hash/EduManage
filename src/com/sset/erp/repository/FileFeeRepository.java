package com.sset.erp.repository;

import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.FeePayment;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * File-backed fee ledger. Charges and payment receipts are stored separately.
 */
public class FileFeeRepository implements FeeRepository {
    private static final Path DEFAULT_DIRECTORY = Paths.get("data");
    private static final Path CHARGES_FILE = DEFAULT_DIRECTORY.resolve("fee_charges.tsv");
    private static final Path PAYMENTS_FILE = DEFAULT_DIRECTORY.resolve("fee_payments.tsv");
    private static final Object LEDGER_LOCK = new Object();

    private final Path chargesFile;
    private final Path paymentsFile;

    public FileFeeRepository() {
        this(CHARGES_FILE, PAYMENTS_FILE);
    }

    public FileFeeRepository(Path chargesFile, Path paymentsFile) {
        if (chargesFile == null || paymentsFile == null) {
            throw new IllegalArgumentException("Fee ledger paths are required.");
        }
        this.chargesFile = chargesFile;
        this.paymentsFile = paymentsFile;
    }

    @Override
    public List<FeeCharge> findCharges() throws IOException {
        synchronized (LEDGER_LOCK) {
            List<FeeCharge> charges = readCharges();
            charges.sort(Comparator.comparing(FeeCharge::getDueDate)
                    .thenComparing(FeeCharge::getStudentRollNumber, String.CASE_INSENSITIVE_ORDER));
            return charges;
        }
    }

    @Override
    public List<FeeCharge> findChargesByStudent(String rollNumber) throws IOException {
        requireRollNumber(rollNumber);
        synchronized (LEDGER_LOCK) {
            List<FeeCharge> charges = new ArrayList<>();
            for (FeeCharge charge : readCharges()) {
                if (charge.getStudentRollNumber().equalsIgnoreCase(rollNumber.trim())) {
                    charges.add(charge);
                }
            }
            charges.sort(Comparator.comparing(FeeCharge::getDueDate));
            return charges;
        }
    }

    @Override
    public List<FeePayment> findPaymentsByStudent(String rollNumber) throws IOException {
        requireRollNumber(rollNumber);
        synchronized (LEDGER_LOCK) {
            List<FeePayment> payments = new ArrayList<>();
            for (FeePayment payment : readPayments()) {
                if (payment.getStudentRollNumber().equalsIgnoreCase(rollNumber.trim())) {
                    payments.add(payment);
                }
            }
            payments.sort(Comparator.comparing(FeePayment::getPaidAt).reversed());
            return payments;
        }
    }

    @Override
    public List<FeePayment> findPaymentsForCharge(String feeId) throws IOException {
        if (feeId == null || feeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Fee ID is required.");
        }
        synchronized (LEDGER_LOCK) {
            List<FeePayment> payments = new ArrayList<>();
            for (FeePayment payment : readPayments()) {
                if (payment.getFeeId().equals(feeId)) {
                    payments.add(payment);
                }
            }
            payments.sort(Comparator.comparing(FeePayment::getPaidAt));
            return payments;
        }
    }

    @Override
    public BigDecimal getPaidAmount(String feeId) throws IOException {
        BigDecimal paid = BigDecimal.ZERO;
        for (FeePayment payment : findPaymentsForCharge(feeId)) {
            paid = paid.add(payment.getAmount());
        }
        return paid;
    }

    @Override
    public FeeCharge saveCharge(FeeCharge charge) throws IOException {
        if (charge == null) {
            throw new IllegalArgumentException("Fee charge is required.");
        }
        synchronized (LEDGER_LOCK) {
            List<FeeCharge> charges = readCharges();
            if (charges.stream().anyMatch(existing -> existing.getId().equals(charge.getId()))) {
                throw new IllegalArgumentException("Fee ID already exists.");
            }
            charges.add(charge);
            writeCharges(charges);
            return charge;
        }
    }

    @Override
    public FeePayment recordPayment(String feeId, BigDecimal amount, String paymentMethod,
                                    String receivedBy) throws IOException {
        FeeCharge.requireAmount(amount);
        if (paymentMethod == null || paymentMethod.trim().isEmpty()
                || receivedBy == null || receivedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method and receiver are required.");
        }
        synchronized (LEDGER_LOCK) {
            Optional<FeeCharge> charge = readCharges().stream()
                    .filter(item -> item.getId().equals(feeId)).findFirst();
            if (charge.isEmpty()) {
                throw new IllegalArgumentException("Fee charge was not found.");
            }
            BigDecimal paid = BigDecimal.ZERO;
            for (FeePayment payment : readPayments()) {
                if (payment.getFeeId().equals(feeId)) {
                    paid = paid.add(payment.getAmount());
                }
            }
            BigDecimal balance = charge.get().getAmount().subtract(paid);
            if (amount.compareTo(balance) > 0) {
                throw new IllegalArgumentException("Payment exceeds outstanding balance of " + balance + ".");
            }
            FeePayment payment = FeePayment.create(feeId, charge.get().getStudentRollNumber(), amount,
                    paymentMethod, receivedBy);
            List<FeePayment> payments = readPayments();
            payments.add(payment);
            writePayments(payments);
            return payment;
        }
    }

    private List<FeeCharge> readCharges() throws IOException {
        List<FeeCharge> charges = new ArrayList<>();
        if (!Files.exists(chargesFile)) {
            return charges;
        }
        List<String> lines = Files.readAllLines(chargesFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                continue;
            }
            try {
                String[] fields = lines.get(i).split("\\t", -1);
                if (fields.length != 7) {
                    throw new IllegalArgumentException("Expected 7 fields.");
                }
                charges.add(new FeeCharge(decode(fields[0]), decode(fields[1]), decode(fields[2]),
                        new BigDecimal(fields[3]), LocalDate.parse(decode(fields[4])),
                        decode(fields[5]), LocalDateTime.parse(decode(fields[6]))));
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException("Invalid fee charge at " + chargesFile + ":" + (i + 1), e);
            }
        }
        return charges;
    }

    private List<FeePayment> readPayments() throws IOException {
        List<FeePayment> payments = new ArrayList<>();
        if (!Files.exists(paymentsFile)) {
            return payments;
        }
        List<String> lines = Files.readAllLines(paymentsFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                continue;
            }
            try {
                String[] fields = lines.get(i).split("\\t", -1);
                if (fields.length != 7) {
                    throw new IllegalArgumentException("Expected 7 fields.");
                }
                payments.add(new FeePayment(decode(fields[0]), decode(fields[1]), decode(fields[2]),
                        new BigDecimal(fields[3]), decode(fields[4]), decode(fields[5]),
                        LocalDateTime.parse(decode(fields[6]))));
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException("Invalid fee payment at " + paymentsFile + ":" + (i + 1), e);
            }
        }
        return payments;
    }

    private void writeCharges(List<FeeCharge> charges) throws IOException {
        List<String> lines = new ArrayList<>();
        for (FeeCharge charge : charges) {
            lines.add(encode(charge.getId()) + "\t" + encode(charge.getStudentRollNumber()) + "\t"
                    + encode(charge.getDescription()) + "\t" + charge.getAmount().toPlainString() + "\t"
                    + encode(charge.getDueDate().toString()) + "\t" + encode(charge.getCreatedBy()) + "\t"
                    + encode(charge.getCreatedAt().toString()));
        }
        writeAtomically(chargesFile, lines, "fee-charges-");
    }

    private void writePayments(List<FeePayment> payments) throws IOException {
        List<String> lines = new ArrayList<>();
        for (FeePayment payment : payments) {
            lines.add(encode(payment.getReceiptNumber()) + "\t" + encode(payment.getFeeId()) + "\t"
                    + encode(payment.getStudentRollNumber()) + "\t" + payment.getAmount().toPlainString() + "\t"
                    + encode(payment.getPaymentMethod()) + "\t" + encode(payment.getReceivedBy()) + "\t"
                    + encode(payment.getPaidAt().toString()));
        }
        writeAtomically(paymentsFile, lines, "fee-payments-");
    }

    private static void writeAtomically(Path target, List<String> lines, String prefix) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path tempFile = Files.createTempFile(parent, prefix, ".tmp");
        try {
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void requireRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student roll number is required.");
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
