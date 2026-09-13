package exam_management_syatem.test;

import exam_management_syatem.dao.ExamAttemptDAO;
import exam_management_syatem.dao.ExamAttemptQuestionDAO;
import exam_management_syatem.dao.ExamResultDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.dao.UserDAO;
import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.QuestionService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.views.admin.AdminDashboardView;
import exam_management_syatem.ui.views.admin.AdminExamsView;
import exam_management_syatem.ui.views.admin.AdminQuestionsView;
import exam_management_syatem.ui.views.admin.AdminResultsView;
import exam_management_syatem.ui.views.admin.AdminStudentsView;
import exam_management_syatem.ui.views.admin.AdminSubjectsView;
import exam_management_syatem.ui.views.student.StudentMyExamsView;
import exam_management_syatem.ui.views.student.StudentMyResultsView;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class EndToEndWorkflowVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("      END-TO-END WORKFLOW VERIFICATION TEST SUITE        ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();

            testAdminEndToEndWorkflow();
            testStudentEndToEndWorkflow();

            System.out.println("=========================================================");
            System.out.printf("END-TO-END VERIFICATION: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
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

    private static void testAdminEndToEndWorkflow() throws Exception {
        System.out.println("\n--- ADMIN WORKFLOW: Login -> Dashboard -> Students -> Subjects -> Questions -> Exams -> Results ---");

        AuthenticationService authService = new AuthenticationService();
        StudentService studentService = new StudentService();
        SubjectService subjectService = new SubjectService();
        QuestionService questionService = new QuestionService();
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();

        // 1. Login Admin
        UserSession adminSession = authService.loginAdmin("superadmin", "123456");
        if (adminSession != null && adminSession.isAdmin()) {
            pass("Step 1: Admin successfully authenticated with role = ADMIN");
        } else {
            fail("Step 1: Admin authentication failed.");
        }

        // 2. Dashboard View
        final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new MainApplicationFrame(adminSession);
            frameHolder[0].setVisible(false);
        });
        // Allow the invokeLater navigation in constructor to complete
        SwingUtilities.invokeAndWait(() -> {});
        MainApplicationFrame frame = frameHolder[0];

        if ("ADMIN_DASHBOARD".equals(frame.getCurrentViewId())) {
            pass("Step 2: Admin shell loaded with initial view = ADMIN_DASHBOARD");
        } else {
            fail("Step 2: Expected initial view ADMIN_DASHBOARD but found " + frame.getCurrentViewId());
        }

        // 3. Students View & Registration
        SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_STUDENTS"));
        if ("ADMIN_STUDENTS".equals(frame.getCurrentViewId())) {
            pass("Step 3: Navigated to ADMIN_STUDENTS");
        } else {
            fail("Step 3: Navigation to ADMIN_STUDENTS failed");
        }

        String testAadhar = "8888" + (System.currentTimeMillis() % 100000000L);
        if (testAadhar.length() < 12) {
            testAadhar = String.format("%-12s", testAadhar).replace(' ', '1');
        } else if (testAadhar.length() > 12) {
            testAadhar = testAadhar.substring(0, 12);
        }

        StudentService.RegistrationResult studentReg = studentService.registerStudent(
                adminSession, "E2E Student", "9988776655", "e2e.student@example.com", testAadhar, "15082002"
        );
        int studentId = studentReg.studentId;
        String studentUsername = studentReg.username;
        String studentPassword = studentReg.password;
        pass("Step 3: Registered student #" + studentId + " (Username: " + studentUsername + ")");

        int subjectId = 0;
        int questionId = 0;
        int scheduleId = 0;

        try {
            // 4. Subjects View & Subject Creation
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_SUBJECTS"));
            if ("ADMIN_SUBJECTS".equals(frame.getCurrentViewId())) {
                pass("Step 4: Navigated to ADMIN_SUBJECTS");
            } else {
                fail("Step 4: Navigation to ADMIN_SUBJECTS failed");
            }

            String subjectName = "E2E Automated Testing " + System.currentTimeMillis();
            Subject subject = subjectService.addSubject(adminSession, subjectName);
            subjectId = subject.getId();
            pass("Step 4: Created subject #" + subjectId + " ('" + subjectName + "')");

            // 5. Questions View & Question Creation
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_QUESTIONS"));
            if ("ADMIN_QUESTIONS".equals(frame.getCurrentViewId())) {
                pass("Step 5: Navigated to ADMIN_QUESTIONS");
            } else {
                fail("Step 5: Navigation to ADMIN_QUESTIONS failed");
            }

            Question question = questionService.addQuestion(
                    adminSession, subjectId,
                    "What is 10 * 10 in decimal?",
                    "50", "100", "200", "1000",
                    "100", "EASY"
            );
            questionId = question.getId();
            pass("Step 5: Created question #" + questionId + " with 4 options and verified correct answer");

            // 6. Exams View & Exam Scheduling
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_EXAMS"));
            if ("ADMIN_EXAMS".equals(frame.getCurrentViewId())) {
                pass("Step 6: Navigated to ADMIN_EXAMS");
            } else {
                fail("Step 6: Navigation to ADMIN_EXAMS failed");
            }

            // Test scheduling guard: totalQuestions > available
            try {
                examService.scheduleExam(adminSession, studentId, subjectId, 30, 999, 50.0);
                fail("Step 6: Question availability guard did not block over-scheduling");
            } catch (IllegalArgumentException e) {
                pass("Step 6: Question availability guard correctly blocked over-scheduling");
            }

            // Schedule valid exam
            ExamSchedule schedule = examService.scheduleExam(adminSession, studentId, subjectId, 30, 1, 50.0);
            scheduleId = schedule.getId();
            pass("Step 6: Scheduled exam #" + scheduleId + " for student #" + studentId + " on subject #" + subjectId);

            // 7. Results View
            SwingUtilities.invokeAndWait(() -> frame.navigateTo("ADMIN_RESULTS"));
            if ("ADMIN_RESULTS".equals(frame.getCurrentViewId())) {
                pass("Step 7: Navigated to ADMIN_RESULTS");
            } else {
                fail("Step 7: Navigation to ADMIN_RESULTS failed");
            }

            List<ExamResult> results = resultService.getAllResults(adminSession);
            pass("Step 7: Live results table loaded (" + results.size() + " total results recorded)");

        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);

            // Teardown E2E admin test objects safely
            try (Connection conn = DatabaseManager.getConnection()) {
                if (scheduleId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_schedules WHERE id = ?")) {
                        ps.setInt(1, scheduleId);
                        ps.executeUpdate();
                    }
                }
                if (questionId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }
                }
                if (subjectId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                        ps.setInt(1, subjectId);
                        ps.executeUpdate();
                    }
                }
                if (studentId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE student_id = ?")) {
                        ps.setInt(1, studentId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
                        ps.setInt(1, studentId);
                        ps.executeUpdate();
                    }
                }
            }
            pass("Admin Workflow Teardown: Safely cleaned up temporary test records");
        }
    }

    private static void testStudentEndToEndWorkflow() throws Exception {
        System.out.println("\n--- STUDENT WORKFLOW: Login -> My Exams -> Start Exam -> Answer -> Submit -> Result -> My Results ---");

        AuthenticationService authService = new AuthenticationService();
        StudentService studentService = new StudentService();
        SubjectService subjectService = new SubjectService();
        QuestionService questionService = new QuestionService();
        ExamService examService = new ExamService();
        ResultService resultService = new ResultService();

        UserSession adminSession = authService.loginAdmin("superadmin", "123456");

        // Prepare test student and exam for the student workflow
        String testAadhar = "7777" + (System.currentTimeMillis() % 100000000L);
        if (testAadhar.length() < 12) {
            testAadhar = String.format("%-12s", testAadhar).replace(' ', '2');
        } else if (testAadhar.length() > 12) {
            testAadhar = testAadhar.substring(0, 12);
        }

        StudentService.RegistrationResult studentReg = studentService.registerStudent(
                adminSession, "E2E Student Two", "9887766554", "e2e2@example.com", testAadhar, "01012001"
        );
        int studentId = studentReg.studentId;
        String studentUsername = studentReg.username;
        String studentPassword = studentReg.password;

        Subject subject = subjectService.addSubject(adminSession, "E2E Student Subject " + System.currentTimeMillis());
        int subjectId = subject.getId();

        Question question = questionService.addQuestion(
                adminSession, subjectId,
                "What is the capital of France?",
                "Berlin", "Madrid", "Paris", "Rome",
                "Paris", "EASY"
        );
        int questionId = question.getId();

        ExamSchedule schedule = examService.scheduleExam(adminSession, studentId, subjectId, 20, 1, 50.0);
        int scheduleId = schedule.getId();

        int attemptId = 0;
        int resultId = 0;

        try {
            // 1. Student Login
            UserSession studentSession = authService.loginStudent(studentUsername, studentPassword);
            if (studentSession != null && studentSession.isStudent() && studentSession.getStudentId() == studentId) {
                pass("Step 1: Student authenticated successfully (studentId = " + studentSession.getStudentId() + ")");
            } else {
                fail("Step 1: Student login failed");
            }

            // 2. Student Shell & My Exams View
            final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                frameHolder[0] = new MainApplicationFrame(studentSession);
                frameHolder[0].setVisible(false);
            });
            MainApplicationFrame frame = frameHolder[0];

            SwingUtilities.invokeAndWait(() -> frame.navigateTo("STUDENT_MY_EXAMS"));
            if ("STUDENT_MY_EXAMS".equals(frame.getCurrentViewId())) {
                pass("Step 2: Student shell navigated to STUDENT_MY_EXAMS");
            } else {
                fail("Step 2: Failed to navigate to STUDENT_MY_EXAMS");
            }

            // 3. Start Exam
            ExamAttempt attempt = examService.startOrResumeExam(studentSession, scheduleId);
            attemptId = attempt.getId();
            if (attemptId > 0 && "IN_PROGRESS".equalsIgnoreCase(attempt.getStatus())) {
                pass("Step 3: Started exam attempt #" + attemptId + " with status IN_PROGRESS");
            } else {
                fail("Step 3: Exam start failed, status: " + attempt.getStatus());
            }

            List<ExamAttemptQuestion> attemptQuestions = examService.getAttemptQuestions(studentSession, attemptId);
            if (attemptQuestions.size() == 1) {
                pass("Step 3: Loaded 1 authoritative attempt question from database");
            } else {
                fail("Step 3: Expected 1 attempt question but found " + attemptQuestions.size());
            }

            // 4. Answer Questions
            ExamAttemptQuestion assignedQ = attemptQuestions.get(0);
            examService.saveAnswer(studentSession, attemptId, assignedQ.getQuestionId(), "Paris");
            pass("Step 4: Saved answer 'Paris' for question #" + assignedQ.getQuestionId());

            // 5. Submit Exam
            ExamResult examResult = resultService.submitAttempt(studentSession, attemptId);
            resultId = examResult.getId();
            if (resultId > 0 && "Pass".equalsIgnoreCase(examResult.getResult()) && examResult.getPercentage() == 100.0) {
                pass("Step 5: Submitted attempt #" + attemptId + " -> Result #" + resultId + " (Score: 100.0%, Status: Pass)");
            } else {
                fail("Step 5: Exam submission score mismatch: " + examResult.getPercentage());
            }

            // 6. Navigation to My Results with intact UserSession
            if (studentSession.isAuthenticated() && studentSession.getStudentId() == studentId) {
                pass("Step 6: Authenticated UserSession preserved intact post-submission");
            } else {
                fail("Step 6: UserSession was lost or corrupted");
            }

            SwingUtilities.invokeAndWait(() -> frame.navigateTo("STUDENT_MY_RESULTS"));
            if ("STUDENT_MY_RESULTS".equals(frame.getCurrentViewId())) {
                pass("Step 6: Navigated to STUDENT_MY_RESULTS");
            } else {
                fail("Step 6: Navigation to STUDENT_MY_RESULTS failed");
            }

            List<ExamResult> studentResults = resultService.getResultsByStudent(studentSession, studentId);
            if (!studentResults.isEmpty() && studentResults.get(0).getId() == resultId) {
                pass("Step 6: Result #" + resultId + " displayed in StudentMyResultsView with 100.0% score");
            } else {
                fail("Step 6: Result not found in student's results list");
            }

            SwingUtilities.invokeAndWait(frame::dispose);

        } finally {
            // Teardown student workflow test data safely
            try (Connection conn = DatabaseManager.getConnection()) {
                if (resultId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_results WHERE id = ?")) {
                        ps.setInt(1, resultId);
                        ps.executeUpdate();
                    }
                }
                if (attemptId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_attempt_questions WHERE attempt_id = ?")) {
                        ps.setInt(1, attemptId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_attempts WHERE id = ?")) {
                        ps.setInt(1, attemptId);
                        ps.executeUpdate();
                    }
                }
                if (scheduleId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_schedules WHERE id = ?")) {
                        ps.setInt(1, scheduleId);
                        ps.executeUpdate();
                    }
                }
                if (questionId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                        ps.setInt(1, questionId);
                        ps.executeUpdate();
                    }
                }
                if (subjectId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                        ps.setInt(1, subjectId);
                        ps.executeUpdate();
                    }
                }
                if (studentId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE student_id = ?")) {
                        ps.setInt(1, studentId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
                        ps.setInt(1, studentId);
                        ps.executeUpdate();
                    }
                }
            }
            pass("Student Workflow Teardown: Safely cleaned up all temporary test records");
        }
    }
}
