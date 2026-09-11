# Exam Management System (EMS)

A comprehensive, desktop-based Exam Management System built with **Java 19** and **Swing UI**, powered by a unified **SQLite** relational database with a clean layered architecture (**UI $\rightarrow$ Service $\rightarrow$ DAO $\rightarrow$ Database**).

---

## 🚀 Key Features

### 👨‍🎓 Student Capabilities
- **Dedicated Student Portal**: Directs students upon login to a central dashboard with personalized greeting, exam schedule, and result history.
- **Available Exams & Direct Launch**: View scheduled exams with time limit and question count, and launch the test session with real-time countdown.
- **Protected Exam-Taking Engine**:
  - Live timer with non-negative protection and single-trigger auto-submission upon expiry.
  - Real-time progress tracker (`Answered: X / Y`).
  - Pre-submission confirmation summary (Total questions, Answered, Unanswered, Time remaining).
  - Deterministic stable question shuffling per exam schedule.
- **Self-Service Security**: Students can update their password directly from their profile with current-password validation.
- **Exam History & Performance**: Full breakdown of previous attempts, percentages, and pass/fail statuses.

### 🛡️ Admin Capabilities
- **Real-Time KPI Dashboard**: Live metrics tracking Total/Active Students, Question Bank count, Overall Pass Rate, and Average Exam Score.
- **Student Management**:
  - Searchable student directory (Name, Email, Mobile, Aadhar).
  - Student registration with auto-generated credentials.
  - Profile editing with field validation.
  - Soft-delete (Active/Inactive) toggle preventing deactivated students from logging in.
  - Secure temporary password resets for students.
- **Exam Schedule & Lifecycle Management**:
  - Schedule exams with student selection, subject, duration, question count, and passing threshold.
  - Question limit validation against the subject's active question bank.
  - Lifecycle state machine: `SCHEDULED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` or `CANCELLED`.
  - Edit or cancel schedules prior to exam start.
- **Question Bank Management**:
  - Add questions with 4 options, validated correct answer, and difficulty levels (`EASY`, `MEDIUM`, `HARD`).
  - Search and filter questions by keyword and subject.
  - Soft-delete toggle to deactivate questions without corrupting historical result records.
- **Subject Management**:
  - Add, rename, and toggle active status for subjects.
  - Guard protection preventing subject deactivation if active scheduled exams are pending.
- **Result & Analytics Management**:
  - Filter results by Subject and Pass/Fail status with live search.
  - Detailed attempt modal with scoring metrics and submission timestamps.
  - Safe single-attempt result deletion.

---

## 🏛️ Architecture & Security

- **Layered Pattern**: Complete separation of concerns:
  - `exam_management_syatem.model`: Domain entity models (`Student`, `User`, `Subject`, `Question`, `ExamSchedule`, `ExamResult`).
  - `exam_management_syatem.dao`: Data Access Object layer executing parameterized SQLite JDBC queries.
  - `exam_management_syatem.service`: Business logic layer enforcing validation, transactions, and state transitions.
  - `exam_management_syatem.security`: Secure password hashing using **PBKDF2WithHmacSHA256** (65,536 iterations, cryptographic salt) and thread-safe `UserSession`.
  - Swing UI Components: Pure presentation consuming services.
- **Database**: Single unified `exam_management.db` SQLite database with foreign key constraints, cascading rules, and check constraints.

---

## 🛠️ Tech Stack & Prerequisites

- **Java Development Kit**: JDK 19+
- **Database**: SQLite 3 (via `sqlite-jdbc-3.45.1.0`)
- **GUI Toolkit**: Java Swing (AWT/Swing)
- **Security**: Java Cryptography Architecture (PBKDF2 HMAC-SHA256)

---

## 📦 How to Compile & Run

### 1. Compile
```bash
javac -cp "Resource/*:src" -d bin src/exam_management_syatem/*.java src/exam_management_syatem/*/*.java
```

### 2. Run the Application
```bash
java -cp "bin:Resource/*" exam_management_syatem.index
```

### 3. Run Automated Tests
```bash
java -cp "bin:Resource/*" exam_management_syatem.scratch.Phase3FullTestSuite
```

---

## 🔑 Default Credentials

- **Admin Login**:
  - **Username**: `superadmin`
  - **Password**: `123456`
- **Student Login**: Use registered student credentials or create a new student via Admin $\rightarrow$ Students $\rightarrow$ Add Student.
