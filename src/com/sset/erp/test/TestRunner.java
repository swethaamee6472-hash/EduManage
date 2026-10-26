package com.sset.erp.test;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.*;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileFeeRepository;
import com.sset.erp.repository.FileStudentNotificationRepository;
import com.sset.erp.repository.UserRepository;
import com.sset.erp.service.AuthService;
import com.sset.erp.service.impl.AuthServiceImpl;
import com.sset.erp.session.SessionManager;
import com.sset.erp.util.PasswordUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Automated verification suite for Modules 1, 5, and 6.
 * Tests OOP principles, cryptographic security, role inheritance, and service logic.
 */
public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" EduManage - Modules 1, 5-10 Verification Test Suite");
        System.out.println("==========================================================\n");

        testPasswordSecurity();
        testOOPInheritanceAndPolymorphism();
        testAuthenticationFlow();
        testRegistrationAndValidation();
        testSessionManagerSingleton();
        testAssessmentRepository();
        testStudentPortalRepositories();
        testParentWardProgressAccess();
        testDashboardAnalytics();
        testFeeManagement();

        System.out.println("\n----------------------------------------------------------");
        System.out.println("TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("----------------------------------------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testPasswordSecurity() {
        System.out.println("[TEST GROUP 1] Password Hashing & Cryptographic Salt");

        String rawPassword = "securePassword123";
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        // 1. Verify salts are unique
        assertTrue("Salts must be cryptographically unique", !salt1.equals(salt2));

        // 2. Verify hashing
        String hash1 = PasswordUtil.hashPassword(rawPassword, salt1);
        String hash2 = PasswordUtil.hashPassword(rawPassword, salt2);
        assertTrue("Same password with different salts must produce distinct hashes", !hash1.equals(hash2));

        // 3. Verify correct verification
        assertTrue("Correct password must verify successfully", PasswordUtil.verifyPassword(rawPassword, hash1, salt1));
        assertTrue("Wrong password must fail verification", !PasswordUtil.verifyPassword("wrongPass", hash1, salt1));
        assertTrue("Wrong salt must fail verification", !PasswordUtil.verifyPassword(rawPassword, hash1, salt2));
    }

    private static void testOOPInheritanceAndPolymorphism() {
        System.out.println("\n[TEST GROUP 2] OOP Inheritance, Abstraction & Polymorphism");

        User admin = new AdminUser("A1", "admin_t", "pass", "Admin T", "admin@sset.ac.in", "Lead Coordinator");
        User faculty = new FacultyUser("F1", "fac_t", "pass", "Prof T", "fac@sset.ac.in", "EMP-99", "CSE", "HOD");
        User student = new StudentUser("S1", "stu_t", "pass", "Stu T", "stu@sset.ac.in", "SSET24CS001", "CSE", 3, "2024-28");
        User parent = new ParentUser("P1", "par_t", "pass", "Par T", "par@sset.ac.in", "SSET24CS001", "+91 9999999999");

        // Polymorphic invocation of abstract getRoleBadge()
        assertTrue("Admin role badge polymorphism", admin.getRoleBadge().contains("ADMIN"));
        assertTrue("Faculty role badge polymorphism", faculty.getRoleBadge().contains("FACULTY"));
        assertTrue("Student role badge polymorphism", student.getRoleBadge().contains("STUDENT"));
        assertTrue("Parent role badge polymorphism", parent.getRoleBadge().contains("PARENT"));

        // Polymorphic invocation of getProfileDetails()
        assertTrue("Admin profile contains Admin Level", admin.getProfileDetails().containsKey("Admin Level"));
        assertTrue("Faculty profile contains Employee ID", faculty.getProfileDetails().containsKey("Employee ID"));
        assertTrue("Student profile contains Roll Number", student.getProfileDetails().containsKey("Roll Number"));
        assertTrue("Parent profile contains Ward Roll Number", parent.getProfileDetails().containsKey("Ward Roll Number"));
    }

    private static void testAuthenticationFlow() {
        System.out.println("\n[TEST GROUP 3] Multi-Role Authentication Flow");

        AuthService service = new AuthServiceImpl();

        // Test login for all 4 seed demo roles
        AuthResult adminLogin = service.login("admin", "admin123");
        assertTrue("Admin demo account login success", adminLogin.isSuccess() && adminLogin.getUser().getRole() == Role.ADMIN);

        AuthResult facLogin = service.login("faculty_cs", "faculty123");
        assertTrue("Faculty demo account login success", facLogin.isSuccess() && facLogin.getUser().getRole() == Role.FACULTY);

        AuthResult stuLogin = service.login("student_cs", "student123");
        assertTrue("Student demo account login success", stuLogin.isSuccess() && stuLogin.getUser().getRole() == Role.STUDENT);

        AuthResult parLogin = service.login("parent_cs", "parent123");
        assertTrue("Parent demo account login success", parLogin.isSuccess() && parLogin.getUser().getRole() == Role.PARENT);

        // Test bad password
        AuthResult badPass = service.login("admin", "wrongPassword");
        assertTrue("Bad password rejection", !badPass.isSuccess());

        // Test non-existent user
        AuthResult unknownUser = service.login("nonexistent_user", "password");
        assertTrue("Unknown username rejection", !unknownUser.isSuccess());
    }

    private static void testRegistrationAndValidation() {
        System.out.println("\n[TEST GROUP 4] User Registration & Validation (DTO Pattern)");

        AuthService service = new AuthServiceImpl(new TestUserRepository(), SessionManager.getInstance());

        // 1. Valid Student Registration
        String testUser = "test_user_" + (System.currentTimeMillis() % 100000);
        UserRegistrationDTO validStudent = new UserRegistrationDTO();
        validStudent.setUsername(testUser);
        validStudent.setPassword("ragendu123");
        validStudent.setFullName("Ragendu M");
        validStudent.setEmail(testUser + "@gmail.com");
        validStudent.setRole(Role.STUDENT);
        validStudent.setRollNumber("SSET24CS015");
        validStudent.setDepartment("Computer Science & Engineering");
        validStudent.setSemester(3);
        validStudent.setBatch("2024-2028 (CS4)");

        AuthResult regResult = service.register(validStudent);
        assertTrue("Valid student registration success", regResult.isSuccess());

        // 2. Duplicate Username Rejection
        AuthResult dupResult = service.register(validStudent);
        assertTrue("Duplicate username rejection", !dupResult.isSuccess());
        // 3. Validation Rules (short password, invalid email)
        UserRegistrationDTO invalidDto = new UserRegistrationDTO();
        invalidDto.setUsername("ab"); // too short (< 3)
        invalidDto.setPassword("123"); // too short (< 6)
        invalidDto.setFullName(""); // blank
        invalidDto.setEmail("invalid-email"); // not gmail
        List<String> errors = invalidDto.validate();
        assertTrue("Validation catches short username", errors.stream().anyMatch(e -> e.contains("Username")));
        assertTrue("Validation catches short password", errors.stream().anyMatch(e -> e.contains("Password")));
        assertTrue("Validation catches blank full name", errors.stream().anyMatch(e -> e.contains("Full name")));
        assertTrue("Validation catches bad email", errors.stream().anyMatch(e -> e.contains("Gmail")));

        // Test non-gmail domain rejection
        invalidDto.setEmail("user@yahoo.com");
        assertTrue("Validation rejects non-gmail addresses", invalidDto.validate().stream().anyMatch(e -> e.contains("Gmail")));
    }

    private static void testSessionManagerSingleton() {
        System.out.println("\n[TEST GROUP 5] SessionManager Singleton Verification");

        SessionManager s1 = SessionManager.getInstance();
        SessionManager s2 = SessionManager.getInstance();
        assertTrue("SessionManager returns the same singleton instance", s1 == s2);

        User testUser = new StudentUser("S9", "ses_stu", "pass", "Session Stu", "ses@sset.ac.in", "S99", "CSE", 3, "2024");
        s1.setCurrentUser(testUser);
        assertTrue("SessionManager stores authenticated user", s2.isLoggedIn() && s2.getCurrentUser().equals(testUser));

        s1.logout();
        assertTrue("SessionManager clears session on logout", !s2.isLoggedIn() && s2.getCurrentUser() == null);
    }

    private static void testAssessmentRepository() {
        System.out.println("\n[TEST GROUP 6] Module 5 Assessment Records");
        Path directory = null;
        Path storageFile = null;
        try {
            directory = Files.createTempDirectory("edumanage-assessment-test-");
            storageFile = directory.resolve("assessments.tsv");
            FileAssessmentRepository repository = new FileAssessmentRepository(storageFile);
            AssessmentRecord record = AssessmentRecord.create(
                    "SSET24CS001", "Object Oriented Programming", "Internal Test 1",
                    42, 50, 4, "Prof Test");

            assertTrue("New assessment store starts empty", repository.findAll().isEmpty());
            repository.save(record);
            FileAssessmentRepository reopenedRepository = new FileAssessmentRepository(storageFile);
            assertTrue("Assessment persists and can be found by roll number",
                    reopenedRepository.findByRollNumber("sset24cs001").size() == 1);
            assertTrue("Assessment percentage is calculated correctly", record.getPercentage() == 84.0);
            assertTrue("Assessment grade and grade point are calculated correctly",
                    record.getGrade().equals("A") && record.getGradePoint() == 8);
            assertTrue("Subject credits persist with assessment marks",
                    reopenedRepository.findByRollNumber("SSET24CS001").get(0).getSubjectCredits() == 4);

            AssessmentRecord updated = new AssessmentRecord(record.getId(), record.getRollNumber(),
                    record.getSubject(), record.getAssessmentName(), 45, 50, 4, "Prof Test",
                    record.getRecordedAt());
            reopenedRepository.save(updated);
            assertTrue("Saving an existing ID updates rather than duplicates it",
                    reopenedRepository.findAll().size() == 1
                            && reopenedRepository.findById(record.getId()).get().getScore() == 45);

            boolean rejectedInvalidScore = false;
            try {
                AssessmentRecord.create("SSET24CS001", "OOP", "Invalid", 51, 50, "Prof Test");
            } catch (IllegalArgumentException e) {
                rejectedInvalidScore = true;
            }
            assertTrue("Assessment rejects scores outside the maximum", rejectedInvalidScore);
            assertTrue("Assessment can be deleted", reopenedRepository.deleteById(record.getId())
                    && reopenedRepository.findAll().isEmpty());
            testLegacyAssessmentCredits(directory);

            AssessmentRecord highCredit = AssessmentRecord.create("CGPA-001", "Algorithms", "Combined tests",
                    90, 100, 3, "Faculty");
            AssessmentRecord lowCredit = AssessmentRecord.create("CGPA-001", "Databases", "Final exam",
                    70, 100, 1, "Faculty");
            AcademicPerformanceSummary summary = AcademicPerformanceSummary.from(List.of(highCredit, lowCredit));
            assertTrue("CGPA is credit-weighted across subject grades",
                    Math.abs(summary.getCgpa() - 9.25) < 0.001
                            && summary.getTotalCredits() == 4);
            assertTrue("Grade scale assigns the configured boundary values",
                    AcademicPerformanceSummary.gradeForPercentage(90).equals("S")
                            && AcademicPerformanceSummary.gradeForPercentage(85).equals("A+")
                            && AcademicPerformanceSummary.gradeForPercentage(80).equals("A")
                            && AcademicPerformanceSummary.gradeForPercentage(70).equals("B+")
                            && AcademicPerformanceSummary.gradeForPercentage(60).equals("B")
                            && AcademicPerformanceSummary.gradeForPercentage(50).equals("C")
                            && AcademicPerformanceSummary.gradeForPercentage(40).equals("P")
                            && AcademicPerformanceSummary.gradeForPercentage(39.99).equals("F"));
        } catch (Exception e) {
            assertTrue("Assessment repository operations complete without errors: " + e.getMessage(), false);
        } finally {
            try {
                if (storageFile != null) {
                    Files.deleteIfExists(storageFile);
                }
                if (directory != null) {
                    Files.deleteIfExists(directory);
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to clean assessment test files: " + e.getMessage());
                failed++;
            }
        }
    }

    private static void testLegacyAssessmentCredits(Path directory) throws Exception {
        Path legacyFile = directory.resolve("legacy-assessments.tsv");
        List<String> fields = List.of(
                "bGVnYWN5LWlk", "U1RVRE5FLTAwMQ", "TGVnYWN5IFN1YmplY3Q", "SW50ZXJuYWwgMQ",
                "45.0", "50.0", "RmFjdWx0eQ", "MjAyNi0wMS0wMVQxMDowMDowMA");
        Files.write(legacyFile, List.of(String.join("\t", fields)), java.nio.charset.StandardCharsets.UTF_8);
        FileAssessmentRepository legacyRepository = new FileAssessmentRepository(legacyFile);
        assertTrue("Legacy assessment rows load with a default one credit",
                legacyRepository.findAll().get(0).getSubjectCredits() == 1);
        Files.deleteIfExists(legacyFile);
    }

    private static void testStudentPortalRepositories() {
        System.out.println("\n[TEST GROUP 7] Student Portal & Notifications");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("edumanage-student-portal-test-");
            Path attendanceFile = directory.resolve("attendance.tsv");
            Path notificationsFile = directory.resolve("notifications.tsv");

            FileAttendanceRepository attendance = new FileAttendanceRepository(attendanceFile);
            AttendanceRecord classRecord = AttendanceRecord.create(
                    "SSET24CS001", "Object Oriented Programming", "Prof Test");
            attendance.save(classRecord);
            attendance.save(AttendanceRecord.create(
                    "SSET24CS001", "Object Oriented Programming", "Prof Test"));
            FileAttendanceRepository reopenedAttendance = new FileAttendanceRepository(attendanceFile);
            assertTrue("Student attendance history persists across repository instances",
                    reopenedAttendance.findByRollNumber("sset24cs001").size() == 1);
            assertTrue("Attendance is isolated by student roll number",
                    reopenedAttendance.findByRollNumber("SSET24CS002").isEmpty());

            FileStudentNotificationRepository notifications =
                    new FileStudentNotificationRepository(notificationsFile);
            notifications.save(StudentNotification.create(StudentNotification.ALL_STUDENTS,
                    "College closed", "Campus is closed tomorrow.", "Faculty"));
            notifications.save(StudentNotification.create(NotificationType.EXAM, "SSET24CS001",
                    "Advising", "Please meet your advisor.", "Faculty"));
            notifications.save(StudentNotification.create(NotificationType.EVENT, "SSET24CS002",
                    "Private notice", "This is for another student.", "Faculty"));
            notifications.save(StudentNotification.create(NotificationType.ATTENDANCE, "SSET24CS001",
                    "Attendance update", "Attendance was marked.", "Faculty"));
            notifications.save(StudentNotification.create(NotificationType.FEE, "SSET24CS001",
                    "Fee payment received", "A payment receipt was created.", "Admin"));
            FileStudentNotificationRepository reopenedNotifications =
                    new FileStudentNotificationRepository(notificationsFile);
            List<StudentNotification> studentInbox = reopenedNotifications.findForStudent("sset24cs001");
            assertTrue("Student inbox includes general and targeted notifications",
                    studentInbox.size() == 4);
            assertTrue("Student inbox excludes another student's targeted notification",
                    studentInbox.stream().noneMatch(n -> n.getTitle().equals("Private notice")));
            assertTrue("Faculty notification list includes targeted and general notices",
                    reopenedNotifications.findAll().size() == 5);
            assertTrue("Notification categories survive persistence",
                    studentInbox.stream().anyMatch(n -> n.getType() == NotificationType.EXAM)
                            && studentInbox.stream().anyMatch(n -> n.getType() == NotificationType.ATTENDANCE)
                            && studentInbox.stream().anyMatch(n -> n.getType() == NotificationType.FEE)
                            && studentInbox.stream().anyMatch(n -> n.getType() == NotificationType.ANNOUNCEMENT)
                            && reopenedNotifications.findAll().stream()
                                    .anyMatch(n -> n.getType() == NotificationType.EVENT));

            Path legacyFile = directory.resolve("legacy-notifications.tsv");
            String legacyLine = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                    "legacy-id".getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "\t"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                            StudentNotification.ALL_STUDENTS.getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "\t"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                            "Old notice".getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "\t"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                            "Legacy message".getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "\t"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                            "Faculty".getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "\t"
                    + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                            java.time.LocalDateTime.now().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Files.write(legacyFile, List.of(legacyLine), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue("Legacy notifications default to announcement category",
                    new FileStudentNotificationRepository(legacyFile).findForStudent("SSET24CS001")
                            .get(0).getType() == NotificationType.ANNOUNCEMENT);
        } catch (Exception e) {
            assertTrue("Student portal repository operations complete without errors: " + e.getMessage(), false);
        } finally {
            try {
                if (directory != null) {
                    try (java.util.stream.Stream<Path> files = Files.list(directory)) {
                        files.forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (java.io.IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
                    }
                    Files.deleteIfExists(directory);
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to clean student portal test files: " + e.getMessage());
                failed++;
            }
        }
    }

    private static void testParentWardProgressAccess() {
        System.out.println("\n[TEST GROUP 8] Module 7 Parent Portal Progress");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("edumanage-parent-progress-test-");
            ParentUser parent = new ParentUser("P1", "parent_test", "pass", "Parent Test",
                    "parent@test.edu", "WARD-001", "555-0100");

            FileAttendanceRepository attendance = new FileAttendanceRepository(directory.resolve("attendance.tsv"));
            attendance.save(AttendanceRecord.create("WARD-001", "Algorithms", "Faculty One"));
            attendance.save(AttendanceRecord.create("WARD-002", "Algorithms", "Faculty One"));
            assertTrue("Parent attendance lookup is restricted to the linked ward",
                    attendance.findByRollNumber(parent.getStudentRollNumber()).size() == 1);

            FileAssessmentRepository assessments = new FileAssessmentRepository(directory.resolve("assessments.tsv"));
            assessments.save(AssessmentRecord.create("WARD-001", "Algorithms", "Test 1", 18, 20, "Faculty One"));
            assessments.save(AssessmentRecord.create("WARD-002", "Algorithms", "Test 1", 8, 10, "Faculty One"));
            List<AssessmentRecord> wardResults =
                    assessments.findByRollNumber(parent.getStudentRollNumber());
            assertTrue("Parent academic progress lookup returns only the linked ward's marks",
                    wardResults.size() == 1 && wardResults.get(0).getScore() == 18);
        } catch (Exception e) {
            assertTrue("Parent progress data operations complete without errors: " + e.getMessage(), false);
        } finally {
            try {
                if (directory != null) {
                    try (java.util.stream.Stream<Path> files = Files.list(directory)) {
                        files.forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (java.io.IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
                    }
                    Files.deleteIfExists(directory);
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to clean parent progress test files: " + e.getMessage());
                failed++;
            }
        }
    }

    private static void testDashboardAnalytics() {
        System.out.println("\n[TEST GROUP 9] Dashboard Analytics");
        List<User> users = List.of(
                new StudentUser("S1", "stu_analytics", "pass", "Student Analytics",
                        "student@analytics.edu", "AN-001", "CSE", 3, "2024"),
                new FacultyUser("F1", "fac_analytics", "pass", "Faculty Analytics",
                        "faculty@analytics.edu", "EMP-1", "CSE", "Lecturer"));
        users.get(1).setActive(false);
        List<AttendanceRecord> attendance = List.of(
                AttendanceRecord.create("AN-001", "Algorithms", "Faculty Analytics"),
                AttendanceRecord.create("AN-001", "Algorithms", "Faculty Analytics"),
                AttendanceRecord.create("AN-001", "Databases", "Faculty Analytics"));
        List<AssessmentRecord> assessments = List.of(
                AssessmentRecord.create("AN-001", "Algorithms", "Test 1", 18, 20, "Faculty Analytics"),
                AssessmentRecord.create("AN-002", "Algorithms", "Test 1", 8, 10, "Faculty Analytics"),
                AssessmentRecord.create("AN-001", "Databases", "Test 1", 30, 40, "Faculty Analytics"));

        DashboardAnalytics analytics = DashboardAnalytics.from(users, attendance, assessments);
        assertTrue("Analytics counts accounts, active users, and students correctly",
                analytics.getTotalUsers() == 2 && analytics.getActiveUsers() == 1
                        && analytics.getStudentCount() == 1);
        assertTrue("Analytics aggregates attendance records per subject",
                analytics.getAttendanceRecordCount() == 3
                        && analytics.getAttendanceBySubject().get("Algorithms") == 2);
        assertTrue("Analytics computes weighted averages by subject and institution",
                Math.abs(analytics.getAssessmentAverageBySubject().get("Algorithms") - (26.0 / 30.0 * 100)) < 0.001
                        && Math.abs(analytics.getWeightedAssessmentAverage() - (56.0 / 70.0 * 100)) < 0.001);
    }

    private static void testFeeManagement() {
        System.out.println("\n[TEST GROUP 10] Fee Management");
        Path directory = null;
        try {
            directory = Files.createTempDirectory("edumanage-fees-test-");
            Path chargesFile = directory.resolve("fee_charges.tsv");
            Path paymentsFile = directory.resolve("fee_payments.tsv");
            FileFeeRepository fees = new FileFeeRepository(chargesFile, paymentsFile);
            FeeCharge wardCharge = FeeCharge.create("FEE-001", "Tuition", new BigDecimal("1000.00"),
                    LocalDate.now().plusDays(20), "Admin");
            FeeCharge otherCharge = FeeCharge.create("FEE-002", "Library", new BigDecimal("250.00"),
                    LocalDate.now().plusDays(20), "Admin");
            fees.saveCharge(wardCharge);
            fees.saveCharge(otherCharge);

            FeePayment firstPayment = fees.recordPayment(wardCharge.getId(), new BigDecimal("300.00"),
                    "Cash", "Admin");
            BigDecimal partialBalance = wardCharge.getAmount().subtract(fees.getPaidAmount(wardCharge.getId()));
            assertTrue("Partial fee payments reduce the balance without closing it",
                    partialBalance.compareTo(new BigDecimal("700.00")) == 0);

            boolean overpaymentRejected = false;
            try {
                fees.recordPayment(wardCharge.getId(), new BigDecimal("700.01"), "Cash", "Admin");
            } catch (IllegalArgumentException e) {
                overpaymentRejected = true;
            }
            assertTrue("Payment exceeding the outstanding balance is rejected", overpaymentRejected);

            FeePayment finalPayment = fees.recordPayment(wardCharge.getId(), new BigDecimal("700.00"),
                    "UPI", "Admin");
            FileFeeRepository reopened = new FileFeeRepository(chargesFile, paymentsFile);
            assertTrue("Fee balances and receipts persist across repository instances",
                    reopened.getPaidAmount(wardCharge.getId()).compareTo(new BigDecimal("1000.00")) == 0
                            && reopened.findPaymentsForCharge(wardCharge.getId()).size() == 2);
            assertTrue("Payments receive distinct receipt numbers",
                    !firstPayment.getReceiptNumber().equals(finalPayment.getReceiptNumber()));
            assertTrue("Student fee lookups include only that student's charges and receipts",
                    reopened.findChargesByStudent("fee-001").size() == 1
                            && reopened.findPaymentsByStudent("FEE-001").size() == 2);
            assertTrue("Another student's fee remains isolated",
                    reopened.findChargesByStudent("FEE-002").size() == 1
                            && reopened.findPaymentsByStudent("FEE-002").isEmpty());
            assertTrue("Fee amounts accept excess trailing zeroes but reject fractions of a cent",
                    FeeCharge.requireAmount(new BigDecimal("1.000")).compareTo(new BigDecimal("1.00")) == 0
                            && rejectsFeeAmount(new BigDecimal("0.001")));
        } catch (Exception e) {
            assertTrue("Fee management operations complete without errors: " + e.getMessage(), false);
        } finally {
            try {
                if (directory != null) {
                    Files.deleteIfExists(directory.resolve("fee_charges.tsv"));
                    Files.deleteIfExists(directory.resolve("fee_payments.tsv"));
                    Files.deleteIfExists(directory);
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to clean fee management test files: " + e.getMessage());
                failed++;
            }
        }
    }

    private static boolean rejectsFeeAmount(BigDecimal amount) {
        try {
            FeeCharge.requireAmount(amount);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  ✓ PASS: " + testName);
            passed++;
        } else {
            System.err.println("  ✗ FAIL: " + testName);
            failed++;
        }
    }

    private static class TestUserRepository implements UserRepository {
        private final List<User> users = new ArrayList<>();

        @Override
        public Optional<User> findById(String id) {
            return users.stream().filter(user -> user.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.stream().filter(user -> user.getUsername().equalsIgnoreCase(username)).findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users);
        }

        @Override
        public List<User> findByRole(Role role) {
            List<User> matches = new ArrayList<>();
            for (User user : users) {
                if (user.getRole() == role) {
                    matches.add(user);
                }
            }
            return matches;
        }

        @Override
        public User save(User user) {
            users.removeIf(existing -> existing.getId().equals(user.getId()));
            users.add(user);
            return user;
        }

        @Override
        public boolean deleteById(String id) {
            return users.removeIf(user -> user.getId().equals(id));
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username).isPresent();
        }

        @Override
        public long count() {
            return users.size();
        }

        @Override
        public long countByRole(Role role) {
            return users.stream().filter(user -> user.getRole() == role).count();
        }
    }
}
