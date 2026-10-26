package com.sset.erp.repository;

import com.sset.erp.model.AssessmentRecord;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for student assessment results.
 */
public interface AssessmentRepository {
    List<AssessmentRecord> findAll() throws IOException;
    List<AssessmentRecord> findByRollNumber(String rollNumber) throws IOException;
    Optional<AssessmentRecord> findById(String id) throws IOException;
    AssessmentRecord save(AssessmentRecord record) throws IOException;
    boolean deleteById(String id) throws IOException;
}
