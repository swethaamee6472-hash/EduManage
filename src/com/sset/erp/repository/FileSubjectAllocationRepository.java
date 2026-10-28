package com.sset.erp.repository;

import com.sset.erp.model.SubjectAllocation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * File-backed repository storing Subject Allocation records in UTF-8 Base64 TSV format.
 */
public class FileSubjectAllocationRepository implements SubjectAllocationRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "subject_allocations.tsv");
    private final Path storageFile;

    public FileSubjectAllocationRepository() {
        this(DEFAULT_FILE);
    }

    public FileSubjectAllocationRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Subject allocation storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<SubjectAllocation> findAll() throws IOException {
        List<SubjectAllocation> list = readRecords();
        list.sort(Comparator.comparing(SubjectAllocation::getSemester)
                .thenComparing(SubjectAllocation::getSubjectCode));
        return list;
    }

    @Override
    public synchronized List<SubjectAllocation> findByFaculty(String facultyUsername) throws IOException {
        if (facultyUsername == null) return List.of();
        List<SubjectAllocation> matches = new ArrayList<>();
        for (SubjectAllocation alloc : readRecords()) {
            if (alloc.getFacultyUsername().equalsIgnoreCase(facultyUsername.trim())) {
                matches.add(alloc);
            }
        }
        matches.sort(Comparator.comparing(SubjectAllocation::getSemester)
                .thenComparing(SubjectAllocation::getSubjectCode));
        return matches;
    }

    @Override
    public synchronized List<SubjectAllocation> findByDepartmentAndSemester(String department, int semester) throws IOException {
        List<SubjectAllocation> matches = new ArrayList<>();
        for (SubjectAllocation alloc : readRecords()) {
            boolean deptMatches = department == null || department.trim().isEmpty()
                    || alloc.getDepartment().equalsIgnoreCase(department.trim());
            boolean semMatches = semester <= 0 || alloc.getSemester() == semester;
            if (deptMatches && semMatches) {
                matches.add(alloc);
            }
        }
        matches.sort(Comparator.comparing(SubjectAllocation::getSubjectCode));
        return matches;
    }

    @Override
    public synchronized Optional<SubjectAllocation> findById(String id) throws IOException {
        if (id == null) return Optional.empty();
        for (SubjectAllocation alloc : readRecords()) {
            if (alloc.getId().equals(id)) {
                return Optional.of(alloc);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized SubjectAllocation save(SubjectAllocation allocation) throws IOException {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation is required.");
        }
        List<SubjectAllocation> records = readRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId().equals(allocation.getId())) {
                records.set(i, allocation);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(allocation);
        }
        writeRecords(records);
        return allocation;
    }

    @Override
    public synchronized boolean delete(String id) throws IOException {
        if (id == null) return false;
        List<SubjectAllocation> records = readRecords();
        boolean removed = records.removeIf(a -> a.getId().equals(id));
        if (removed) {
            writeRecords(records);
        }
        return removed;
    }

    private List<SubjectAllocation> readRecords() throws IOException {
        if (!Files.exists(storageFile)) {
            if (DEFAULT_FILE.equals(storageFile)) {
                List<SubjectAllocation> seed = createDefaultAllocations();
                writeRecords(seed);
                return seed;
            }
            return new ArrayList<>();
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        if (lines.isEmpty() && DEFAULT_FILE.equals(storageFile)) {
            List<SubjectAllocation> seed = createDefaultAllocations();
            writeRecords(seed);
            return seed;
        }
        List<SubjectAllocation> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] fields = line.split("\t", -1);
            if (fields.length < 10) {
                throw new IOException("Malformed subject allocation row at " + storageFile + ":" + (i + 1));
            }
            try {
                records.add(new SubjectAllocation(
                        decode(fields[0]),
                        decode(fields[1]),
                        decode(fields[2]),
                        decode(fields[3]),
                        Integer.parseInt(fields[4]),
                        decode(fields[5]),
                        decode(fields[6]),
                        decode(fields[7]),
                        decode(fields[8]),
                        LocalDateTime.parse(decode(fields[9]))
                ));
            } catch (Exception e) {
                throw new IOException("Invalid subject allocation data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return records;
    }

    private void writeRecords(List<SubjectAllocation> records) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tempFile = Files.createTempFile(parent, "subject_alloc-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (SubjectAllocation a : records) {
                lines.add(encode(a.getId()) + "\t"
                        + encode(a.getSubjectCode()) + "\t"
                        + encode(a.getSubjectName()) + "\t"
                        + encode(a.getDepartment()) + "\t"
                        + a.getSemester() + "\t"
                        + encode(a.getBatch()) + "\t"
                        + encode(a.getFacultyUsername()) + "\t"
                        + encode(a.getFacultyName()) + "\t"
                        + encode(a.getAcademicYear()) + "\t"
                        + encode(a.getAllocatedAt().toString()));
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

    private List<SubjectAllocation> createDefaultAllocations() {
        List<SubjectAllocation> list = new ArrayList<>();
        list.add(SubjectAllocation.create("CST201", "Object Oriented Programming with Java",
                "Computer Science & Engineering", 3, "2024-2028 (CS4)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        list.add(SubjectAllocation.create("CSL201", "Object Oriented Programming Lab",
                "Computer Science & Engineering", 3, "2024-2028 (CS4)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        list.add(SubjectAllocation.create("CST203", "Data Structures",
                "Computer Science & Engineering", 3, "2024-2028 (CS4)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        list.add(SubjectAllocation.create("CST205", "Discrete Mathematical Structures",
                "Computer Science & Engineering", 3, "2024-2028 (CS4)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        // Courses taught by Prof. Rajesh Kumar in other classes
        list.add(SubjectAllocation.create("CST301", "Operating Systems",
                "Computer Science & Engineering", 5, "2023-2027 (CS3)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        list.add(SubjectAllocation.create("CST281", "Object Oriented Programming Concepts",
                "Electronics & Communication Engineering", 3, "2024-2028 (EC1)", "faculty_cs", "Prof. Rajesh Kumar", "2026-2027"));
        return list;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
