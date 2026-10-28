package com.sset.erp.repository;

import com.sset.erp.model.Subject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * File-backed repository storing Subject definitions in UTF-8 Base64 TSV format.
 */
public class FileSubjectRepository implements SubjectRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "subjects.tsv");
    private final Path storageFile;

    public FileSubjectRepository() {
        this(DEFAULT_FILE);
    }

    public FileSubjectRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Subject storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<Subject> findAll() throws IOException {
        List<Subject> subjects = readRecords();
        subjects.sort(Comparator.comparing(Subject::getSemester).thenComparing(Subject::getCode));
        return subjects;
    }

    @Override
    public synchronized Optional<Subject> findByCode(String code) throws IOException {
        if (code == null) return Optional.empty();
        for (Subject s : readRecords()) {
            if (s.getCode().equalsIgnoreCase(code.trim())) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized List<Subject> findByDepartmentAndSemester(String department, int semester) throws IOException {
        List<Subject> matches = new ArrayList<>();
        for (Subject s : readRecords()) {
            boolean deptMatches = department == null || department.trim().isEmpty()
                    || s.getDepartment().equalsIgnoreCase(department.trim());
            boolean semMatches = semester <= 0 || s.getSemester() == semester;
            if (deptMatches && semMatches) {
                matches.add(s);
            }
        }
        matches.sort(Comparator.comparing(Subject::getCode));
        return matches;
    }

    @Override
    public synchronized Subject save(Subject subject) throws IOException {
        if (subject == null) {
            throw new IllegalArgumentException("Subject is required.");
        }
        List<Subject> records = readRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getCode().equalsIgnoreCase(subject.getCode())) {
                records.set(i, subject);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(subject);
        }
        writeRecords(records);
        return subject;
    }

    @Override
    public synchronized boolean delete(String code) throws IOException {
        if (code == null) return false;
        List<Subject> records = readRecords();
        boolean removed = records.removeIf(s -> s.getCode().equalsIgnoreCase(code.trim()));
        if (removed) {
            writeRecords(records);
        }
        return removed;
    }

    private List<Subject> readRecords() throws IOException {
        if (!Files.exists(storageFile)) {
            if (DEFAULT_FILE.equals(storageFile)) {
                List<Subject> seed = createDefaultSubjects();
                writeRecords(seed);
                return seed;
            }
            return new ArrayList<>();
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        if (lines.isEmpty() && DEFAULT_FILE.equals(storageFile)) {
            List<Subject> seed = createDefaultSubjects();
            writeRecords(seed);
            return seed;
        }
        List<Subject> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] fields = line.split("\t", -1);
            if (fields.length < 6) {
                throw new IOException("Malformed subject row at " + storageFile + ":" + (i + 1));
            }
            try {
                records.add(new Subject(
                        decode(fields[0]),
                        decode(fields[1]),
                        decode(fields[2]),
                        Integer.parseInt(fields[3]),
                        Integer.parseInt(fields[4]),
                        decode(fields[5])
                ));
            } catch (Exception e) {
                throw new IOException("Invalid subject data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return records;
    }

    private void writeRecords(List<Subject> records) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tempFile = Files.createTempFile(parent, "subjects-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (Subject s : records) {
                lines.add(encode(s.getCode()) + "\t"
                        + encode(s.getName()) + "\t"
                        + encode(s.getDepartment()) + "\t"
                        + s.getSemester() + "\t"
                        + s.getCredits() + "\t"
                        + encode(s.getType()));
            }
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, storageFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, storageFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private List<Subject> createDefaultSubjects() {
        List<Subject> list = new ArrayList<>();
        list.add(new Subject("CST201", "Object Oriented Programming with Java", "Computer Science & Engineering", 3, 4, "Theory"));
        list.add(new Subject("CST203", "Data Structures", "Computer Science & Engineering", 3, 4, "Theory"));
        list.add(new Subject("CST205", "Discrete Mathematical Structures", "Computer Science & Engineering", 3, 4, "Theory"));
        list.add(new Subject("EST200", "Design & Engineering", "Computer Science & Engineering", 3, 2, "Theory"));
        list.add(new Subject("MCN201", "Sustainable Engineering", "Computer Science & Engineering", 3, 2, "Theory"));
        list.add(new Subject("CSL201", "Object Oriented Programming Lab", "Computer Science & Engineering", 3, 2, "Lab"));
        list.add(new Subject("CSL203", "Data Structures Lab", "Computer Science & Engineering", 3, 2, "Lab"));
        list.add(new Subject("CST301", "Operating Systems", "Computer Science & Engineering", 5, 4, "Theory"));
        list.add(new Subject("CST281", "Object Oriented Programming Concepts", "Electronics & Communication Engineering", 3, 4, "Theory"));
        return list;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
