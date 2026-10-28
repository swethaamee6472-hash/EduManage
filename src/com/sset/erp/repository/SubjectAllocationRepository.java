package com.sset.erp.repository;

import com.sset.erp.model.SubjectAllocation;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing faculty subject allocations.
 */
public interface SubjectAllocationRepository {
    List<SubjectAllocation> findAll() throws IOException;
    List<SubjectAllocation> findByFaculty(String facultyUsername) throws IOException;
    List<SubjectAllocation> findByDepartmentAndSemester(String department, int semester) throws IOException;
    Optional<SubjectAllocation> findById(String id) throws IOException;
    SubjectAllocation save(SubjectAllocation allocation) throws IOException;
    boolean delete(String id) throws IOException;
}
