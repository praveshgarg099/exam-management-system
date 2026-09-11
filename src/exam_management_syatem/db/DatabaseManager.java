package exam_management_syatem.db;

import exam_management_syatem.config.AppConfig;
import exam_management_syatem.security.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(AppConfig.getDbUrl());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static synchronized void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. students
            stmt.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "mobile TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "aadhar_no TEXT NOT NULL UNIQUE, "
                    + "date_of_birth TEXT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");

            // 2. users
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password_hash TEXT NOT NULL, "
                    + "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'STUDENT')), "
                    + "student_id INTEGER UNIQUE, "
                    + "active INTEGER DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE SET NULL"
                    + ");");

            // 3. subjects
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL UNIQUE, "
                    + "active INTEGER DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");

            // 4. questions
            stmt.execute("CREATE TABLE IF NOT EXISTS questions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "subject_id INTEGER NOT NULL, "
                    + "question_text TEXT NOT NULL, "
                    + "option1 TEXT NOT NULL, "
                    + "option2 TEXT NOT NULL, "
                    + "option3 TEXT NOT NULL, "
                    + "option4 TEXT NOT NULL, "
                    + "correct_answer TEXT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE"
                    + ");");

            // 5. exam_schedules
            stmt.execute("CREATE TABLE IF NOT EXISTS exam_schedules ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "student_id INTEGER NOT NULL, "
                    + "subject_id INTEGER NOT NULL, "
                    + "duration_minutes INTEGER NOT NULL, "
                    + "total_questions INTEGER NOT NULL, "
                    + "passing_percentage REAL NOT NULL, "
                    + "status TEXT DEFAULT 'SCHEDULED' CHECK(status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')), "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE"
                    + ");");

            // 6. exam_results
            stmt.execute("CREATE TABLE IF NOT EXISTS exam_results ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "exam_schedule_id INTEGER, "
                    + "student_id INTEGER NOT NULL, "
                    + "subject_id INTEGER NOT NULL, "
                    + "total_questions INTEGER NOT NULL, "
                    + "correct_answers INTEGER NOT NULL, "
                    + "marks REAL NOT NULL, "
                    + "percentage REAL NOT NULL, "
                    + "result TEXT NOT NULL CHECK(result IN ('Pass', 'Fail')), "
                    + "started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(exam_schedule_id) REFERENCES exam_schedules(id) ON DELETE SET NULL, "
                    + "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE"
                    + ");");

            // Indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_questions_subject ON questions(subject_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_schedules_student ON exam_schedules(student_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_results_student ON exam_results(student_id);");

            // Migration checks for new Phase 3 columns
            try {
                stmt.execute("ALTER TABLE questions ADD COLUMN difficulty TEXT DEFAULT 'MEDIUM';");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE questions ADD COLUMN active INTEGER DEFAULT 1;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE students ADD COLUMN active INTEGER DEFAULT 1;");
            } catch (SQLException ignored) {}

            // Ensure exam_schedules status check constraint supports IN_PROGRESS
            try {
                String tableSql = "";
                try (ResultSet rs = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='exam_schedules'")) {
                    if (rs.next()) tableSql = rs.getString(1);
                }
                if (tableSql != null && !tableSql.isEmpty() && !tableSql.contains("IN_PROGRESS")) {
                    stmt.execute("PRAGMA foreign_keys = OFF;");
                    stmt.execute("CREATE TABLE exam_schedules_temp ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "student_id INTEGER NOT NULL, "
                            + "subject_id INTEGER NOT NULL, "
                            + "duration_minutes INTEGER NOT NULL, "
                            + "total_questions INTEGER NOT NULL, "
                            + "passing_percentage REAL NOT NULL, "
                            + "status TEXT DEFAULT 'SCHEDULED' CHECK(status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')), "
                            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                            + "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE, "
                            + "FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE"
                            + ");");
                    stmt.execute("INSERT INTO exam_schedules_temp (id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at) "
                            + "SELECT id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at FROM exam_schedules;");
                    stmt.execute("DROP TABLE exam_schedules;");
                    stmt.execute("ALTER TABLE exam_schedules_temp RENAME TO exam_schedules;");
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            } catch (SQLException ignored) {}

            // Initialize default admin user if none exists
            ensureDefaultAdmin(conn);
        }
    }

    private static void ensureDefaultAdmin(Connection conn) throws SQLException {
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdminSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdminSql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'ADMIN')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
                    pstmt.setString(1, "superadmin");
                    pstmt.setString(2, PasswordHasher.hashPassword("123456"));
                    pstmt.executeUpdate();
                }
            }
        }
    }
}
