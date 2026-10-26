package com.sset.erp.repository;

import com.sset.erp.model.AssessmentRecord;
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
import java.util.Optional;

/**
 * File-backed assessment repository using UTF-8 tab-separated records.
 */
public class FileAssessmentRepository implements AssessmentRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "assessments.tsv");
    private final Path storageFile;

    public FileAssessmentRepository() {
        this(DEFAULT_FILE);
    }

    public FileAssessmentRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Assessment storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<AssessmentRecord> findAll() throws IOException {
        List<AssessmentRecord> records = readRecords();
        records.sort(Comparator.comparing(AssessmentRecord::getRecordedAt).reversed());
        return records;
    }

    @Override
    public synchronized List<AssessmentRecord> findByRollNumber(String rollNumber) throws IOException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student roll number is required.");
        }
        List<AssessmentRecord> matches = new ArrayList<>();
        for (AssessmentRecord record : readRecords()) {
            if (record.getRollNumber().equalsIgnoreCase(rollNumber.trim())) {
                matches.add(record);
            }
        }
        matches.sort(Comparator.comparing(AssessmentRecord::getRecordedAt).reversed());
        return matches;
    }

    @Override
    public synchronized Optional<AssessmentRecord> findById(String id) throws IOException {
        for (AssessmentRecord record : readRecords()) {
            if (record.getId().equals(id)) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized AssessmentRecord save(AssessmentRecord record) throws IOException {
        if (record == null) {
            throw new IllegalArgumentException("Assessment record is required.");
        }
        List<AssessmentRecord> records = readRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId().equals(record.getId())) {
                records.set(i, record);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(record);
        }
        writeRecords(records);
        return record;
    }

    @Override
    public synchronized boolean deleteById(String id) throws IOException {
        List<AssessmentRecord> records = readRecords();
        boolean removed = records.removeIf(record -> record.getId().equals(id));
        if (removed) {
            writeRecords(records);
        }
        return removed;
    }

    private List<AssessmentRecord> readRecords() throws IOException {
        List<AssessmentRecord> records = new ArrayList<>();
        if (!Files.exists(storageFile)) {
            return records;
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                String[] fields = line.split("\\t", -1);
                if (fields.length != 8 && fields.length != 9) {
                    throw new IllegalArgumentException("Expected 8 legacy fields or 9 current fields.");
                }
                int subjectCredits = fields.length == 9 ? Integer.parseInt(fields[6]) : 1;
                int facultyIndex = fields.length == 9 ? 7 : 6;
                int recordedAtIndex = fields.length == 9 ? 8 : 7;
                records.add(new AssessmentRecord(
                        decode(fields[0]), decode(fields[1]), decode(fields[2]), decode(fields[3]),
                        Double.parseDouble(fields[4]), Double.parseDouble(fields[5]), subjectCredits,
                        decode(fields[facultyIndex]), LocalDateTime.parse(decode(fields[recordedAtIndex]))));
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException("Invalid assessment data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return records;
    }

    private void writeRecords(List<AssessmentRecord> records) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tempFile = Files.createTempFile(parent, "assessments-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (AssessmentRecord record : records) {
                lines.add(encode(record.getId()) + "\t"
                        + encode(record.getRollNumber()) + "\t"
                        + encode(record.getSubject()) + "\t"
                        + encode(record.getAssessmentName()) + "\t"
                        + Double.toString(record.getScore()) + "\t"
                        + Double.toString(record.getMaximumScore()) + "\t"
                        + record.getSubjectCredits() + "\t"
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
