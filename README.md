# Exam Management System

An enterprise-grade, desktop-based Exam Management System (EMS) developed in **Java 19** and **Swing / AWT**, backed by a robust **PostgreSQL** relational database. The application adheres to a clean, multi-layered architectural pattern:

$$\text{Swing / AWT UI} \longrightarrow \text{Service Layer} \longrightarrow \text{DAO Layer} \longrightarrow \text{JDBC} \longrightarrow \text{PostgreSQL}$$

The system operates strictly as a standalone Java desktop application with native JDBC connectivity and parameterized queries. It contains **no Spring Boot, no Spring Framework, no Hibernate/JPA, no REST controllers, and no Microsoft Access/UCanAccess dependencies**.

---

## 1. Overview

The **Exam Management System (EMS)** is an end-to-end academic examination platform designed to manage the entire lifecycle of computer-based testing — from student onboarding and curriculum management to live timed test delivery, automated grading, and longitudinal analytics.

### Target Users
- **System Administrators / Educators**: Manage students, academic subjects, question repositories, exam schedules, and grading audits.
- **Students / Examinees**: Access personalized dashboards, view exam rosters, take timed tests with in-flight answer persistence, and inspect immediate performance reports.

### Problems Solved
- **Data Integrity & Concurrency**: Replaces legacy, file-based Access setups with an ACID-compliant PostgreSQL relational engine featuring identity sequences, unique constraints, and transaction savepoints.
- **Test Integrity & Timer Enforceability**: Guarantees server-side timer calculations, deterministic question allocation, single-trigger submissions, and state machines that prevent manipulation or duplicate submissions.
- **Security & Authorization**: Implements PBKDF2WithHmacSHA256 password cryptography, Role-Based Access Control (RBAC), and Anti-Insecure Direct Object Reference (Anti-IDOR) validation across all services.

---

## 2. Key Features

### 🛡️ Admin Capabilities

| Feature Area | Implemented Functionality |
| :--- | :--- |
| **Telemetry Dashboard** | Real-time analytics tracking Total Students, Active Students, Question Bank size, Total Exam Schedules, Overall Pass Rate, and Average Exam Score with instant refresh. |
| **Student Management** | Full CRUD operations; real-time keyword search (name, email, mobile, Aadhar); concurrency-safe registration with auto-generated credentials; active/inactive status toggle; admin password resets. |
| **Subject Management** | Academic subject directory; subject creation with unique constraint validation; subject renaming; soft-deactivation guards protecting subjects referenced in pending exams. |
| **Question Bank Management** | Comprehensive MCQ management across all subjects; 4 distinct options with strict correct-answer matching; difficulty tagging (`EASY`, `MEDIUM`, `HARD`); subject-filtering and keyword search; soft-delete protection for historical attempt auditing. |
| **Exam Scheduling** | Student-to-subject scheduling with custom duration, question counts, and passing thresholds; **Question Availability Guards** that strictly block over-scheduling; schedule editing and cancellation. |
| **Results & Audit Management** | Global examination results directory with student keyword search, subject filtering, and Pass/Fail status filtering; **Detailed Attempt Modal** inspecting every question, option choice, student answer, and grading status. |

### 👨‍🎓 Student Capabilities

| Feature Area | Implemented Functionality |
| :--- | :--- |
| **Authentication & Profile** | Role-segregated login screen with password visibility toggles; student profile view with self-service password update requiring current password confirmation. |
| **Personal Dashboard** | Customized student portal displaying overall performance metrics, recent examination history, and a list of upcoming/scheduled exams. |
| **My Exams** | Real-time examination roster categorizing exams into `SCHEDULED`, `IN_PROGRESS`, and `COMPLETED`; direct exam initiation and seamless attempt resumption. |
| **Examination Engine** | Non-negative countdown timer with server-authoritative ceiling; question navigation panel; immediate answer saving to the database; single-trigger submission with idempotency protection. |
| **My Results** | Personal results archive displaying marks obtained, total marks, percentage, Pass/Fail status, and timestamps. |

---

## 3. Technology Stack

| Component | Technology | Version / Specification | Notes |
| :--- | :--- | :--- | :--- |
| **Runtime Environment** | Java Development Kit (JDK) | **Java 19** | Modern Java language features |
| **User Interface** | Java Swing / AWT | Desktop GUI | Custom design system tokens, responsive CardLayout |
| **Database Engine** | PostgreSQL | **14.x / 16.x / 18.x** | Authoritative relational storage with strict ACID compliance |
| **Database Driver** | PostgreSQL JDBC Driver | **42.7.3** | Parameterized queries, identity sequence handling |
| **Cryptography** | PBKDF2WithHmacSHA256 | 65,536 iterations | Cryptographically random salt, constant-time verification |
| **Build System** | Pure Java Tools (`javac`) | Command-line | Manifest-based compilation (`@sources.txt`) |
| **Testing** | Standalone Automated Suites | Pure Java / Assertion API | 300+ assertions verifying services, security, and UI flows |
| **Spring Framework** | **None** | **N/A** | Explicitly excluded; pure Java desktop application |
| **ORM / Hibernate** | **None** | **N/A** | Explicitly excluded; pure JDBC DAO layer |
| **Microsoft Access** | **None** | **N/A** | All `.accdb` files and UCanAccess drivers eliminated |

---

## 4. Architecture & Layered Design

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Swing / AWT UI Layer                            │
│  • Shell: MainApplicationFrame, AppSidebar, AppTopBar, ViewRegistry    │
│  • Auth: LoginFrame, LoginPanel                                        │
│  • Admin Views: AdminDashboard, Students, Subjects, Questions, Exams   │
│  • Student Views: StudentDashboard, MyExams, MyResults, StudentProfile │
│  • Examination Engine: testtake, last                                  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Method Invocations with UserSession
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           Service Layer                                │
│  • AuthenticationService   • StudentService       • SubjectService     │
│  • QuestionService         • ExamService          • ResultService      │
│  • DashboardMetricsService                                             │
│  • Responsibilities: Role-Based Access Control (RBAC), Anti-IDOR,      │
│    Business Invariant Validation, Savepoint Retries, Atomic Grading    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Domain Models & Transaction Conn
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             DAO Layer                                  │
│  • UserDAO                 • StudentDAO           • SubjectDAO         │
│  • QuestionDAO             • ExamScheduleDAO      • ExamAttemptDAO     │
│  • ExamAttemptQuestionDAO  • ExamResultDAO                             │
│  • Responsibilities: Pure SQL queries, PreparedStatement mapping,      │
│    ResultSet serialization, Batch inserts                              │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Pure JDBC Connection / Pool
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       Database Access Layer                            │
│  • DatabaseManager: Connection acquisition, DDL Schema Bootstrap       │
│  • AppConfig: Centralized environment & database configuration         │
│  • PostgreSQL JDBC Driver (org.postgresql.Driver)                      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ TCP/IP (localhost:5432)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Relational Database                      │
│  • Database: exam_management                                           │
│  • 8 Tables: students, users, subjects, questions,                     │
│              exam_schedules, exam_attempts,                            │
│              exam_attempt_questions, exam_results                      │
└────────────────────────────────────────────────────────────────────────┘
```

### Architectural Principles
1. **Clean Separation of Concerns**: UI components never contain database queries or business grading logic; they delegate entirely to the Service layer.
2. **Stateless Service Layer**: Services receive an authenticated `UserSession` on every call and enforce authorization before delegating to DAOs.
3. **Transaction Safety**: Multi-table operations (such as student registration with credential provisioning, exam start with question snapshotting, and submission with grading) execute in atomic transactions with explicit commit/rollback handling.
4. **Resilient Concurrency**: The student username generation routine utilizes transactional savepoints (`conn.setSavepoint()`) to catch unique constraint collisions (`23505`) and safely retry with numeric suffixes without invalidating parent transactions.

---

## 5. Database Schema & Tables

The application manages **8 relational tables** in PostgreSQL:

| Table Name | Primary Key | Key Foreign Keys & Constraints | Purpose |
| :--- | :--- | :--- | :--- |
| **`students`** | `id` (IDENTITY) | `aadhar_no` (UNIQUE), `mobile`, `email`, `active` (BOOLEAN) | Stores student personal details, contact info, and active status. |
| **`users`** | `id` (IDENTITY) | `username` (UNIQUE), `student_id` (FK $\rightarrow$ `students`, UNIQUE), `role` IN (`ADMIN`, `STUDENT`) | System login accounts, password hashes, and role definitions. |
| **`subjects`** | `id` (IDENTITY) | `name` (UNIQUE), `active` (BOOLEAN) | Academic subjects / exam curricula. |
| **`questions`** | `id` (IDENTITY) | `subject_id` (FK $\rightarrow$ `subjects`), `difficulty` IN (`EASY`, `MEDIUM`, `HARD`), `active` (BOOLEAN) | Question bank containing text, 4 options, and correct answer. |
| **`exam_schedules`** | `id` (IDENTITY) | `student_id` (FK $\rightarrow$ `students`), `subject_id` (FK $\rightarrow$ `subjects`), `status` IN (`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`) | Scheduled examinations for specific students with duration and threshold rules. |
| **`exam_attempts`** | `id` (IDENTITY) | `exam_schedule_id` (FK $\rightarrow$ `exam_schedules`), `student_id`, `status` IN (`IN_PROGRESS`, `COMPLETED`, `EXPIRED`) | Real-time attempt instance tracking timers, timestamps, and active state. |
| **`exam_attempt_questions`**| `id` (IDENTITY) | `attempt_id` (FK $\rightarrow$ `exam_attempts`), `question_id` (FK $\rightarrow$ `questions`) | Snapshot of allocated questions and chosen answers for an attempt. |
| **`exam_results`** | `id` (IDENTITY) | `attempt_id` (FK $\rightarrow$ `exam_attempts`, UNIQUE), `student_id`, `subject_id`, `result` IN (`Pass`, `Fail`) | Final graded results, total marks, percentage, and submission timestamp. |

---

## 6. Project Structure

```text
exam_management_syatem
├── Resource/                                 # Runtime JAR libraries (PostgreSQL driver, Look & Feel, etc.)
│   ├── postgresql-42.7.3.jar
│   ├── JTattoo-1.6.11.jar
│   └── ...
├── src/exam_management_syatem/
│   ├── app/
│   │   └── Main.java                         # Main application bootstrap entrypoint
│   ├── config/
│   │   └── AppConfig.java                    # Environment variable parsing & PostgreSQL JDBC URL
│   ├── security/
│   │   ├── PasswordHasher.java               # PBKDF2 cryptography with salt generation & verification
│   │   └── UserSession.java                  # Authenticated session context with RBAC assertions
│   ├── model/                                # Domain entities (Student, User, Subject, Question, etc.)
│   ├── dao/                                  # Data Access Objects (Pure JDBC PreparedStatement implementations)
│   ├── service/                              # Business service layer (ExamService, StudentService, etc.)
│   ├── db/
│   │   ├── DatabaseManager.java              # JDBC connection factory & DDL schema initializer
│   │   ├── FreshDatabaseSeeder.java          # Transactional database reset & fresh dataset populator
│   │   ├── QuestionBankPart1.java            # Seed data: 250 MCQs (Java, DS, Algos, DBMS, OS)
│   │   ├── QuestionBankPart2.java            # Seed data: 250 MCQs (Networks, SE, OOP, Arch, Web)
│   │   └── QuestionBankPart3.java            # Seed data: 250 MCQs (AI, ML, Cloud, Security, Dist Sys)
│   ├── ui/
│   │   ├── auth/                             # LoginFrame, LoginPanel
│   │   ├── design/                           # Design tokens: Colors, Typography, Spacing, Dimensions, Icons
│   │   ├── components/                       # AppButton, AppCard, AppTable, AppTextField, SearchField, etc.
│   │   ├── shell/                            # MainApplicationFrame, AppSidebar, AppTopBar, ViewRegistry
│   │   └── views/
│   │       ├── admin/                        # AdminDashboardView, AdminStudentsView, AdminSubjectsView,
│   │       │                                 # AdminQuestionsView, AdminExamsView, AdminResultsView
│   │       ├── student/                      # StudentDashboardView, StudentMyExamsView,
│   │       │                                 # StudentMyResultsView, StudentProfileView
│   │       └── exam/                         # testtake (Active exam engine), last (Completion screen)
│   ├── legacy/ui/                            # Preserved legacy screens (historical reference)
│   ├── tools/
│   │   └── DesignSystemPreview.java          # Visual gallery of all custom Swing components
│   └── test/                                 # Automated integration and workflow verification test suites
├── packaging/                                # Desktop application packaging & Windows installer pipeline
│   ├── README.md                             # Packaging manual & architecture documentation
│   ├── build-distribution.sh                 # Cross-platform JAR & modular JRE runtime builder
│   ├── common/conf/                          # Production database configuration template
│   └── windows/                              # Inno Setup 6 script, launchers (.bat/.vbs), and PowerShell pipeline
├── sources.txt                               # Compilation manifest listing all active source files
└── README.md                                 # Complete project documentation
```

---

## 7. Configuration & Environment Variables

The application resolves its database configuration dynamically using a multi-tiered hierarchy:
1. **Environment Variables**: Highest precedence (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).
2. **User Profile Directory**:
   - Windows: `%APPDATA%\ExamManagementSystem\database.properties`
   - macOS / Linux: `~/.exammanagementsystem/database.properties`
3. **Application Directory**: `conf/database.properties` or `database.properties` in current working directory.
4. **Safe Defaults**: `localhost:5432/exam_management` (database user defaults to `postgres` on Windows or local OS user on Unix/macOS).

| Variable / Key | Default Value | Description |
| :--- | :--- | :--- |
| `DB_HOST` / `db.host` | `localhost` | PostgreSQL server hostname |
| `DB_PORT` / `db.port` | `5432` | PostgreSQL server port |
| `DB_NAME` / `db.name` | `exam_management` | PostgreSQL database name |
| `DB_USER` / `db.user` | `postgres` (Win) / OS user | PostgreSQL database username |
| `DB_PASSWORD` / `db.password` | `""` (empty string) | PostgreSQL database password |

---

## 8. Build, Execution & Testing Guide

### Prerequisites
1. **JDK 19+** installed and available on `PATH` (`java -version`).
2. **PostgreSQL Server** installed and running on `localhost:5432`.
3. Database created:
   ```bash
   createdb exam_management
   ```
   *(Note: The application also includes zero-touch DB creation if connected to administrative `postgres` database).*

### 1. Compile the Project
Compile the complete source tree using the provided manifest:
```bash
rm -rf bin && mkdir bin
javac -cp "Resource/*:src" -d bin @sources.txt
```

### 2. Run the Application
Launch the graphical interface:
```bash
java -cp "bin:Resource/*" exam_management_syatem.app.Main
```

### 3. Launch Design System Preview (Optional)
Inspect all custom buttons, inputs, cards, tables, and typography tokens:
```bash
java -cp "bin:Resource/*" exam_management_syatem.tools.DesignSystemPreview
```

### 4. Run Automated Test Suites
The codebase includes comprehensive integration and workflow verification tests:

```bash
# 1. Fresh Database & PostgreSQL Integrity Audit (30 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.FreshDatabaseVerificationTest

# 2. Modern Admin Views Live CRUD & Guard Verification (51 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.ModernAdminViewsVerificationTest

# 3. Complete End-to-End Admin & Student Workflow Verification (24 tests)
java -cp "bin:Resource/*" exam_management_syatem.test.EndToEndWorkflowVerificationTest
```

### 5. Desktop Application Packaging & Windows Installer
To package the Exam Management System into a standalone desktop application:

- **Windows (Single-Click Installer)**:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
  .\packaging\windows\scripts\build-windows-installer.ps1
  ```
  Generates `dist\installer\ExamManagementSystem-Setup.exe` with bundled modular JRE runtime (~51MB) and Inno Setup 6 installer.

- **macOS / Linux (Distribution Assembly & Tests)**:
  ```bash
  chmod +x packaging/build-distribution.sh
  ./packaging/build-distribution.sh
  ```
  Assembles `dist/exam-management-system.jar` and custom modular JRE runtime via `jlink`.

See [packaging/README.md](packaging/README.md) for full architectural documentation.

### 6. All-In-One Docker Container (Zero-Install Web Browser Access)
To run the entire application (PostgreSQL + Java + Web GUI) in a single container without installing Java, PostgreSQL, or desktop software:

```bash
docker compose up -d --build
```
Open in any browser:
```text
http://localhost:8080
```
See [DOCKER.md](DOCKER.md) for cloud deployment guides (AWS, DigitalOcean, Render, Caddy/Nginx).

---

## 9. Default Credentials & Sample Accounts

### Administrator Account
The system administrator account is pre-configured and preserved in PostgreSQL:
- **Username**: `superadmin`
- **Password**: `123456`
- **Role**: `ADMIN`

### Pre-Seeded Student Accounts
All 20 sample students have the default password **`Password@123`**:

| Student ID | Full Name | Username | Password | Email | Status |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **1** | Aarav Sharma | `aaravs` | `Password@123` | `aarav.sharma@example.com` | **Active** |
| **2** | Ananya Verma | `ananyav` | `Password@123` | `ananya.verma@example.com` | **Active** |
| **3** | Rohan Patel | `rohanp` | `Password@123` | `rohan.patel@example.com` | **Active** |
| **4** | Priya Nair | `priyan` | `Password@123` | `priya.nair@example.com` | **Active** |
| **5** | Vikram Singh | `vikrams` | `Password@123` | `vikram.singh@example.com` | **Active** |
| **6** | Sneha Rao | `snehar` | `Password@123` | `sneha.rao@example.com` | **Active** |
| **7** | Aditya Gupta | `adityag` | `Password@123` | `aditya.gupta@example.com` | **Active** |
| **8** | Pooja Joshi | `poojaj` | `Password@123` | `pooja.joshi@example.com` | **Active** |
| **9** | Rahul Mehta | `rahulm` | `Password@123` | `rahul.mehta@example.com` | **Active** |
| **10** | Neha Kulkarni | `nehak` | `Password@123` | `neha.kulkarni@example.com` | **Active** |
| **11** | Siddharth Malhotra | `siddharthm` | `Password@123` | `siddharth.m@example.com` | **Active** |
| **12** | Tanvi Deshmukh | `tanvid` | `Password@123` | `tanvi.d@example.com` | **Active** |
| **13** | Arjun Reddy | `arjunr` | `Password@123` | `arjun.reddy@example.com` | **Active** |
| **14** | Kavya Iyer | `kavyai` | `Password@123` | `kavya.iyer@example.com` | **Active** |
| **15** | Manish Tiwari | `manisht` | `Password@123` | `manish.tiwari@example.com` | **Active** |
| **16** | Ritu Agarwal | `ritua` | `Password@123` | `ritu.agarwal@example.com` | **Active** |
| **17** | Harsh Vardhan | `harshv` | `Password@123` | `harsh.vardhan@example.com` | **Active** |
| **18** | Divya Pillai | `divyap` | `Password@123` | `divya.pillai@example.com` | **Active** |
| **19** | Kunal Saxena | `kunals` | `Password@123` | `kunal.saxena@example.com` | **Inactive** |
| **20** | Shreya Bhattacharya | `shreyab` | `Password@123` | `shreya.b@example.com` | **Inactive** |
