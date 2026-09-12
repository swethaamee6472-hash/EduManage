package com.sset.erp.test;

import com.sset.erp.dto.AuthResult;
import com.sset.erp.dto.UserRegistrationDTO;
import com.sset.erp.model.*;
import com.sset.erp.service.AuthService;
import com.sset.erp.service.impl.AuthServiceImpl;
import com.sset.erp.session.SessionManager;
import com.sset.erp.util.PasswordUtil;
import java.util.List;

/**
 * Automated Verification Test Suite for Module 1.
 * Tests OOP principles, cryptographic security, role inheritance, and service logic.
 */
public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" EduManage - Module 1 Verification Test Suite");
        System.out.println("==========================================================\n");

        testPasswordSecurity();
        testOOPInheritanceAndPolymorphism();
        testAuthenticationFlow();
        testRegistrationAndValidation();
        testSessionManagerSingleton();

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

        AuthService service = new AuthServiceImpl();

        // 1. Valid Student Registration
        String testUser = "test_user_" + (System.currentTimeMillis() % 100000);
        UserRegistrationDTO validStudent = new UserRegistrationDTO();
        validStudent.setUsername(testUser);
        validStudent.setPassword("ragendu123");
        validStudent.setFullName("Ragendu M");
        validStudent.setEmail(testUser + "@edumanage.edu");
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
        invalidDto.setEmail("invalid-email"); // missing @ or .
        List<String> errors = invalidDto.validate();
        assertTrue("Validation catches short username", errors.stream().anyMatch(e -> e.contains("Username")));
        assertTrue("Validation catches short password", errors.stream().anyMatch(e -> e.contains("Password")));
        assertTrue("Validation catches blank full name", errors.stream().anyMatch(e -> e.contains("Full name")));
        assertTrue("Validation catches bad email", errors.stream().anyMatch(e -> e.contains("valid email")));
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

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  ✓ PASS: " + testName);
            passed++;
        } else {
            System.err.println("  ✗ FAIL: " + testName);
            failed++;
        }
    }
}
