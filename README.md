# EduManage - Student Data Management System

**Project:** EduManage ERP  
**Group:** 2 (Subin P Saji, Ragendu M, Vismaya Sajith, Swetha Sathyan)  
**Class:** S3 BTech CS4  

---

## 🎯 Project Overview
This project is an enterprise-grade **College Student Data Management System (ERP)** developed in Java demonstrating core **Object-Oriented Programming (OOP) concepts**.

This repository contains **Module 1: Authentication & Role Management (Admin, Faculty, Student, Parent)**, **Module 5: Marks, Grade & CGPA Management**, **Module 6: Student Portal**, **Module 7: Parent Portal**, **Module 8: Dashboard Analytics**, **Module 9: Instant Notifications**, and **Module 10: Fee Management**, built as an interactive desktop GUI application using **Java Swing**.

### Module 5: Marks, Grade & CGPA Management
- Faculty can add, review, edit, and delete a student's assessment marks, including subject credits.
- Assessment percentages map to S (90+, 10 points), A+ (85+, 9), A (80+, 8), B+ (70+, 7), B (60+, 6), C (50+, 5), P (40+, 4), and F (below 40, 0).
- A subject result combines the student's recorded assessments by total earned marks divided by total possible marks. CGPA is the credit-weighted average of the subject grade points; keep a subject's credit value consistent across its assessments (the latest record supplies the subject credit value).
- Students can view assessment marks, subject grades, overall percentage, and CGPA. Parents can view the linked student's marks and CGPA.
- Assessment records persist in `data/assessments.tsv`. Existing eight-field rows remain readable with a one-credit default; new rows store the subject credit value.

### Module 6: Student Portal
- Students can view their profile, marks, attendance history, and notifications from the student dashboard.
- Faculty can record attendance by scanning a student's QR code and entering the subject; duplicate scans for the same student, subject, and day are ignored.
- Faculty can publish notices to all students or target an individual student. Each student's inbox shows general notices and notices addressed to that roll number.
- Attendance and notifications persist in `data/attendance.tsv` and `data/notifications.tsv`.

### Module 7: Parent Portal
- Parents can review attendance history and assessment marks for the student roll number linked to their account.
- The portal shows the recorded attendance count and academic progress/CGPA. It does not invent an attendance percentage because total scheduled classes are not currently tracked.
- The detailed progress view separates attendance and assessment records and can be refreshed.

### Module 8: Dashboard Analytics
- Administrators can open Dashboard Analytics from the user management dashboard.
- Summary metrics report account totals, active accounts, attendance records, and the weighted assessment average.
- Charts show account counts by role, attendance records by subject, and weighted marks averages by subject.
- Analytics are calculated from current persisted application data and can be refreshed.

### Module 9: Instant Notifications
- Student notifications are categorized as attendance, exam, event, fee, or announcement updates.
- Recording attendance or saving assessment marks immediately writes a targeted notification for the student.
- Faculty can publish event, exam, and announcement notices to all students or an individual student.
- The student inbox checks for new notifications every three seconds while open and indicates newly received updates.
- Existing notification files remain readable; older notices without a category are treated as announcements.

### Module 10: Fee Management
- Administrators can assign fee charges to students, review paid and outstanding balances, and record full or partial payments with a payment method.
- Each recorded payment creates a unique receipt record. Administrators can view and save the receipt as a text file.
- Students can view only their own fee balances and receipt history; parents can view the history for the student linked to their account.
- Students receive a fee notification after a payment is recorded. If notification saving fails, the saved payment remains intact and the error is reported separately.
- Payments are recorded by staff; this module does not process online transactions or collect money.
- Charges and payment records persist in `data/fee_charges.tsv` and `data/fee_payments.tsv`.

---

## 🚀 Quick Start (1-Command Execution)

### Prerequisites
- Java JDK 17 or higher (tested on Java 26)

### Run the Application (GUI)
```bash
./compile_and_run.sh
```

### Run the Automated Test Suite
```bash
./compile_and_run.sh --test
```

---

## 🔑 Pre-Seeded Demo Credentials

For quick evaluation and viva demonstrations, the login screen includes **Quick Demo Pills** to autofill credentials with a single click:

| Role | Username | Password | User Profile Details |
|------|----------|----------|----------------------|
| **Administrator** | `admin` | `admin123` | **Dr. Litty Koshy** (Project Coordinator / HOD) |
| **Faculty** | `faculty_cs` | `faculty123` | **Prof. Rajesh Kumar** (Assoc. Professor, CSE) |
| **Student** | `student_cs` | `student123` | **Swetha Sathyan** (Roll: `SSET24CS042`, S3 CS4) |
| **Parent** | `parent_cs` | `parent123` | **Sathyan K** (Ward Roll: `SSET24CS042`) |

---

## 🏛️ Object-Oriented Programming (OOP) Principles Demonstrated

This module explicitly models real-world entities through fundamental OOP concepts:

### 1. Encapsulation
- All domain entity fields (`id`, `username`, `passwordHash`, `salt`, `email`, etc.) are declared `private`.
- State mutation and retrieval are strictly handled via accessors and mutators (`validatePassword()`, `setPassword()`, getters/setters).
- Cryptographic salts and SHA-256 hashes protect user passwords against plain-text exposure.

### 2. Inheritance
- Base class `com.sset.erp.model.User` provides common attributes and operations (`id`, `username`, `createdAt`, `active`, `validatePassword()`).
- Derived specialized classes extend `User`:
  - `AdminUser`: Adds `adminLevel`
  - `FacultyUser`: Adds `employeeId`, `department`, `designation`
  - `StudentUser`: Adds `rollNumber`, `department`, `semester`, `batch`
  - `ParentUser`: Adds `studentRollNumber`, `emergencyContact`

### 3. Polymorphism & Abstraction
- Abstract methods defined in `User` and overridden by child classes:
  - `getRoleBadge()`: Each subclass provides a customized role badge representation.
  - `getProfileDetails()`: Polymorphically yields key-value attributes for dynamic portal display.
- Interface Abstraction:
  - `AuthService` defines authentication operations decoupled from `AuthServiceImpl`.
  - `UserRepository` defines the data access contract decoupled from `FileUserRepository`.

### 4. Software Design Patterns
- **Singleton Pattern (`SessionManager`)**: Guarantees a single authenticated user session throughout the application lifecycle.
- **Data Transfer Object (DTO) (`UserRegistrationDTO`)**: Decouples UI form fields and validation rules from persistent domain models.
- **Factory Pattern**: The service dynamically instantiates the correct `User` subclass during registration based on the selected `Role`.
- **Repository Pattern**: Centralizes entity persistence and retrieval logic into `UserRepository`.

---

## 📁 Project Directory Structure

```
OOP PROJECT/
├── compile_and_run.sh                     # Bash compilation & launcher script
├── data/
│   └── users.json                         # Persistent JSON user store (auto-seeded)
│   └── assessments.tsv                    # Persistent student assessment results
│   └── attendance.tsv                     # Persistent QR attendance records
│   └── notifications.tsv                  # Persistent student notifications
│   └── fee_charges.tsv                    # Persistent assigned student fees
│   └── fee_payments.tsv                   # Persistent payment and receipt records
├── bin/                                   # Compiled bytecode (.class files)
├── src/
│   └── com/sset/erp/
│       ├── Main.java                      # GUI application entry point
│       ├── model/
│       │   ├── Role.java                  # Role enum (ADMIN, FACULTY, STUDENT, PARENT)
│       │   ├── User.java                  # Abstract base User entity
│       │   ├── AdminUser.java             # Admin subclass
│       │   ├── FacultyUser.java           # Faculty subclass
│       │   ├── StudentUser.java           # Student subclass
│       │   └── ParentUser.java            # Parent subclass
│       ├── dto/
│       │   ├── AuthResult.java            # Authentication response DTO
│       │   └── UserRegistrationDTO.java   # Registration payload DTO with validation
│       ├── repository/
│       │   ├── UserRepository.java        # Repository interface (Abstraction)
│       │   └── FileUserRepository.java    # JSON persistent repository
│       │   ├── AssessmentRepository.java  # Assessment data access contract
│       │   ├── FileAssessmentRepository.java # Persistent assessment records
│       │   ├── AttendanceRepository.java  # Attendance data access contract
│       │   ├── FileAttendanceRepository.java # Persistent attendance records
│       │   ├── StudentNotificationRepository.java # Student notice contract
│       │   └── FileStudentNotificationRepository.java # Persistent notifications
│       │   ├── FeeRepository.java          # Fee ledger data access contract
│       │   └── FileFeeRepository.java      # Persistent charges and receipts
│       │   └── NotificationType.java      # Attendance, exam, event, announcement categories
│       ├── model/
│       │   ├── AssessmentRecord.java      # Validated marks, subject credits, and grade
│       │   └── AcademicPerformanceSummary.java # Subject grades and credit-weighted CGPA
│       │   ├── AttendanceRecord.java      # Student attendance entry
│       │   └── StudentNotification.java   # General or targeted student notice
│       │   ├── FeeCharge.java              # Student fee obligation
│       │   └── FeePayment.java             # Immutable payment and receipt record
│       │   └── DashboardAnalytics.java    # Aggregated dashboard metrics
│       ├── service/
│       │   ├── AuthService.java           # Service interface
│       │   └── impl/
│       │       └── AuthServiceImpl.java   # Service implementation & factory
│       ├── session/
│       │   └── SessionManager.java        # Singleton session state manager
│       ├── util/
│       │   ├── PasswordUtil.java          # SHA-256 + Salt hashing & constant-time verify
│       │   └── UITheme.java               # Modern design tokens, colors & typography
│       ├── ui/
│       │   ├── LoginFrame.java            # Branded login window with demo buttons
│       │   ├── RegisterDialog.java        # Dynamic role-adaptive registration modal
│       │   ├── components/
│       │   │   ├── ModernButton.java      # Rounded hover-animated buttons
│       │   │   ├── ModernTextField.java   # Rounded text field with focus outline
│       │   │   ├── ModernPasswordField.java # Password field with Show/Hide toggle
│       │   │   ├── ModernCard.java        # Rounded shadow container panel
│       │   │   └── BadgeLabel.java        # Pill badge with role colors
│       │   └── dashboard/
│       │       ├── MainDashboardFrame.java    # Top bar & dynamic dashboard host
│       │       ├── AdminDashboardPanel.java   # Admin User Management & Stats table
│       │       ├── FacultyDashboardPanel.java # Faculty academic profile & modules
│       │       ├── StudentDashboardPanel.java # Student records & Digital ID preview
│       │       └── ParentDashboardPanel.java  # Parent ward health & alerts
│       │   ├── AssessmentManagementDialog.java # Faculty assessment management
│       │   ├── StudentAssessmentDialog.java # Student read-only results
│       │   ├── StudentAttendanceDialog.java # Student attendance history
│       │   ├── StudentNotificationsDialog.java # Student notifications inbox
│       │   └── FacultyNotificationsDialog.java # Faculty notice publishing
│       │   ├── FeeManagementDialog.java    # Admin charge/payment entry and receipts
│       │   └── FeeHistoryDialog.java       # Student/parent fee history
│       │   ├── ParentProgressDialog.java    # Parent view of ward attendance and marks
│       │   └── DashboardAnalyticsDialog.java # Admin analytics metrics and charts
│       └── test/
│           └── TestRunner.java            # Automated verification suite
└── README.md
```

---

## 🧪 Verification & Automated Tests
The built-in test suite verifies 10 core subsystems:
1. **Password Security**: Unique salt generation, SHA-256 hashing, constant-time verification.
2. **OOP Polymorphism**: Subclass badge resolution and metadata mapping.
3. **Authentication**: Success across all 4 roles, bad password rejection, unknown user rejection.
4. **Registration & Validation**: DTO field validation, duplicate username rejection.
5. **Session Management**: Singleton state preservation and cleanup.
6. **Marks, Grade & CGPA**: Score/credit validation, legacy migration, grade boundaries, and credit-weighted CGPA.
7. **Student Portal & Notifications**: Attendance persistence, category round-trips, migration of legacy notices, targeted delivery, and inbox privacy.
8. **Parent Portal**: Attendance and marks lookups restricted to the parent's linked ward.
9. **Dashboard Analytics**: Account counts, attendance activity, and weighted assessment averages.
10. **Fee Management**: Charge/payment persistence, partial and full balances, receipt uniqueness, overpayment rejection, and student-scoped lookups.
