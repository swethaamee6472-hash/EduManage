package com.sset.erp.repository;

import com.sset.erp.model.AttendanceRecord;
import java.io.IOException;
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

/**
 * File-backed attendance repository.
 */
public class FileAttendanceRepository implements AttendanceRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "attendance.tsv");
    private final Path storageFile;

    public FileAttendanceRepository() {
        this(DEFAULT_FILE);
    }

    public FileAttendanceRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Attendance storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<AttendanceRecord> findAll() throws IOException {
        List<AttendanceRecord> records = readRecords();
        records.sort(Comparator.comparing(AttendanceRecord::getRecordedAt).reversed());
        return records;
    }

    @Override
    public synchronized List<AttendanceRecord> findByRollNumber(String rollNumber) throws IOException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student roll number is required.");
        }
        List<AttendanceRecord> matches = new ArrayList<>();
        for (AttendanceRecord record : readRecords()) {
            if (record.getRollNumber().equalsIgnoreCase(rollNumber.trim())) {
                matches.add(record);
            }
        }
        matches.sort(Comparator.comparing(AttendanceRecord::getRecordedAt).reversed());
        return matches;
    }

    @Override
    public synchronized AttendanceRecord save(AttendanceRecord record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException("Attendance record is required.");
        }
        List<AttendanceRecord> records = readRecords();
        for (int i = 0; i < records.size(); i++) {
            AttendanceRecord existing = records.get(i);
            if (existing.getId().equals(record.getId())) {
                records.set(i, record);
                writeRecords(records);
                return record;
            }
            if (existing.getRollNumber().equalsIgnoreCase(record.getRollNumber())
                    && existing.getSubject().equalsIgnoreCase(record.getSubject())
                    && existing.getRecordedAt().toLocalDate().equals(record.getRecordedAt().toLocalDate())) {
                return existing;
            }
        }
        records.add(record);
        writeRecords(records);
        return record;
    }

    private List<AttendanceRecord> readRecords() throws IOException {
        List<AttendanceRecord> records = new ArrayList<>();
        if (!Files.exists(storageFile)) {
            return records;
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                continue;
            }
            try {
                String[] fields = lines.get(i).split("\\t", -1);
                if (fields.length != 5) {
                    throw new IllegalArgumentException("Expected 5 fields.");
                }
                records.add(new AttendanceRecord(decode(fields[0]), decode(fields[1]), decode(fields[2]),
                        decode(fields[3]), LocalDateTime.parse(decode(fields[4]))));
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException("Invalid attendance data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return records;
    }

    private void writeRecords(List<AttendanceRecord> records) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path tempFile = Files.createTempFile(parent, "attendance-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (AttendanceRecord record : records) {
                lines.add(encode(record.getId()) + "\t"
                        + encode(record.getRollNumber()) + "\t"
                        + encode(record.getSubject()) + "\t"
                        + encode(record.getFacultyName()) + "\t"
                        + encode(record.getRecordedAt().toString()));
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
