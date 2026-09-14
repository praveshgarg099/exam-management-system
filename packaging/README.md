# Exam Management System - Packaging & Installer Architecture

This directory contains the production installer infrastructure for packaging the **Exam Management System (EMS)** into a standalone, single-file Windows desktop application installer (`ExamManagementSystem-Setup.exe`), as well as cross-platform distribution build tooling.

---

## 1. Zero-Touch Architecture Overview

The packaging architecture is engineered to provide a seamless, consumer-grade desktop application experience:

```
[ End User Downloads ExamManagementSystem-Setup.exe ]
                       │
                       ▼
            [ Runs Inno Setup Wizard ]
   • Installs to LocalAppData / Program Files
   • Creates Start Menu & Desktop Shortcuts
                       │
                       ▼
         [ Double-Clicks Desktop Shortcut ]
                       │
                       ▼
        [ ExamManagementSystem.vbs (Silent) ]
                       │
                       ▼
       [ ExamManagementSystem.bat Launcher ]
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
[ Port 5432 Active? ]       [ Start Local DB ]
  • Yes: use existing DB      • Init cluster in %LOCALAPPDATA%
                              • Start pg_ctl background
         │                           │
         └─────────────┬─────────────┘
                       ▼
       [ Bundled Modular JRE (~50MB) ]
        • target\runtime\bin\javaw.exe
                       │
                       ▼
      [ dist\exam-management-system.jar ]
   • Classpath image loading
   • Configuration hierarchy (%APPDATA% / conf / defaults)
   • Zero-touch DB auto-creation & 750 MCQ auto-seeding
                       │
                       ▼
    [ Professional Swing Login Window Opens ]
```

### Key Technical Achievements

1. **No External Java Requirement**: Bundles a lean, custom ~51MB Java Runtime Environment built via `jlink`. The user never needs to install Java, set `JAVA_HOME`, or configure environment variables.
2. **Zero-Touch PostgreSQL Provisioning**:
   - Checks if PostgreSQL is already active on port 5432 (e.g. system service or existing install).
   - If not active, initialises and starts a portable PostgreSQL cluster located safely in `%LOCALAPPDATA%\ExamManagementSystem\pgsql\data`.
   - On launch, `DatabaseManager` connects to the server, automatically runs `CREATE DATABASE exam_management` if absent, creates all 6 relational tables, provisions the default `superadmin`, and auto-seeds the 15 subjects and 750 MCQs.
3. **Silent Native Launcher**: Double-clicking the desktop shortcut runs `ExamManagementSystem.vbs`, which launches the batch script and `javaw.exe` silently without a black command-prompt window flickering on the screen.
4. **Data Preservation**: User databases and configuration are stored in user-writable `%LOCALAPPDATA%` and `%APPDATA%` paths, ensuring that application updates or uninstalls never accidentally erase user test results or student records.
5. **Preserved Core Architecture**: Zero rewrites. Retains pure Java 19+, Swing/AWT, PostgreSQL, JDBC, DAO, and Service layers with 100% test pass rate across all verification suites.

---

## 2. Directory Layout

```
packaging/
├── README.md                            # Complete packaging manual
├── build-distribution.sh                # Single-command distribution build & test (Unix/macOS)
├── common/
│   └── conf/
│       └── database.properties.template # Production configuration template
└── windows/
    ├── inno-setup/
    │   └── ExamManagementSystem.iss     # Inno Setup 6 compiler script
    ├── launcher/
    │   ├── ExamManagementSystem.bat     # Windows launcher (service check + JRE + app)
    │   └── ExamManagementSystem.vbs     # Silent VBS launcher (suppresses console window)
    └── scripts/
        ├── build-app-jar.bat            # Windows application JAR builder
        ├── build-app-jar.sh             # Unix/macOS application JAR builder
        ├── build-runtime.bat            # Windows jlink modular runtime builder
        ├── build-runtime.sh             # Unix/macOS jlink modular runtime builder
        ├── build-windows-installer.ps1  # One-click Windows PowerShell build pipeline
        ├── setup-local-postgres.bat     # Windows portable PostgreSQL initializer & starter
        └── stop-local-postgres.bat      # Windows portable PostgreSQL graceful shutdown
```

---

## 3. How to Build on Windows

### Prerequisites
1. **Java Development Kit (JDK 19+)**: Ensure `javac` and `jlink` are in your `PATH` or configured in `JAVA_HOME`.
2. **Inno Setup 6**: Download and install [Inno Setup 6](https://jrsoftware.org/isinfo.php). Default installation directory (`C:\Program Files (x86)\Inno Setup 6\ISCC.exe`) is automatically detected.
3. *(Optional)* **Portable PostgreSQL**: Place portable PostgreSQL binaries in `dist\pgsql\` if you wish to bundle the database directly into the single installer.

### One-Click Build Command
Open PowerShell as Administrator (or standard user) and run:

```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\packaging\windows\scripts\build-windows-installer.ps1
```

### Build Options
- **Skip PostgreSQL Bundling** (if deploying to client environments with centralized database server):
  ```powershell
  .\packaging\windows\scripts\build-windows-installer.ps1 -SkipPostgres
  ```
- **Skip JRE Rebuild** (if `target\runtime` is already built):
  ```powershell
  .\packaging\windows\scripts\build-windows-installer.ps1 -SkipRuntime
  ```

### Output
The compiled standalone installer will be located at:
```
dist\installer\ExamManagementSystem-Setup.exe
```

---

## 4. How to Build & Test on macOS / Linux

You can assemble the application JAR, build the modular JRE, and run all regression verification tests using a single command:

```bash
chmod +x packaging/build-distribution.sh
./packaging/build-distribution.sh
```

To run the packaged application JAR using the generated modular runtime:

```bash
./target/runtime/bin/java -jar dist/exam-management-system.jar
```

To run the full test suite against the packaged artifacts:

```bash
./target/runtime/bin/java -cp "dist/exam-management-system.jar:dist/lib/*" exam_management_syatem.test.FreshDatabaseVerificationTest
./target/runtime/bin/java -cp "dist/exam-management-system.jar:dist/lib/*" exam_management_syatem.test.ModernAdminViewsVerificationTest
./target/runtime/bin/java -cp "dist/exam-management-system.jar:dist/lib/*" exam_management_syatem.test.EndToEndWorkflowVerificationTest
./target/runtime/bin/java -cp "dist/exam-management-system.jar:dist/lib/*" exam_management_syatem.test.IndependentFinalVerificationTest
```

---

## 5. End-User Installation & Runtime Details

### Installation Flow
1. User downloads `ExamManagementSystem-Setup.exe`.
2. Runs the setup wizard. By default, it installs to `{autopf}\Exam Management System` (or `%LOCALAPPDATA%\Programs\Exam Management System` when non-elevated).
3. Option to create a Desktop shortcut is selected by default.
4. Click **Finish**.

### Application Launch Flow
1. User double-clicks the desktop shortcut **Exam Management System**.
2. `ExamManagementSystem.vbs` executes `ExamManagementSystem.bat` with window style `0` (completely hidden).
3. `setup-local-postgres.bat` checks if port 5432 is open. If not, it initializes the database cluster in `%LOCALAPPDATA%\ExamManagementSystem\pgsql\data` and starts the server.
4. `ExamManagementSystem.bat` locates the bundled modular JRE at `runtime\bin\javaw.exe`.
5. `javaw.exe` boots `dist\exam-management-system.jar`.
6. `AppConfig` resolves database connection parameters in this priority order:
   1. Environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`)
   2. `%APPDATA%\ExamManagementSystem\database.properties` (Windows) or `~/.exammanagementsystem/database.properties` (macOS/Linux)
   3. `conf\database.properties`
   4. Safe defaults (`localhost:5432/exam_management`, user: `postgres` on Windows or login name on Unix)
7. `DatabaseManager` connects to PostgreSQL. If `exam_management` database does not exist, it connects to administrative `postgres` database and executes `CREATE DATABASE exam_management`.
8. `DatabaseManager` verifies tables. If empty, it provisions the default admin (`superadmin` / `123456`) and seeds 15 subjects and 750 MCQs.
9. Swing Login Window appears.

### Customizing Configuration
To connect EMS to a remote database server:
Create or edit:
- **Windows**: `%APPDATA%\ExamManagementSystem\database.properties`
- **macOS / Linux**: `~/.exammanagementsystem/database.properties`

Example:
```properties
db.host=db.internal.school.edu
db.port=5432
db.name=exam_management
db.user=ems_admin
db.password=StrongPassword123!
```

---

## 6. Uninstallation Flow
1. When uninstalled via **Windows Settings > Apps & features** or the Start Menu shortcut:
2. The uninstaller invokes `scripts\stop-local-postgres.bat` to cleanly shut down any active portable PostgreSQL cluster.
3. Program files in `{app}` are removed.
4. User database in `%LOCALAPPDATA%\ExamManagementSystem\pgsql\data` is preserved to prevent accidental data loss.
