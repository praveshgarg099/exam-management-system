# Exam Management System (EMS)

A enterprise-grade, desktop-based Exam Management System built with **Java 19** and **Swing UI**, backed by a robust **PostgreSQL** relational database with a clean layered architecture (**UI $\rightarrow$ Service $\rightarrow$ DAO $\rightarrow$ PostgreSQL**).

---

## 🏛️ Project Architecture & Package Structure

The codebase is organized into clean, dedicated packages with strict boundary separation:

```text
exam_management_syatem
├── app
│   └── Main.java                         # Application entrypoint (bootstrap, DB init, theme, LoginFrame)
├── config
│   └── DatabaseConfig.java               # PostgreSQL connection management & credentials
├── security
│   ├── PasswordHasher.java               # PBKDF2WithHmacSHA256 (65,536 iterations, cryptographic salt)
│   └── UserSession.java                  # Thread-safe authenticated user context & role authorization
├── model                                 # Domain models (Student, User, Subject, Question, ExamSchedule, etc.)
├── dao                                   # Data Access Objects (parameterized PostgreSQL queries)
├── service                               # Business logic & transaction enforcement
├── util
│   ├── ImageLoader.java                  # Centralized image loading with cached fallback icons
│   └── ValidationUtils.java              # Field validation, regex checks, and safe conversions
├── ui
│   ├── design                            # Design system tokens, Theme colors, Typography, Icons
│   ├── components                        # Reusable Swing widgets (Buttons, StatCards, StatusBadges, Inputs)
│   ├── shell                             # Application frame (CardLayout, Sidebar, Topbar, ViewRegistry)
│   ├── auth                              # Premium Login experience (LoginFrame)
│   └── views
│       ├── admin                         # Admin modern views (AdminDashboardView)
│       ├── student                       # Student modern views (Dashboard, My Exams, Results, Profile)
│       └── exam                          # Active examination engine views (testtake, last)
├── legacy
│   └── ui                                # Isolated legacy Swing screens (adminpage1, testmaker, etc.)
├── tools
│   └── DesignSystemPreview.java          # Interactive UI component & token gallery
└── test                                  # Automated test suites for all subsystems & phases
```

---

## 🚀 Key Features

### 👨‍🎓 Student Capabilities
- **Dedicated Student Portal**: Modern dashboard with greeting, metric cards, exam schedule preview, and recent results.
- **My Exams & Direct Launch**: Real-time examination view with authoritative `ExamService.getExistingAttempt()` status checks.
- **Protected Examination Engine**:
  - Live timer with non-negative countdown and authoritative single-trigger submission.
  - Real-time question navigation, progress tracker, and status summary.
  - Deterministic stable question shuffling per exam schedule.
  - Terminal attempt protections (`COMPLETED` and `EXPIRED` attempts cannot be restarted or manipulated).
- **Self-Service Security**: Students can update their password directly from their profile with current-password validation.
- **Results & Performance**: Full breakdown of past attempts, percentages, and pass/fail indicators.

### 🛡️ Admin Capabilities
- **Real-Time Telemetry Dashboard**: Live metrics tracking Total/Active Students, Question Bank count, Schedules, Overall Pass Rate, and Average Exam Score.
- **Student Management**: Directory search, registration with secure credential generation, soft-deletes, and profile editing.
- **Exam Schedule Lifecycle**: Lifecycle state machine (`SCHEDULED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` / `CANCELLED`).
- **Question Bank Management**: Multi-choice question creation with difficulty tagging, subject associations, and soft-delete protection.
- **Subject Management**: Subject directory with guard protections preventing deactivation when pending schedules exist.
- **Result & Analytics Management**: Filtered examination performance reports with detailed attempt modals.

---

## 🔒 Security & Integrity Baseline

- **Password Hashing**: Strictly **PBKDF2WithHmacSHA256** with unique salts and constant-time comparisons. No BCrypt code or dependencies exist.
- **Role Enforcement**: Every service method requires a valid `UserSession` and asserts role permissions (`requireAdmin()`, student identity match).
- **Database Concurrency & Idempotency**: Strict PostgreSQL schema with foreign keys, checks, unique constraints, and transaction rollbacks.
- **Attempt Invariant**: Exactly one attempt record per student per exam schedule across all attempt states.

---

## 🛠️ Tech Stack & Prerequisites

- **Java Development Kit**: JDK 19+
- **Database**: PostgreSQL 14+ / 18+ (running on `localhost:5432`, database `exam_management`)
- **JDBC Driver**: `postgresql-42.7.2.jar` (in `Resource/`)
- **GUI Toolkit**: Java Swing / AWT

---

## 📦 How to Compile & Run

### 1. Compile All Sources
Using the comprehensive sources manifest:
```bash
javac -cp "Resource/*:src" -d bin @sources.txt
```

### 2. Launch the Application
Modern entrypoint:
```bash
java -cp "bin:Resource/*" exam_management_syatem.app.Main
```
*(Legacy entrypoint `exam_management_syatem.index` is preserved as a backward-compatible delegating facade).*

### 3. Launch Design System Preview
```bash
java -cp "bin:Resource/*" exam_management_syatem.tools.DesignSystemPreview
```

### 4. Execute Automated Test Suites
Run any of the 6 comprehensive test suites:
```bash
# 1. PostgreSQL Migration & Integration (52 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.PostgreSQLMigrationVerification

# 2. Phase 4A Security & Password Hardening (36 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.Phase4ASecurityTestSuite

# 3. Phase 3 Product & Lifecycle Suite (43 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.Phase3FullTestSuite

# 4. UI.3 Authentication Suite (15 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.UI3VerificationTest

# 5. UI.4 Admin Dashboard Suite (22 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.UI4VerificationTest

# 6. UI.5 Student Dashboard Suite (30 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.UI5VerificationTest
```

---

## 🔑 Default Credentials

- **Admin Account**:
  - **Username**: `superadmin`
  - **Password**: `123456`
- **Student Account**: Register a new student via Admin $\rightarrow$ Student Directory, or use existing student credentials.
