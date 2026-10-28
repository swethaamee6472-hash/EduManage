package com.sset.erp.repository;

import com.sset.erp.model.TimetableConflictException;
import com.sset.erp.model.TimetableEntry;
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
 * File-backed repository storing timetable schedule slots in UTF-8 Base64 TSV format.
 * Includes automated multi-level conflict detection for faculty, classrooms, and student batches.
 */
public class FileTimetableRepository implements TimetableRepository {
    private static final Path DEFAULT_FILE = Paths.get("data", "timetable.tsv");
    private final Path storageFile;

    public FileTimetableRepository() {
        this(DEFAULT_FILE);
    }

    public FileTimetableRepository(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException("Timetable storage path is required.");
        }
        this.storageFile = storageFile;
    }

    @Override
    public synchronized List<TimetableEntry> findAll() throws IOException {
        List<TimetableEntry> list = readRecords();
        sortEntries(list);
        return list;
    }

    @Override
    public synchronized List<TimetableEntry> findByDepartmentAndSemester(String department, int semester) throws IOException {
        List<TimetableEntry> matches = new ArrayList<>();
        for (TimetableEntry e : readRecords()) {
            boolean deptMatches = department == null || department.trim().isEmpty()
                    || e.getDepartment().equalsIgnoreCase(department.trim());
            boolean semMatches = semester <= 0 || e.getSemester() == semester;
            if (deptMatches && semMatches) {
                matches.add(e);
            }
        }
        sortEntries(matches);
        return matches;
    }

    @Override
    public synchronized List<TimetableEntry> findByDepartmentSemesterAndBatch(String department, int semester, String batch) throws IOException {
        List<TimetableEntry> matches = new ArrayList<>();
        for (TimetableEntry e : readRecords()) {
            boolean deptMatches = department == null || department.trim().isEmpty()
                    || e.getDepartment().equalsIgnoreCase(department.trim());
            boolean semMatches = semester <= 0 || e.getSemester() == semester;
            boolean batchMatches = batch == null || batch.trim().isEmpty()
                    || e.getBatch().equalsIgnoreCase("All Batches")
                    || batch.equalsIgnoreCase("All Batches")
                    || e.getBatch().equalsIgnoreCase(batch.trim());
            if (deptMatches && semMatches && batchMatches) {
                matches.add(e);
            }
        }
        sortEntries(matches);
        return matches;
    }

    @Override
    public synchronized List<TimetableEntry> findByFaculty(String facultyUsername) throws IOException {
        if (facultyUsername == null) return List.of();
        List<TimetableEntry> matches = new ArrayList<>();
        for (TimetableEntry e : readRecords()) {
            if (e.getFacultyUsername().equalsIgnoreCase(facultyUsername.trim())) {
                matches.add(e);
            }
        }
        sortEntries(matches);
        return matches;
    }

    @Override
    public synchronized Optional<TimetableEntry> findById(String id) throws IOException {
        if (id == null) return Optional.empty();
        for (TimetableEntry e : readRecords()) {
            if (e.getId().equals(id)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized void checkConflict(TimetableEntry candidate, String excludeId)
            throws IOException, TimetableConflictException {
        if (candidate == null) return;
        List<TimetableEntry> entries = readRecords();
        for (TimetableEntry other : entries) {
            if (excludeId != null && other.getId().equals(excludeId)) {
                continue;
            }
            if (!other.getDayOfWeek().equalsIgnoreCase(candidate.getDayOfWeek())
                    || other.getPeriodNumber() != candidate.getPeriodNumber()) {
                continue;
            }

            // 1. Check Class / Student Batch Conflict
            boolean sameDept = other.getDepartment().equalsIgnoreCase(candidate.getDepartment());
            boolean sameSem = other.getSemester() == candidate.getSemester();
            boolean batchOverlap = other.getBatch().equalsIgnoreCase("All Batches")
                    || candidate.getBatch().equalsIgnoreCase("All Batches")
                    || other.getBatch().equalsIgnoreCase(candidate.getBatch());
            if (sameDept && sameSem && batchOverlap) {
                throw new TimetableConflictException("CLASS_CONFLICT",
                        "Class Overlap: " + candidate.getDepartment() + " Sem " + candidate.getSemester()
                        + " (" + candidate.getBatch() + ") already has '" + other.getSubjectName()
                        + "' scheduled on " + candidate.getDayOfWeek() + " Period " + candidate.getPeriodNumber() + ".");
            }

            // 2. Check Faculty Double-Booking Conflict
            if (other.getFacultyUsername().equalsIgnoreCase(candidate.getFacultyUsername())) {
                throw new TimetableConflictException("FACULTY_CONFLICT",
                        "Faculty Conflict: " + candidate.getFacultyName() + " is already assigned to '"
                        + other.getSubjectName() + "' (Room " + other.getRoomNumber() + ") on "
                        + candidate.getDayOfWeek() + " Period " + candidate.getPeriodNumber() + ".");
            }

            // 3. Check Classroom / Lab Conflict
            if (other.getRoomNumber().equalsIgnoreCase(candidate.getRoomNumber())) {
                throw new TimetableConflictException("ROOM_CONFLICT",
                        "Room Conflict: Room '" + candidate.getRoomNumber() + "' is already occupied by '"
                        + other.getSubjectName() + "' (" + other.getFacultyName() + ") on "
                        + candidate.getDayOfWeek() + " Period " + candidate.getPeriodNumber() + ".");
            }
        }
    }

    @Override
    public synchronized TimetableEntry save(TimetableEntry entry) throws IOException, TimetableConflictException {
        if (entry == null) {
            throw new IllegalArgumentException("Timetable entry is required.");
        }
        checkConflict(entry, entry.getId());

        List<TimetableEntry> records = readRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId().equals(entry.getId())) {
                records.set(i, entry);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(entry);
        }
        writeRecords(records);
        return entry;
    }

    @Override
    public synchronized boolean delete(String id) throws IOException {
        if (id == null) return false;
        List<TimetableEntry> records = readRecords();
        boolean removed = records.removeIf(e -> e.getId().equals(id));
        if (removed) {
            writeRecords(records);
        }
        return removed;
    }

    private void sortEntries(List<TimetableEntry> list) {
        list.sort(Comparator.comparingInt((TimetableEntry e) -> getDayIndex(e.getDayOfWeek()))
                .thenComparingInt(TimetableEntry::getPeriodNumber)
                .thenComparing(TimetableEntry::getDepartment)
                .thenComparingInt(TimetableEntry::getSemester));
    }

    private int getDayIndex(String day) {
        for (int i = 0; i < TimetableEntry.DAYS_OF_WEEK.length; i++) {
            if (TimetableEntry.DAYS_OF_WEEK[i].equalsIgnoreCase(day)) {
                return i;
            }
        }
        return 99;
    }

    private List<TimetableEntry> readRecords() throws IOException {
        if (!Files.exists(storageFile)) {
            if (DEFAULT_FILE.equals(storageFile)) {
                List<TimetableEntry> seed = createDefaultSchedule();
                writeRecords(seed);
                return seed;
            }
            return new ArrayList<>();
        }
        List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
        if (lines.isEmpty() && DEFAULT_FILE.equals(storageFile)) {
            List<TimetableEntry> seed = createDefaultSchedule();
            writeRecords(seed);
            return seed;
        }
        List<TimetableEntry> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] fields = line.split("\t", -1);
            if (fields.length < 13) {
                throw new IOException("Malformed timetable row at " + storageFile + ":" + (i + 1));
            }
            try {
                records.add(new TimetableEntry(
                        decode(fields[0]),
                        decode(fields[1]),
                        Integer.parseInt(fields[2]),
                        decode(fields[3]),
                        decode(fields[4]),
                        Integer.parseInt(fields[5]),
                        decode(fields[6]),
                        decode(fields[7]),
                        decode(fields[8]),
                        decode(fields[9]),
                        decode(fields[10]),
                        decode(fields[11]),
                        LocalDateTime.parse(decode(fields[12]))
                ));
            } catch (Exception e) {
                throw new IOException("Invalid timetable data at " + storageFile + ":" + (i + 1), e);
            }
        }
        return records;
    }

    private void writeRecords(List<TimetableEntry> records) throws IOException {
        Path parent = storageFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tempFile = Files.createTempFile(parent, "timetable-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (TimetableEntry e : records) {
                lines.add(encode(e.getId()) + "\t"
                        + encode(e.getDayOfWeek()) + "\t"
                        + e.getPeriodNumber() + "\t"
                        + encode(e.getTimeSlot()) + "\t"
                        + encode(e.getDepartment()) + "\t"
                        + e.getSemester() + "\t"
                        + encode(e.getBatch()) + "\t"
                        + encode(e.getSubjectCode()) + "\t"
                        + encode(e.getSubjectName()) + "\t"
                        + encode(e.getFacultyUsername()) + "\t"
                        + encode(e.getFacultyName()) + "\t"
                        + encode(e.getRoomNumber()) + "\t"
                        + encode(e.getUpdatedAt().toString()));
            }
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, storageFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, storageFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private List<TimetableEntry> createDefaultSchedule() {
        List<TimetableEntry> list = new ArrayList<>();
        String dept = "Computer Science & Engineering";
        int sem = 3;
        String batch = "2024-2028 (CS4)";
        String facUser = "faculty_cs";
        String facName = "Prof. Rajesh Kumar";

        // Monday
        list.add(TimetableEntry.create("Monday", 1, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Monday", 2, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Monday", 3, dept, sem, batch, "CST205", "Discrete Mathematical Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Monday", 4, dept, sem, batch, "EST200", "Design & Engineering", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Monday", 5, dept, sem, batch, "CSL201", "Object Oriented Programming Lab", facUser, facName, "CS-Lab 2"));
        list.add(TimetableEntry.create("Monday", 6, dept, sem, batch, "CSL201", "Object Oriented Programming Lab", facUser, facName, "CS-Lab 2"));

        // Tuesday
        list.add(TimetableEntry.create("Tuesday", 1, dept, sem, batch, "CST205", "Discrete Mathematical Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Tuesday", 2, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Tuesday", 3, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Tuesday", 4, dept, sem, batch, "MCN201", "Sustainable Engineering", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Tuesday", 5, dept, sem, batch, "CSL203", "Data Structures Lab", facUser, facName, "CS-Lab 1"));
        list.add(TimetableEntry.create("Tuesday", 6, dept, sem, batch, "CSL203", "Data Structures Lab", facUser, facName, "CS-Lab 1"));

        // Wednesday
        list.add(TimetableEntry.create("Wednesday", 1, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Wednesday", 2, dept, sem, batch, "CST205", "Discrete Mathematical Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Wednesday", 3, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Wednesday", 4, dept, sem, batch, "EST200", "Design & Engineering", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Wednesday", 5, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Wednesday", 6, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));

        // Thursday
        list.add(TimetableEntry.create("Thursday", 1, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Thursday", 2, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Thursday", 3, dept, sem, batch, "MCN201", "Sustainable Engineering", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Thursday", 4, dept, sem, batch, "CST205", "Discrete Mathematical Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Thursday", 5, dept, sem, batch, "CSL201", "Object Oriented Programming Lab", facUser, facName, "CS-Lab 2"));
        list.add(TimetableEntry.create("Thursday", 6, dept, sem, batch, "CSL201", "Object Oriented Programming Lab", facUser, facName, "CS-Lab 2"));

        // Friday
        list.add(TimetableEntry.create("Friday", 1, dept, sem, batch, "CST205", "Discrete Mathematical Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Friday", 2, dept, sem, batch, "CST201", "Object Oriented Programming with Java", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Friday", 3, dept, sem, batch, "CST203", "Data Structures", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Friday", 4, dept, sem, batch, "EST200", "Design & Engineering", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Friday", 5, dept, sem, batch, "CST201", "Object Oriented Programming (Tutorial)", facUser, facName, "LH-302"));
        list.add(TimetableEntry.create("Friday", 6, dept, sem, batch, "CST203", "Data Structures (Remedial)", facUser, facName, "LH-302"));

        return list;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
