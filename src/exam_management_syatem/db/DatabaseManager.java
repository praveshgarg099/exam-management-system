package exam_management_syatem.db;

import exam_management_syatem.config.AppConfig;
import exam_management_syatem.security.PasswordHasher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DatabaseManager {

    private static class PooledEntry {
        final Connection conn;
        volatile long lastUsedTime;

        PooledEntry(Connection conn) {
            this.conn = conn;
            this.lastUsedTime = System.currentTimeMillis();
        }
    }

    private static final BlockingQueue<PooledEntry> pool = new LinkedBlockingQueue<>(16);
    private static final int MAX_POOL_SIZE = 10;
    private static int activeCount = 0;
    private static volatile boolean isInitialized = false;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        PooledEntry entry = pool.poll();
        while (entry != null) {
            Connection physicalConn = entry.conn;
            try {
                if (!physicalConn.isClosed()) {
                    long idleTime = System.currentTimeMillis() - entry.lastUsedTime;
                    // If connection was used within the last 30 seconds, it is guaranteed fresh.
                    // Avoid the 250ms isValid(1) network ping across the internet for active connections!
                    if (idleTime < 30_000 || physicalConn.isValid(2)) {
                        return wrapConnection(physicalConn);
                    }
                }
            } catch (Exception ignored) {}
            try { physicalConn.close(); } catch (Exception ignored) {}
            synchronized (DatabaseManager.class) {
                if (activeCount > 0) activeCount--;
            }
            entry = pool.poll();
        }

        synchronized (DatabaseManager.class) {
            Connection physicalConn = createPhysicalConnection();
            activeCount++;
            return wrapConnection(physicalConn);
        }
    }

    public static void warmUpPool(int targetCount) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            for (int i = 0; i < targetCount; i++) {
                try {
                    Connection conn = getConnection();
                    conn.close(); // Returns to pool as a warm connection
                } catch (Exception e) {
                    System.err.println("[DatabaseManager] Pool warm-up connection failed: " + e.getMessage());
                    break;
                }
            }
            System.out.println("[DatabaseManager] Connection pool warmed up with " + pool.size() + " ready connections.");
        });
    }

    private static Connection createPhysicalConnection() throws SQLException {
        try {
            return DriverManager.getConnection(AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword());
        } catch (SQLException e) {
            // If the target database does not exist, connect to postgres and create it
            if ("3D000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("does not exist"))) {
                ensureDatabaseExists();
                return DriverManager.getConnection(AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword());
            }
            throw e;
        }
    }

    private static Connection wrapConnection(Connection target) {
        return (Connection) Proxy.newProxyInstance(
                DatabaseManager.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("close".equals(method.getName())) {
                            if (!target.isClosed() && pool.size() < MAX_POOL_SIZE) {
                                try {
                                    if (!target.getAutoCommit()) {
                                        target.rollback();
                                        target.setAutoCommit(true);
                                    }
                                } catch (Exception ignored) {}
                                pool.offer(new PooledEntry(target));
                                return null;
                            }
                            synchronized (DatabaseManager.class) {
                                if (activeCount > 0) activeCount--;
                            }
                            target.close();
                            return null;
                        }
                        return method.invoke(target, args);
                    }
                }
        );
    }

    private static synchronized void ensureDatabaseExists() {
        if (!"localhost".equalsIgnoreCase(AppConfig.getDbHost()) && !"127.0.0.1".equals(AppConfig.getDbHost())) {
            return;
        }
        try (Connection adminConn = DriverManager.getConnection(AppConfig.getAdminDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword());
             Statement stmt = adminConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + AppConfig.getDbName());
            System.out.println("[DatabaseManager] Successfully created database: " + AppConfig.getDbName());
        } catch (Exception ex) {
            System.err.println("[DatabaseManager] Could not auto-create database: " + ex.getMessage());
        }
    }

    public static synchronized void initializeDatabase() throws SQLException {
        if (isInitialized) {
            return;
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Consolidated single-roundtrip DDL script (reduces 25 network round trips to 1)
            String ddlScript = 
                    "CREATE TABLE IF NOT EXISTS students ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "mobile VARCHAR(50) NOT NULL, "
                    + "email VARCHAR(255) NOT NULL, "
                    + "aadhar_no VARCHAR(50) NOT NULL UNIQUE, "
                    + "date_of_birth VARCHAR(50) NOT NULL, "
                    + "active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "username VARCHAR(100) NOT NULL UNIQUE, "
                    + "password_hash VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'STUDENT')), "
                    + "student_id INTEGER UNIQUE REFERENCES students(id) ON DELETE SET NULL, "
                    + "active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);\n"
                    + "CREATE TABLE IF NOT EXISTS subjects ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL UNIQUE, "
                    + "active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE TABLE IF NOT EXISTS questions ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT, "
                    + "question_text TEXT NOT NULL, "
                    + "option1 TEXT NOT NULL, "
                    + "option2 TEXT NOT NULL, "
                    + "option3 TEXT NOT NULL, "
                    + "option4 TEXT NOT NULL, "
                    + "correct_answer TEXT NOT NULL, "
                    + "difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', "
                    + "active BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_questions_subject ON questions(subject_id);\n"
                    + "CREATE TABLE IF NOT EXISTS exam_schedules ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE RESTRICT, "
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT, "
                    + "duration_minutes INTEGER NOT NULL, "
                    + "total_questions INTEGER NOT NULL, "
                    + "passing_percentage DOUBLE PRECISION NOT NULL, "
                    + "status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK(status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')), "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_schedules_student ON exam_schedules(student_id);\n"
                    + "CREATE TABLE IF NOT EXISTS exam_attempts ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "exam_schedule_id INTEGER REFERENCES exam_schedules(id) ON DELETE RESTRICT, "
                    + "student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE RESTRICT, "
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT, "
                    + "duration_minutes INTEGER NOT NULL, "
                    + "total_questions INTEGER NOT NULL, "
                    + "passing_percentage DOUBLE PRECISION NOT NULL DEFAULT 40.0, "
                    + "status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK(status IN ('IN_PROGRESS', 'COMPLETED', 'EXPIRED', 'CANCELLED')), "
                    + "started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "completed_at TIMESTAMPTZ, "
                    + "remaining_seconds INTEGER"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_attempts_student ON exam_attempts(student_id);\n"
                    + "CREATE INDEX IF NOT EXISTS idx_attempts_schedule ON exam_attempts(exam_schedule_id);\n"
                    + "DROP INDEX IF EXISTS idx_one_active_attempt_per_schedule;\n"
                    + "DROP INDEX IF EXISTS idx_one_active_or_completed_attempt_per_schedule;\n"
                    + "CREATE UNIQUE INDEX IF NOT EXISTS idx_one_attempt_per_schedule ON exam_attempts(student_id, exam_schedule_id);\n"
                    + "CREATE TABLE IF NOT EXISTS exam_attempt_questions ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "attempt_id INTEGER NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE, "
                    + "question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE RESTRICT, "
                    + "question_order INTEGER NOT NULL, "
                    + "selected_option TEXT, "
                    + "is_correct BOOLEAN DEFAULT NULL, "
                    + "UNIQUE(attempt_id, question_id), "
                    + "UNIQUE(attempt_id, question_order)"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_attempt_questions_attempt ON exam_attempt_questions(attempt_id);\n"
                    + "CREATE TABLE IF NOT EXISTS exam_results ("
                    + "id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "attempt_id INTEGER UNIQUE REFERENCES exam_attempts(id) ON DELETE RESTRICT, "
                    + "exam_schedule_id INTEGER REFERENCES exam_schedules(id) ON DELETE SET NULL, "
                    + "student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE RESTRICT, "
                    + "subject_id INTEGER NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT, "
                    + "total_questions INTEGER NOT NULL, "
                    + "correct_answers INTEGER NOT NULL, "
                    + "marks DOUBLE PRECISION NOT NULL, "
                    + "percentage DOUBLE PRECISION NOT NULL, "
                    + "result VARCHAR(20) NOT NULL CHECK(result IN ('Pass', 'Fail')), "
                    + "started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");\n"
                    + "CREATE INDEX IF NOT EXISTS idx_results_student ON exam_results(student_id);\n"
                    + "CREATE INDEX IF NOT EXISTS idx_results_attempt ON exam_results(attempt_id);\n";

            stmt.execute(ddlScript);

            // Initialize default admin user if none exists
            ensureDefaultAdmin(conn);

            // Initialize default curriculum & questions if database is brand new and empty
            ensureDefaultCurriculumAndQuestions(conn);

            isInitialized = true;
        }
    }

    private static void ensureDefaultAdmin(Connection conn) throws SQLException {
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdminSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String adminUser = AppConfig.getAdminUsername();
                String adminPass = AppConfig.getAdminPassword();
                if (adminUser != null && !adminUser.trim().isEmpty() && adminPass != null && !adminPass.trim().isEmpty()) {
                    String insertAdminSql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'ADMIN')";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
                        pstmt.setString(1, adminUser);
                        pstmt.setString(2, PasswordHasher.hashPassword(adminPass));
                        pstmt.executeUpdate();
                        System.out.println("[DatabaseManager] Seeded default administrator account successfully.");
                    }
                }
            }
        }
    }

    private static void ensureDefaultCurriculumAndQuestions(Connection conn) {
        try {
            int subjectCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
                if (rs.next()) subjectCount = rs.getInt(1);
            }

            if (subjectCount == 0) {
                System.out.println("[DatabaseManager] Empty curriculum detected. Populating 15 subjects and 750 MCQs...");
                String[] subjectNames = {
                        "Java Programming", "Data Structures", "Algorithms", "Database Management Systems", "Operating Systems",
                        "Computer Networks", "Software Engineering", "Object-Oriented Programming", "Computer Architecture", "Web Technologies",
                        "Artificial Intelligence", "Machine Learning", "Cloud Computing", "Cyber Security", "Distributed Systems"
                };

                java.util.List<java.util.List<QuestionBankPart1.QuestionItem>> questionSets = new java.util.ArrayList<>();
                questionSets.add(QuestionBankPart1.getJavaQuestions());
                questionSets.add(QuestionBankPart1.getDataStructuresQuestions());
                questionSets.add(QuestionBankPart1.getAlgorithmsQuestions());
                questionSets.add(QuestionBankPart1.getDbmsQuestions());
                questionSets.add(QuestionBankPart1.getOperatingSystemsQuestions());

                questionSets.add(QuestionBankPart2.getComputerNetworksQuestions());
                questionSets.add(QuestionBankPart2.getSoftwareEngineeringQuestions());
                questionSets.add(QuestionBankPart2.getOopQuestions());
                questionSets.add(QuestionBankPart2.getComputerArchitectureQuestions());
                questionSets.add(QuestionBankPart2.getWebTechnologiesQuestions());

                questionSets.add(QuestionBankPart3.getAiQuestions());
                questionSets.add(QuestionBankPart3.getMlQuestions());
                questionSets.add(QuestionBankPart3.getCloudComputingQuestions());
                questionSets.add(QuestionBankPart3.getCyberSecurityQuestions());
                questionSets.add(QuestionBankPart3.getDistributedSystemsQuestions());

                for (int i = 0; i < subjectNames.length; i++) {
                    int subId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO subjects (name, active) VALUES (?, true) RETURNING id")) {
                        ps.setString(1, subjectNames[i]);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) subId = rs.getInt(1);
                        }
                    }

                    if (subId > 0 && i < questionSets.size()) {
                        java.util.List<QuestionBankPart1.QuestionItem> qList = questionSets.get(i);
                        try (PreparedStatement qps = conn.prepareStatement(
                                "INSERT INTO questions (subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)")) {
                            for (QuestionBankPart1.QuestionItem q : qList) {
                                qps.setInt(1, subId);
                                qps.setString(2, q.question);
                                qps.setString(3, q.optA);
                                qps.setString(4, q.optB);
                                qps.setString(5, q.optC);
                                qps.setString(6, q.optD);
                                qps.setString(7, q.correct);
                                qps.setString(8, q.difficulty);
                                qps.addBatch();
                            }
                            qps.executeBatch();
                        }
                    }
                }
                System.out.println("[DatabaseManager] Completed initial seeding of 15 subjects and 750 MCQs.");
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Note: Curriculum auto-seed skipped or completed: " + e.getMessage());
        }
    }
}
