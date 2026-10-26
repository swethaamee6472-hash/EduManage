package com.sset.erp.repository;

import com.sset.erp.model.AttendanceRecord;
import java.io.IOException;
import java.util.List;

/**
 * Data access contract for attendance history.
 */
public interface AttendanceRepository {
    List<AttendanceRecord> findAll() throws IOException;
    List<AttendanceRecord> findByRollNumber(String rollNumber) throws IOException;
    AttendanceRecord save(AttendanceRecord record) throws IOException;
}
