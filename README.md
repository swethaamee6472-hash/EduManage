# EduManage - Student Data Management System

**Project:** EduManage ERP  
**Group:** 2 (Subin P Saji, Ragendu M, Vismaya Sajith, Swetha Sathyan)  
**Class:** S3 BTech CS4  

---

## 🎯 Project Overview
This project is an enterprise-grade **College Student Data Management System (ERP)** developed in Java demonstrating core **Object-Oriented Programming (OOP) concepts**.

This repository contains **Module 1: Authentication & Role Management (Admin, Faculty, Student, Parent)**, built as an interactive desktop GUI application using **Java Swing**.

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
│       └── test/
│           └── TestRunner.java            # Automated 28-test verification suite
└── README.md
```

---

## 🧪 Verification & Automated Tests
The built-in test suite tests 5 core subsystems:
1. **Password Security**: Unique salt generation, SHA-256 hashing, constant-time verification.
2. **OOP Polymorphism**: Subclass badge resolution and metadata mapping.
3. **Authentication**: Success across all 4 roles, bad password rejection, unknown user rejection.
4. **Registration & Validation**: DTO field validation, duplicate username rejection.
5. **Session Management**: Singleton state preservation and cleanup.
