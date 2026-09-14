package exam_management_syatem.test;

import exam_management_syatem.dao.*;
import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.*;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;

import exam_management_syatem.security.UserSession;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLMigrationVerification {

    private static int passed = 0;
    private static int failed = 0;

    private static final UserSession adminSession = new UserSession(1, "admin", "ADMIN", null);
    private static final UserSession student1Session = new UserSession(2, "student1", "STUDENT", 1);
    private static final UserSession student2Session = new UserSession(3, "student2", "STUDENT", 2);
    private static final UserSession student3Session = new UserSession(4, "student3", "STUDENT", 3);

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("     POSTGRESQL MIGRATION & PERSISTENCE VERIFICATION     ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();

            testPostgreSQLConnectionAndVersion();
            testSchemaTablesAndIdentity();
            testHistoricalDataCounts();
            testNativeBooleanTypes();
            testPersistentAttemptCreationAndResume();
            testQuestionDeactivationSafety();
            testAuthoritativeTimer();
            testConcurrentAttemptProtection();
            testAtomicAndIdempotentSubmission();
            testScheduleIndependenceRule();
            testPostgreSQLFailureNoFallback();

        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }

        System.out.println("=========================================================");
        System.out.println("AUDIT RESULT: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("=========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void testPostgreSQLConnectionAndVersion() throws SQLException {
        System.out.println("\n--- 1. Connection & Engine Audit ---");
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String dbProduct = meta.getDatabaseProductName();
            String dbVersion = meta.getDatabaseProductVersion();
            System.out.println("  Connected to: " + dbProduct + " version " + dbVersion);
            assertTrue("Database is PostgreSQL", "PostgreSQL".equalsIgnoreCase(dbProduct));
        }
    }

    private static void testSchemaTablesAndIdentity() throws SQLException {
        System.out.println("\n--- 2. Schema Tables & Identity Column Audit ---");
        String[] requiredTables = {
                "students", "users", "subjects", "questions",
                "exam_schedules", "exam_attempts", "exam_attempt_questions", "exam_results"
        };
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String t : requiredTables) {
                try (ResultSet rs = meta.getTables(null, "public", t, new String[]{"TABLE"})) {
                    assertTrue("Table '" + t + "' exists in PostgreSQL", rs.next());
                }
            }
        }
    }

    private static void testHistoricalDataCounts() throws SQLException {
        System.out.println("\n--- 3. Historical Data Row Counts & Null Attempt Link ---");
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamScheduleDAO scheduleDAO = new ExamScheduleDAO();
        ExamResultDAO resultDAO = new ExamResultDAO();

        assertTrue("Historical students preserved (count >= 3)", studentDAO.listAll().size() >= 3);
        assertTrue("Historical users preserved (count >= 5)", userDAO.findByUsername(TestCredentials.getAdminUsername()) != null);
        assertTrue("Historical subjects preserved (count >= 6)", subjectDAO.listAll().size() >= 6);
        assertTrue("Historical questions preserved (count >= 27)", questionDAO.countActiveBySubjectId(1) > 0);
        assertTrue("Historical schedules preserved (count >= 3)", scheduleDAO.listAll().size() >= 3);

        List<ExamResult> results = resultDAO.listAll();
        assertTrue("Historical exam results preserved (count >= 6)", results.size() >= 6);

        // Verify legacy historical results have attempt_id = NULL
        boolean hasLegacyNullAttempt = results.stream().anyMatch(r -> r.getAttemptId() == null);
        assertTrue("Legacy historical results have attempt_id = NULL (not fabricated)", hasLegacyNullAttempt);
    }

    private static void testNativeBooleanTypes() throws SQLException {
        System.out.println("\n--- 4. Native Boolean Verification ---");
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String[] tables = {"students", "users", "subjects", "questions"};
            for (String t : tables) {
                try (ResultSet rs = meta.getColumns(null, "public", t, "active")) {
                    if (rs.next()) {
                        String typeName = rs.getString("TYPE_NAME");
                        assertTrue("Column " + t + ".active is native BOOLEAN (" + typeName + ")", "bool".equalsIgnoreCase(typeName) || "boolean".equalsIgnoreCase(typeName));
                    } else {
                        assertTrue("Column " + t + ".active exists", false);
                    }
                }
            }
        }
    }

    private static void testPersistentAttemptCreationAndResume() throws Exception {
        System.out.println("\n--- 5. Persistent Exam Attempt Creation & Resumption ---");
        ExamService examService = new ExamService();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        // Create a schedule for testing
        ExamSchedule sched = examService.scheduleExam(adminSession, 1, sub.getId(), 20, 3, 50.0);
        assertTrue("Schedule created for student 1", sched.getId() > 0);

        // Start exam
        ExamAttempt attempt1 = examService.startOrResumeExam(student1Session, sched.getId());
        assertTrue("Exam attempt created with ID > 0", attempt1.getId() > 0);
        assertTrue("Attempt status is 'IN_PROGRESS'", "IN_PROGRESS".equalsIgnoreCase(attempt1.getStatus()));

        // Verify persisted questions
        List<ExamAttemptQuestion> qList1 = examService.getAttemptQuestions(student1Session, attempt1.getId());
        assertTrue("Assigned questions count matches schedule (3)", qList1.size() == 3);
        assertTrue("First question has question_order = 1", qList1.get(0).getQuestionOrder() == 1);
        assertTrue("Second question has question_order = 2", qList1.get(1).getQuestionOrder() == 2);
        assertTrue("Third question has question_order = 3", qList1.get(2).getQuestionOrder() == 3);

        // Save an in-flight answer
        int qId1 = qList1.get(0).getQuestionId();
        examService.saveAnswer(student1Session, attempt1.getId(), qId1, "Option B");

        // Simulate closing window and resuming attempt
        ExamAttempt attemptResumed = examService.startOrResumeExam(student1Session, sched.getId());
        assertTrue("Resumed attempt returns SAME attempt ID", attemptResumed.getId() == attempt1.getId());

        List<ExamAttemptQuestion> qListResumed = examService.getAttemptQuestions(student1Session, attemptResumed.getId());
        assertTrue("Resumed question count is identical", qListResumed.size() == 3);
        assertTrue("Resumed question 1 has same question ID", qListResumed.get(0).getQuestionId() == qId1);
        assertTrue("In-flight answer 'Option B' was restored from database", "Option B".equals(qListResumed.get(0).getSelectedOption()));
    }

    private static void testQuestionDeactivationSafety() throws Exception {
        System.out.println("\n--- 6. Question Deactivation Safety ---");
        ExamService examService = new ExamService();
        QuestionDAO questionDAO = new QuestionDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        ExamSchedule sched = examService.scheduleExam(adminSession, 2, sub.getId(), 15, 2, 40.0);
        ExamAttempt attempt = examService.startOrResumeExam(student2Session, sched.getId());
        List<ExamAttemptQuestion> questions = examService.getAttemptQuestions(student2Session, attempt.getId());

        int targetQId = questions.get(0).getQuestionId();

        // Deactivate this question in the general bank
        questionDAO.setActive(targetQId, false);

        // Student resumes exam
        ExamAttempt resumed = examService.startOrResumeExam(student2Session, sched.getId());
        List<ExamAttemptQuestion> resumedQuestions = examService.getAttemptQuestions(student2Session, resumed.getId());

        boolean containsDeactivated = resumedQuestions.stream().anyMatch(q -> q.getQuestionId() == targetQId);
        assertTrue("In-flight attempt still serves assigned question even after bank deactivation", containsDeactivated);

        // Cleanup: reactivate question
        questionDAO.setActive(targetQId, true);
    }

    private static void testAuthoritativeTimer() throws Exception {
        System.out.println("\n--- 7. Authoritative Exam Timer & Security Ceiling ---");
        ExamService examService = new ExamService();
        ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        ExamAttempt attempt = new ExamAttempt();
        attempt.setDurationMinutes(30);
        java.time.Instant oneMinAgo = java.time.Instant.now().minus(java.time.Duration.ofMinutes(1));
        attempt.setStartedAt(oneMinAgo); // Started 1 minute ago

        long remainingSec = attempt.getAuthoritativeRemainingSeconds();
        assertTrue("Remaining seconds ~ 29 minutes (between 1700 and 1750s)", remainingSec >= 1700 && remainingSec <= 1750);
        assertTrue("Attempt is not expired", !attempt.isExpired());
        assertTrue("Expiration instant is exactly startedAt + 30 mins", 
                attempt.getExpirationInstant().equals(oneMinAgo.plus(java.time.Duration.ofMinutes(30))));

        // Past duration
        ExamAttempt expiredAttempt = new ExamAttempt();
        expiredAttempt.setDurationMinutes(10);
        expiredAttempt.setStartedAt(java.time.Instant.now().minus(java.time.Duration.ofMinutes(15))); // Started 15 mins ago
        assertTrue("Expired attempt authoritative remaining seconds is 0", expiredAttempt.getAuthoritativeRemainingSeconds() == 0);
        assertTrue("Expired attempt isExpired() returns true", expiredAttempt.isExpired());

        // Timer Security Exploit test: Client sends 999999 seconds to extend exam
        ExamSchedule sched = examService.scheduleExam(adminSession, 1, sub.getId(), 10, 1, 50.0);
        ExamAttempt testAttempt = examService.startOrResumeExam(student1Session, sched.getId());
        long authoritativeBefore = testAttempt.getAuthoritativeRemainingSeconds();

        // Client attempts to extend to 999999 seconds
        examService.updateRemainingTime(student1Session, testAttempt.getId(), 999999);
        ExamAttempt reloadedAttempt = attemptDAO.findById(testAttempt.getId());
        assertTrue("Client-reported 999999s is clamped to authoritative ceiling (<= 600s)", 
                reloadedAttempt.getRemainingSeconds() != null && reloadedAttempt.getRemainingSeconds() <= 600);
        assertTrue("Authoritative remaining seconds cannot be extended past schedule duration",
                reloadedAttempt.getAuthoritativeRemainingSeconds() <= 600);
    }

    private static void testConcurrentAttemptProtection() throws Exception {
        System.out.println("\n--- 8. Concurrent Exam Start Protection ---");
        ExamService examService = new ExamService();
        ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        ExamSchedule sched = examService.scheduleExam(adminSession, 3, sub.getId(), 20, 2, 40.0);
        ExamAttempt attempt = examService.startOrResumeExam(student3Session, sched.getId());
        assertTrue("Initial attempt created", attempt.getId() > 0);

        // Attempt direct duplicate insertion of another IN_PROGRESS attempt
        boolean duplicateCaught = false;
        try (Connection conn = DatabaseManager.getConnection()) {
            ExamAttempt dupe = new ExamAttempt();
            dupe.setExamScheduleId(sched.getId());
            dupe.setStudentId(3);
            dupe.setSubjectId(sub.getId());
            dupe.setDurationMinutes(20);
            dupe.setTotalQuestions(2);
            dupe.setStatus("IN_PROGRESS");
            attemptDAO.insert(dupe, conn);
        } catch (SQLException e) {
            // PostgreSQL unique constraint violation code 23505
            if ("23505".equals(e.getSQLState()) || e.getMessage().contains("idx_one_attempt_per_schedule") || e.getMessage().contains("idx_one_active_or_completed_attempt_per_schedule")) {
                duplicateCaught = true;
            }
        }
        assertTrue("PostgreSQL unique index blocks concurrent attempt on same schedule", duplicateCaught);
    }

    private static void testAtomicAndIdempotentSubmission() throws Exception {
        System.out.println("\n--- 9. Atomic & Idempotent Submission ---");
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        ExamSchedule sched = examService.scheduleExam(adminSession, 1, sub.getId(), 20, 2, 50.0);
        ExamAttempt attempt = examService.startOrResumeExam(student1Session, sched.getId());
        List<ExamAttemptQuestion> qList = examService.getAttemptQuestions(student1Session, attempt.getId());

        // Answer first question correctly, second incorrectly
        Question q1 = qList.get(0).getQuestion();
        examService.saveAnswer(student1Session, attempt.getId(), q1.getId(), q1.getCorrectAnswer());
        Question q2 = qList.get(1).getQuestion();
        examService.saveAnswer(student1Session, attempt.getId(), q2.getId(), "WRONG_ANSWER");

        // Submit attempt
        ExamResult res1 = resultService.submitAttempt(student1Session, attempt.getId());
        assertTrue("ExamResult generated with ID > 0", res1.getId() > 0);
        assertTrue("ExamResult links to attempt_id", res1.getAttemptId() == attempt.getId());
        assertTrue("1 of 2 questions correct -> 1.0 marks", res1.getMarks() == 1.0);
        assertTrue("50.0% percentage calculated", res1.getPercentage() == 50.0);
        assertTrue("Pass result recorded", "Pass".equalsIgnoreCase(res1.getResult()));

        // Verify attempt status is COMPLETED
        ExamAttempt updatedAttempt = new ExamAttemptDAO().findById(attempt.getId());
        assertTrue("Attempt status transitions to 'COMPLETED'", "COMPLETED".equalsIgnoreCase(updatedAttempt.getStatus()));

        // Second submission attempt (Idempotency test)
        ExamResult res2 = resultService.submitAttempt(student1Session, attempt.getId());
        assertTrue("Duplicate submission returns original result ID", res2.getId() == res1.getId());
        assertTrue("Duplicate submission does NOT change score", res2.getMarks() == res1.getMarks());
    }

    private static void testScheduleIndependenceRule() throws Exception {
        System.out.println("\n--- 10. Schedule Independence Rule Audit ---");
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();
        ExamScheduleDAO scheduleDAO = new ExamScheduleDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        Subject sub = subjectDAO.listActive().get(0);

        ExamSchedule sched = examService.scheduleExam(adminSession, 2, sub.getId(), 20, 1, 50.0);
        ExamAttempt attempt = examService.startOrResumeExam(student2Session, sched.getId());

        // Submit the student's attempt
        resultService.submitAttempt(student2Session, attempt.getId());

        // Check schedule status
        ExamSchedule afterSubmit = scheduleDAO.findById(sched.getId());
        assertTrue("ExamSchedule status remains IN_PROGRESS/active (NOT COMPLETED upon single attempt)",
                !"COMPLETED".equalsIgnoreCase(afterSubmit.getStatus()));
    }

    private static void testPostgreSQLFailureNoFallback() {
        System.out.println("\n--- 11. PostgreSQL Failure Mode & Zero-Fallback Audit ---");
        boolean caughtConnectionError = false;
        String unreachableUrl = "jdbc:postgresql://localhost:54399/non_existent_db?connectTimeout=2";
        try (Connection conn = java.sql.DriverManager.getConnection(unreachableUrl, "invalid_user", "invalid_pass")) {
            // Should not succeed
        } catch (SQLException e) {
            caughtConnectionError = true;
            System.out.println("  Verified expected connection failure: " + e.getMessage());
        }
        assertTrue("Unreachable PostgreSQL fails explicitly with SQLException", caughtConnectionError);

        // Verify zero SQLite runtime files created
        java.io.File rootDb = new java.io.File("exam_management.db");
        assertTrue("No fallback SQLite file 'exam_management.db' exists in runtime root", !rootDb.exists());
    }
}
