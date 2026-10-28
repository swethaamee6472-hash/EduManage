package com.sset.erp.repository;

import com.sset.erp.model.TimetableConflictException;
import com.sset.erp.model.TimetableEntry;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for timetable schedules and slot conflict management.
 */
public interface TimetableRepository {
    List<TimetableEntry> findAll() throws IOException;
    List<TimetableEntry> findByDepartmentAndSemester(String department, int semester) throws IOException;
    List<TimetableEntry> findByDepartmentSemesterAndBatch(String department, int semester, String batch) throws IOException;
    List<TimetableEntry> findByFaculty(String facultyUsername) throws IOException;
    Optional<TimetableEntry> findById(String id) throws IOException;
    TimetableEntry save(TimetableEntry entry) throws IOException, TimetableConflictException;
    boolean delete(String id) throws IOException;
    void checkConflict(TimetableEntry candidate, String excludeId) throws IOException, TimetableConflictException;
}
