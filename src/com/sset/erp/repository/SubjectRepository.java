package com.sset.erp.repository;

import com.sset.erp.model.Subject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing academic course and subject catalogues.
 */
public interface SubjectRepository {
    List<Subject> findAll() throws IOException;
    Optional<Subject> findByCode(String code) throws IOException;
    List<Subject> findByDepartmentAndSemester(String department, int semester) throws IOException;
    Subject save(Subject subject) throws IOException;
    boolean delete(String code) throws IOException;
}
