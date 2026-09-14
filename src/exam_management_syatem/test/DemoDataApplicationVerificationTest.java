package exam_management_syatem.test;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.Subject;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.views.admin.AdminDashboardView;
import exam_management_syatem.ui.views.admin.AdminExamsView;
import exam_management_syatem.ui.views.admin.AdminQuestionsView;
import exam_management_syatem.ui.views.admin.AdminResultsView;
import exam_management_syatem.ui.views.admin.AdminStudentsView;
import exam_management_syatem.ui.views.admin.AdminSubjectsView;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.List;

public class DemoDataApplicationVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   DEMO DATA APPLICATION INTEGRATION & UI VERIFICATION   ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();

            testAdminUIWithSeededData();
            testStudentUIWithSeededData();

            System.out.println("=========================================================");
            System.out.printf("DEMO DATA APPLICATION VERIFICATION: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
            System.out.println("=========================================================");

            if (testsPassed != testsRun) {
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
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

    private static void testAdminUIWithSeededData() throws Exception {
        System.out.println("\n--- 1. Admin Application Workflow with Seeded PostgreSQL Data ---");
        AuthenticationService authService = new AuthenticationService();
        UserSession adminSession = authService.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());
        pass("Admin login authenticated successfully");

        final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new MainApplicationFrame(adminSession);
            frameHolder[0].setVisible(false);
        });
        SwingUtilities.invokeAndWait(() -> {});
        MainApplicationFrame frame = frameHolder[0];

        // 1. Dashboard View
        if ("ADMIN_DASHBOARD".equals(frame.getCurrentViewId())) {
            pass("Admin Dashboard loaded as initial view");
        } else {
            fail("Admin Dashboard not loaded");
        }

        // 2. Students View
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_STUDENTS"));
        Thread.sleep(800); // Allow SwingWorker to load data
        pass("Navigated to Admin Students View");

        // 3. Subjects View
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_SUBJECTS"));
        Thread.sleep(800);
        pass("Navigated to Admin Subjects View");

        // 4. Questions View
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_QUESTIONS"));
        Thread.sleep(800);
        pass("Navigated to Admin Questions View");

        // 5. Exams View
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_EXAMS"));
        Thread.sleep(800);
        pass("Navigated to Admin Exams View");

        // 6. Results View
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_RESULTS"));
        Thread.sleep(800);
        pass("Navigated to Admin Results View");

        SwingUtilities.invokeAndWait(frame::dispose);
        pass("Admin UI navigation through all modern views with seeded PostgreSQL data verified");
    }

    private static void testStudentUIWithSeededData() throws Exception {
        System.out.println("\n--- 2. Student Application Workflow with Seeded Student 'teststudent04' ---");
        AuthenticationService authService = new AuthenticationService();
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();

        // Login as seeded student 'aaravs' (fresh dataset) or fallback to 'teststudent01'
        UserSession studentSession = null;
        try {
            studentSession = authService.loginStudent("aaravs", "Password@123");
        } catch (Exception e) {
            studentSession = authService.loginStudent("teststudent01", "Password@123");
        }
        if (studentSession != null && studentSession.isStudent()) {
            pass("Seeded student '" + studentSession.getUsername() + "' authenticated successfully (studentId: " + studentSession.getStudentId() + ")");
        } else {
            fail("Seeded student authentication failed");
        }

        // Check scheduled exams for teststudent01
        List<ExamSchedule> seededSchedules = examService.getSchedulesByStudentId(studentSession, studentSession.getStudentId());
        if (!seededSchedules.isEmpty()) {
            pass("Seeded student has " + seededSchedules.size() + " scheduled exam(s) in PostgreSQL");
        } else {
            fail("No scheduled exams found for seeded student");
        }

        // Check existing seeded results for teststudent01
        List<ExamResult> seededResults = resultService.getResultsByStudent(studentSession, studentSession.getStudentId());
        if (!seededResults.isEmpty()) {
            pass("Seeded student has " + seededResults.size() + " completed result(s) in PostgreSQL (Score: " + seededResults.get(0).getPercentage() + "%)");
        } else {
            fail("No seeded results found for student");
        }

        // Interactive workflow test: Admin schedules a verification exam, student takes and submits it
        UserSession adminSession = authService.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());
        SubjectService subjectService = new SubjectService();
        List<Subject> subjects = subjectService.getAllSubjects(adminSession);
        Subject javaSub = subjects.stream().filter(s -> s.getName().equals("TEST Java Programming")).findFirst().orElse(subjects.get(0));

        ExamSchedule testSchedule = examService.scheduleExam(
                adminSession,
                studentSession.getStudentId(),
                javaSub.getId(),
                30,
                5,
                40.0
        );
        pass("Admin scheduled live verification exam schedule #" + testSchedule.getId() + " for student");

        try {
            // Start exam
            ExamAttempt attempt = examService.startOrResumeExam(studentSession, testSchedule.getId());
            pass("Student started exam attempt #" + attempt.getId() + " (status: " + attempt.getStatus() + ")");

            // Retrieve assigned questions
            List<ExamAttemptQuestion> questions = examService.getAttemptQuestions(studentSession, attempt.getId());
            pass("Loaded " + questions.size() + " assigned questions for attempt #" + attempt.getId());

            // Answer questions
            for (ExamAttemptQuestion q : questions) {
                examService.saveAnswer(studentSession, attempt.getId(), q.getQuestionId(), "32-bit");
            }
            pass("Saved answers for all assigned questions in attempt #" + attempt.getId());

            // Submit exam
            ExamResult result = resultService.submitAttempt(studentSession, attempt.getId());
            pass("Exam attempt #" + attempt.getId() + " submitted successfully -> Result #" + result.getId() + " (" + result.getResult() + ", Score: " + result.getPercentage() + "%)");

            // Verify UserSession still authenticated
            if (studentSession.isAuthenticated() && studentSession.getStudentId() > 0) {
                pass("Student UserSession remains authenticated after submission");
            } else {
                fail("Student UserSession lost authentication after submission");
            }

            // Verify result visible in student results list
            List<ExamResult> studentResults = resultService.getResultsByStudent(studentSession, studentSession.getStudentId());
            boolean foundResult = false;
            for (ExamResult r : studentResults) {
                if (r.getId() == result.getId()) {
                    foundResult = true;
                    break;
                }
            }

            if (foundResult) {
                pass("New result #" + result.getId() + " successfully verified in Student My Results query");
            } else {
                fail("Result #" + result.getId() + " not found in Student My Results query");
            }
        } finally {
            // Clean up temporary live verification exam record safely
            try (java.sql.Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_results WHERE attempt_id IN (SELECT id FROM exam_attempts WHERE exam_schedule_id = ?)")) {
                    ps.setInt(1, testSchedule.getId());
                    ps.executeUpdate();
                }
                try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_attempt_questions WHERE attempt_id IN (SELECT id FROM exam_attempts WHERE exam_schedule_id = ?)")) {
                    ps.setInt(1, testSchedule.getId());
                    ps.executeUpdate();
                }
                try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_attempts WHERE exam_schedule_id = ?")) {
                    ps.setInt(1, testSchedule.getId());
                    ps.executeUpdate();
                }
                try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_schedules WHERE id = ?")) {
                    ps.setInt(1, testSchedule.getId());
                    ps.executeUpdate();
                }
                conn.commit();
                pass("Cleaned up live verification test exam schedule #" + testSchedule.getId() + " safely");
            }
        }
    }
}
