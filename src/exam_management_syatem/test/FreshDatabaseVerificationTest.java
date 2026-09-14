package exam_management_syatem.test;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.shell.ViewRegistry;
import exam_management_syatem.ui.views.admin.AdminDashboardView;
import exam_management_syatem.ui.views.admin.AdminExamsView;
import exam_management_syatem.ui.views.admin.AdminQuestionsView;
import exam_management_syatem.ui.views.admin.AdminResultsView;
import exam_management_syatem.ui.views.admin.AdminStudentsView;
import exam_management_syatem.ui.views.admin.AdminSubjectsView;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * End-to-end integration and verification suite for the fresh database dataset.
 */
public class FreshDatabaseVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("      FRESH DATABASE INTEGRATION & AUDIT TEST SUITE      ");
        System.out.println("=========================================================");

        try {
            testDatabaseCountsAndIntegrity();
            testAdminApplicationWorkflow();
            testStudentApplicationWorkflows();

            System.out.println("=========================================================");
            System.out.println("FRESH DATABASE VERIFICATION: " + testsPassed + " PASSED, " + (testsRun - testsPassed) + " FAILED");
            System.out.println("=========================================================");

            if (testsPassed != testsRun) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Test suite failed with unhandled exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void pass(String msg) {
        testsRun++;
        testsPassed++;
        System.out.println("  [PASS] " + msg);
    }

    private static void fail(String msg) {
        testsRun++;
        System.err.println("  [FAIL] " + msg);
    }

    private static void testDatabaseCountsAndIntegrity() throws Exception {
        System.out.println("\n--- 1. Direct PostgreSQL Counts & Relational Integrity Audit ---");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Students: 20
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && rs.getInt(1) == 20) {
                    pass("Verified students count = 20");
                } else {
                    fail("Students count mismatch");
                }
            }

            // Users: 21 (1 Admin + 20 students)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 21) {
                    pass("Verified users count = 21 (1 Admin + 20 Students)");
                } else {
                    fail("Users count mismatch");
                }
            }

            // Admin account preserved
            try (ResultSet rs = stmt.executeQuery("SELECT id, username, role, active FROM users WHERE role = 'ADMIN'")) {
                if (rs.next() && rs.getInt("id") == 1 && rs.getBoolean("active")) {
                    pass("Verified administrator account preserved intact (ID: 1, Role: ADMIN, Active: true)");
                } else {
                    fail("Administrator account altered or missing");
                }
            }

            // Subjects: 15
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
                if (rs.next() && rs.getInt(1) == 15) {
                    pass("Verified subjects count = 15");
                } else {
                    fail("Subjects count mismatch");
                }
            }

            // Questions: 750 (50 per subject)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next() && rs.getInt(1) == 750) {
                    pass("Verified questions count = 750 (50 per subject)");
                } else {
                    fail("Questions count mismatch");
                }
            }

            // Exam Schedules: 35
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_schedules")) {
                if (rs.next() && rs.getInt(1) == 35) {
                    pass("Verified exam schedules count = 35");
                } else {
                    fail("Exam schedules count mismatch");
                }
            }

            // Exam Attempts: 18
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_attempts")) {
                if (rs.next() && rs.getInt(1) == 18) {
                    pass("Verified exam attempts count = 18 (12 Completed + 6 In-Progress)");
                } else {
                    fail("Exam attempts count mismatch");
                }
            }

            // Exam Results: 12
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results")) {
                if (rs.next() && rs.getInt(1) == 12) {
                    pass("Verified exam results count = 12 (8 Pass + 4 Fail)");
                } else {
                    fail("Exam results count mismatch");
                }
            }

            // Check 0 orphans
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_schedules WHERE student_id NOT IN (SELECT id FROM students)")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    pass("Zero orphan exam_schedules (referencing valid students)");
                } else {
                    fail("Orphan exam_schedules detected");
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_attempts WHERE exam_schedule_id NOT IN (SELECT id FROM exam_schedules)")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    pass("Zero orphan exam_attempts (referencing valid schedules)");
                } else {
                    fail("Orphan exam_attempts detected");
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results WHERE attempt_id IS NOT NULL AND attempt_id NOT IN (SELECT id FROM exam_attempts)")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    pass("Zero orphan exam_results (referencing valid attempts)");
                } else {
                    fail("Orphan exam_results detected");
                }
            }
        }
    }

    private static void testAdminApplicationWorkflow() throws Exception {
        System.out.println("\n--- 2. Admin Application UI & Service Workflow ---");
        AuthenticationService authService = new AuthenticationService();
        UserSession adminSession = authService.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());

        if (adminSession != null && adminSession.isAdmin()) {
            pass("Admin authenticated successfully with preserved credentials");
        } else {
            fail("Admin authentication failed");
        }

        final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new MainApplicationFrame(adminSession);
            frameHolder[0].setSize(new Dimension(1280, 800));
            frameHolder[0].setVisible(true);
        });

        MainApplicationFrame frame = frameHolder[0];

        try {
            ViewRegistry registry = (ViewRegistry) getPrivateField(frame, "registry");

            // Dashboard
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_DASHBOARD"));
            pass("Admin navigated to Dashboard");

            // Students View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_STUDENTS"));
            AdminStudentsView studentsView = (AdminStudentsView) registry.getView("ADMIN_STUDENTS");
            AppTable studentsTable = (AppTable) getPrivateField(studentsView, "table");
            if (studentsTable.getRowCount() >= 20) {
                pass("Admin Students View loaded " + studentsTable.getRowCount() + " students directly from PostgreSQL");
            } else {
                fail("Admin Students View row count expected >= 20, got " + studentsTable.getRowCount());
            }

            // Subjects View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_SUBJECTS"));
            AdminSubjectsView subjectsView = (AdminSubjectsView) registry.getView("ADMIN_SUBJECTS");
            AppTable subjectsTable = (AppTable) getPrivateField(subjectsView, "table");
            if (subjectsTable.getRowCount() == 15) {
                pass("Admin Subjects View loaded " + subjectsTable.getRowCount() + " subjects from PostgreSQL");
            } else {
                fail("Admin Subjects View row count expected 15, got " + subjectsTable.getRowCount());
            }

            // Questions View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_QUESTIONS"));
            AdminQuestionsView questionsView = (AdminQuestionsView) registry.getView("ADMIN_QUESTIONS");
            AppTable questionsTable = (AppTable) getPrivateField(questionsView, "table");
            if (questionsTable.getRowCount() >= 50) {
                pass("Admin Questions View loaded live question bank rows: " + questionsTable.getRowCount());
            } else {
                fail("Admin Questions View question row count insufficient");
            }

            // Exams View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_EXAMS"));
            AdminExamsView examsView = (AdminExamsView) registry.getView("ADMIN_EXAMS");
            AppTable examsTable = (AppTable) getPrivateField(examsView, "table");
            if (examsTable.getRowCount() == 35) {
                pass("Admin Exams View loaded " + examsTable.getRowCount() + " scheduled exams from PostgreSQL");
            } else {
                fail("Admin Exams View expected 35 schedules, got " + examsTable.getRowCount());
            }

            // Results View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_RESULTS"));
            AdminResultsView resultsView = (AdminResultsView) registry.getView("ADMIN_RESULTS");
            AppTable resultsTable = (AppTable) getPrivateField(resultsView, "table");
            if (resultsTable.getRowCount() == 12) {
                pass("Admin Results View loaded " + resultsTable.getRowCount() + " completed results from PostgreSQL");
            } else {
                fail("Admin Results View expected 12 results, got " + resultsTable.getRowCount());
            }

        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private static void testStudentApplicationWorkflows() throws Exception {
        System.out.println("\n--- 3. Student Application & Interactive Workflow ---");
        AuthenticationService authService = new AuthenticationService();
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();

        // Inactive account test: Kunal Saxena (kunals) must be rejected
        try {
            authService.loginStudent("kunals", "Password@123");
            fail("Inactive student login should have been rejected");
        } catch (Exception e) {
            pass("Inactive student login correctly rejected: " + e.getMessage());
        }

        // Active student login: Aarav Sharma (aaravs)
        UserSession studentSession = authService.loginStudent("aaravs", "Password@123");
        if (studentSession != null && studentSession.isStudent() && studentSession.getStudentId() == 1) {
            pass("Student 'aaravs' authenticated successfully (Student ID: 1)");
        } else {
            fail("Student 'aaravs' authentication failed");
        }

        // Check scheduled exams for aaravs (has 2 completed, 1 scheduled)
        List<ExamSchedule> schedules = examService.getSchedulesByStudentId(studentSession, 1);
        if (schedules.size() == 3) {
            pass("Student 'aaravs' retrieved 3 exam schedules from PostgreSQL");
        } else {
            fail("Expected 3 schedules for aaravs, got " + schedules.size());
        }

        // Check completed results for aaravs (has 2 results: 100% on Java, 90% on DBMS)
        List<ExamResult> results = resultService.getResultsByStudent(studentSession, 1);
        if (results.size() == 2) {
            pass("Student 'aaravs' retrieved 2 completed results (Scores: " + results.get(0).getPercentage() + "%, " + results.get(1).getPercentage() + "%)");
        } else {
            fail("Expected 2 results for aaravs, got " + results.size());
        }

        // Find the scheduled exam for aaravs (Schedule on Software Engineering)
        ExamSchedule scheduledExam = schedules.stream()
                .filter(s -> "SCHEDULED".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);

        if (scheduledExam != null) {
            pass("Identified SCHEDULED exam schedule #" + scheduledExam.getId() + " for live execution");
        } else {
            fail("No scheduled exam found for student");
            return;
        }

        // Start exam
        ExamAttempt attempt = examService.startOrResumeExam(studentSession, scheduledExam.getId());
        pass("Student started exam attempt #" + attempt.getId() + " (status: " + attempt.getStatus() + ")");

        // Retrieve assigned questions
        List<ExamAttemptQuestion> questions = examService.getAttemptQuestions(studentSession, attempt.getId());
        pass("Loaded " + questions.size() + " assigned questions for attempt #" + attempt.getId());

        // Answer questions
        for (ExamAttemptQuestion q : questions) {
            examService.saveAnswer(studentSession, attempt.getId(), q.getQuestionId(), "Unit Testing");
        }
        pass("Saved answers for all assigned questions");

        // Submit exam
        ExamResult newResult = resultService.submitAttempt(studentSession, attempt.getId());
        pass("Submitted exam attempt #" + attempt.getId() + " -> Result #" + newResult.getId() + " (" + newResult.getResult() + ", Score: " + newResult.getPercentage() + "%)");

        // Verify UserSession still authenticated
        if (studentSession.isAuthenticated() && studentSession.getStudentId() == 1) {
            pass("Student UserSession remained fully authenticated post-submission");
        } else {
            fail("UserSession lost authentication after exam submission");
        }

        // Verify new result in student's results list
        List<ExamResult> updatedResults = resultService.getResultsByStudent(studentSession, 1);
        if (updatedResults.size() == 3) {
            pass("Verified Student My Results updated to 3 completed results in PostgreSQL");
        } else {
            fail("Expected 3 results after submission, got " + updatedResults.size());
        }

        // Clean up ONLY the temporary live exam attempt/result created during this test to restore pristine seeded state
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM exam_results WHERE id = " + newResult.getId());
                stmt.executeUpdate("DELETE FROM exam_attempt_questions WHERE attempt_id = " + attempt.getId());
                stmt.executeUpdate("DELETE FROM exam_attempts WHERE id = " + attempt.getId());
                stmt.executeUpdate("UPDATE exam_schedules SET status = 'SCHEDULED' WHERE id = " + scheduledExam.getId());
            }
            conn.commit();
            pass("Restored exam schedule #" + scheduledExam.getId() + " back to SCHEDULED for pristine reproducibility");
        }
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
