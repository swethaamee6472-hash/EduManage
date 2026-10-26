package com.sset.erp.repository;

import com.sset.erp.model.StudentNotification;
import java.io.IOException;
import java.util.List;

/**
 * Data access contract for notices shown in the student portal.
 */
public interface StudentNotificationRepository {
    List<StudentNotification> findAll() throws IOException;
    List<StudentNotification> findForStudent(String rollNumber) throws IOException;
    StudentNotification save(StudentNotification notification) throws IOException;
}
