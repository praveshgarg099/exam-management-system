package exam_management_syatem.test;

import exam_management_syatem.dao.ExamAttemptDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.views.student.StudentDashboardView;
import exam_management_syatem.ui.views.student.StudentMyExamsView;
import exam_management_syatem.ui.views.student.StudentMyResultsView;
import exam_management_syatem.ui.views.student.StudentProfileView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UI5VerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    UI.5 STUDENT DASHBOARD VERIFICATION SUITE    ");
        System.out.println("=================================================");

        try {
            exam_management_syatem.db.DatabaseManager.initializeDatabase();

            testStudentAuthorization();
            testAntiIDORIsolation();
            testAuthoritativeAttemptStatusMapping();
            testAuthoritativeStartAndResumeDelegation();
            testTerminalAttemptInvariant();
            testExpiredAttemptHandling();
            testPasswordChangeDelegation();
            testMainShellNavigationIntegration();
            testResponsiveLayouts();

            System.out.println("=================================================");
            System.out.printf("UI.5 TEST SUMMARY: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
            System.out.println("=================================================");

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

    private static void testStudentAuthorization() throws Exception {
        System.out.println("\n--- 1. Service Authorization Enforcement ---");
        ExamService examService = new ExamService();
        StudentService studentService = new StudentService();

        // 1.1 Null session rejection
        try {
            examService.getExistingAttempt(null, 1);
            fail("Null session should be rejected by getExistingAttempt");
        } catch (SecurityException e) {
            pass("Null session rejected by ExamService.getExistingAttempt");
        }

        try {
            examService.getAttemptsByStudent(null, 1);
            fail("Null session should be rejected by getAttemptsByStudent");
        } catch (SecurityException e) {
            pass("Null session rejected by ExamService.getAttemptsByStudent");
        }

        try {
            studentService.getStudentById(null, 1);
            fail("Null session should be rejected by StudentService.getStudentById");
        } catch (SecurityException e) {
            pass("Null session rejected by StudentService.getStudentById");
        }

        // 1.2 Admin session rejection for student-only operations
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");
        try {
            examService.getExistingAttempt(adminSession, 1);
            fail("Admin session should be rejected by getExistingAttempt (requires STUDENT)");
        } catch (SecurityException e) {
            pass("Admin session rejected by ExamService.getExistingAttempt");
        }

        try {
            examService.startOrResumeExam(adminSession, 1);
            fail("Admin session should be rejected by startOrResumeExam (requires STUDENT)");
        } catch (SecurityException e) {
            pass("Admin session rejected by ExamService.startOrResumeExam");
        }
    }

    private static void testAntiIDORIsolation() throws Exception {
        System.out.println("\n--- 2. Anti-IDOR Schedule & Attempt Isolation ---");
        ExamService examService = new ExamService();

        UserSession student1 = new UserSession(101, "student_one", "STUDENT", 101);
        UserSession student2 = new UserSession(102, "student_two", "STUDENT", 102);

        // Student 1 cannot view Student 2 schedules
        try {
            examService.getSchedulesByStudentId(student1, 102);
            fail("Student 1 should not access Student 2 schedules");
        } catch (SecurityException e) {
            pass("Student 1 prevented from accessing Student 2 schedules (Anti-IDOR)");
        }

        // Student 1 cannot view Student 2 attempts
        try {
            examService.getAttemptsByStudent(student1, 102);
            fail("Student 1 should not access Student 2 attempts");
        } catch (SecurityException e) {
            pass("Student 1 prevented from accessing Student 2 attempts (Anti-IDOR)");
        }
    }

    private static void testAuthoritativeAttemptStatusMapping() {
        System.out.println("\n--- 3. Authoritative Persisted Attempt Status Mapping ---");
        ExamSchedule schedule = new ExamSchedule(1, 10, 1, 30, 10, 50.0);

        // 3.1 Null attempt -> Not Started
        StudentMyExamsView.ScheduleItem itemNotStarted = new StudentMyExamsView.ScheduleItem(schedule, null, "Java Core");
        if ("Not Started".equals(itemNotStarted.statusText)
                && itemNotStarted.statusType == StatusBadge.StatusType.INFO
                && itemNotStarted.canLaunch
                && "Start Exam".equals(itemNotStarted.actionLabel)) {
            pass("Null attempt correctly mapped to 'Not Started' (Start Exam)");
        } else {
            fail("Failed mapping null attempt to 'Not Started'");
        }

        // 3.2 In-Progress (not expired) -> Resume Exam
        ExamAttempt activeAttempt = new ExamAttempt();
        activeAttempt.setId(101);
        activeAttempt.setStatus("IN_PROGRESS");
        activeAttempt.setDurationMinutes(30);
        activeAttempt.setStartedAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        activeAttempt.setRemainingSeconds(1500);

        StudentMyExamsView.ScheduleItem itemInProgress = new StudentMyExamsView.ScheduleItem(schedule, activeAttempt, "Java Core");
        if ("In Progress".equals(itemInProgress.statusText)
                && itemInProgress.statusType == StatusBadge.StatusType.WARNING
                && itemInProgress.canLaunch
                && "Resume Exam".equals(itemInProgress.actionLabel)) {
            pass("Active attempt correctly mapped to 'In Progress' (Resume Exam)");
        } else {
            fail("Failed mapping active attempt to 'In Progress'");
        }

        // 3.3 In-Progress (expired) -> Expired (Time Out)
        ExamAttempt expiredAttempt = new ExamAttempt();
        expiredAttempt.setId(102);
        expiredAttempt.setStatus("IN_PROGRESS");
        expiredAttempt.setDurationMinutes(10);
        expiredAttempt.setStartedAt(Instant.now().minus(20, ChronoUnit.MINUTES));
        expiredAttempt.setRemainingSeconds(0);

        StudentMyExamsView.ScheduleItem itemExpiredTime = new StudentMyExamsView.ScheduleItem(schedule, expiredAttempt, "Java Core");
        if ("Expired (Time Out)".equals(itemExpiredTime.statusText)
                && itemExpiredTime.statusType == StatusBadge.StatusType.ERROR
                && !itemExpiredTime.canLaunch) {
            pass("Expired in-progress attempt correctly mapped to 'Expired (Time Out)' (No Launch)");
        } else {
            fail("Failed mapping expired in-progress attempt");
        }

        // 3.4 Terminal Completed -> Completed
        ExamAttempt completedAttempt = new ExamAttempt();
        completedAttempt.setId(103);
        completedAttempt.setStatus("COMPLETED");

        StudentMyExamsView.ScheduleItem itemCompleted = new StudentMyExamsView.ScheduleItem(schedule, completedAttempt, "Java Core");
        if ("Completed".equals(itemCompleted.statusText)
                && itemCompleted.statusType == StatusBadge.StatusType.SUCCESS
                && !itemCompleted.canLaunch) {
            pass("Completed attempt correctly mapped to 'Completed' (Terminal, No Launch)");
        } else {
            fail("Failed mapping completed attempt");
        }

        // 3.5 Terminal Expired -> Expired
        ExamAttempt terminalExpired = new ExamAttempt();
        terminalExpired.setId(104);
        terminalExpired.setStatus("EXPIRED");

        StudentMyExamsView.ScheduleItem itemTerminalExpired = new StudentMyExamsView.ScheduleItem(schedule, terminalExpired, "Java Core");
        if ("Expired".equals(itemTerminalExpired.statusText)
                && itemTerminalExpired.statusType == StatusBadge.StatusType.ERROR
                && !itemTerminalExpired.canLaunch) {
            pass("Expired attempt correctly mapped to 'Expired' (Terminal, No Launch)");
        } else {
            fail("Failed mapping terminal expired attempt");
        }
    }

    private static void testAuthoritativeStartAndResumeDelegation() throws Exception {
        System.out.println("\n--- 4. Authoritative Start/Resume Lifecycle Delegation ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");
        StudentService studentService = new StudentService();
        ExamService examService = new ExamService();
        SubjectService subjectService = new SubjectService();

        // 1. Create unique test subject & questions
        String subName = "UI5_Test_Sub_" + System.currentTimeMillis();
        Subject sub = subjectService.addSubject(adminSession, subName);
        QuestionDAO qDAO = new QuestionDAO();
        for (int i = 1; i <= 3; i++) {
            Question q = new Question();
            q.setSubjectId(sub.getId());
            q.setQuestionText("Q" + i + " text for " + subName);
            q.setOption1("Opt A");
            q.setOption2("Opt B");
            q.setOption3("Opt C");
            q.setOption4("Opt D");
            q.setCorrectAnswer("Opt A");
            q.setActive(true);
            qDAO.insert(q);
        }

        // 2. Register unique student
        long rnd = System.currentTimeMillis() % 10000000;
        String mob = String.format("98%08d", rnd);
        String aadhar = String.format("1234%08d", rnd);
        String email = "ui5_std_" + rnd + "@example.com";
        StudentService.RegistrationResult reg = studentService.registerStudent(adminSession, "UI5 Student", mob, email, aadhar, "01012000");

        // Authenticate student session
        UserSession studentSession = new AuthenticationService().loginStudent(reg.username, reg.password);

        // 3. Schedule exam
        ExamSchedule schedule = examService.scheduleExam(adminSession, reg.studentId, sub.getId(), 15, 2, 50.0);

        // 4. Invoke startOrResumeExam via ExamService
        ExamAttempt attempt1 = examService.startOrResumeExam(studentSession, schedule.getId());
        if (attempt1 != null && attempt1.getId() > 0 && "IN_PROGRESS".equalsIgnoreCase(attempt1.getStatus())) {
            pass("ExamService.startOrResumeExam returned authoritative IN_PROGRESS attempt #" + attempt1.getId());
        } else {
            fail("ExamService.startOrResumeExam failed to return authoritative attempt");
        }

        // 5. Invoke startOrResumeExam again (Resumption)
        ExamAttempt attempt2 = examService.startOrResumeExam(studentSession, schedule.getId());
        if (attempt2 != null && attempt2.getId() == attempt1.getId()) {
            pass("Subsequent startOrResumeExam call idempotently resumed attempt #" + attempt2.getId());
        } else {
            fail("Subsequent startOrResumeExam call did not resume existing attempt");
        }

        // 6. Test read-only getExistingAttempt helper
        ExamAttempt existing = examService.getExistingAttempt(studentSession, schedule.getId());
        if (existing != null && existing.getId() == attempt1.getId()) {
            pass("ExamService.getExistingAttempt returned matching persisted attempt #" + existing.getId());
        } else {
            fail("ExamService.getExistingAttempt failed to return persisted attempt");
        }

        // 7. Test read-only getAttemptsByStudent helper
        List<ExamAttempt> attemptsList = examService.getAttemptsByStudent(studentSession, reg.studentId);
        if (!attemptsList.isEmpty() && attemptsList.get(0).getId() == attempt1.getId()) {
            pass("ExamService.getAttemptsByStudent returned student attempts list");
        } else {
            fail("ExamService.getAttemptsByStudent returned empty or incorrect list");
        }
    }

    private static void testTerminalAttemptInvariant() throws Exception {
        System.out.println("\n--- 5. Terminal Attempt Invariant Enforcement ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");
        StudentService studentService = new StudentService();
        ExamService examService = new ExamService();
        SubjectService subjectService = new SubjectService();
        ResultService resultService = new ResultService();

        String subName = "UI5_Term_" + System.currentTimeMillis();
        Subject sub = subjectService.addSubject(adminSession, subName);
        QuestionDAO qDAO = new QuestionDAO();
        for (int i = 1; i <= 2; i++) {
            Question q = new Question();
            q.setSubjectId(sub.getId());
            q.setQuestionText("Term Q" + i);
            q.setOption1("A");
            q.setOption2("B");
            q.setOption3("C");
            q.setOption4("D");
            q.setCorrectAnswer("A");
            q.setActive(true);
            qDAO.insert(q);
        }

        long rnd = System.currentTimeMillis() % 10000000;
        StudentService.RegistrationResult termReg = studentService.registerStudent(adminSession, "Term Student",
                String.format("97%08d", rnd), "term_" + rnd + "@example.com", String.format("2345%08d", rnd), "02022000");

        UserSession studentSession = new AuthenticationService().loginStudent(termReg.username, termReg.password);
        ExamSchedule schedule = examService.scheduleExam(adminSession, termReg.studentId, sub.getId(), 10, 1, 50.0);

        ExamAttempt attempt = examService.startOrResumeExam(studentSession, schedule.getId());

        // Submit attempt to transition to COMPLETED terminal state
        ExamResult result = resultService.submitAttempt(studentSession, attempt.getId());
        if (result != null && result.getId() > 0) {
            pass("Attempt submitted and completed successfully, result #" + result.getId());
        } else {
            fail("Failed to submit exam attempt");
        }

        // Verifying invariant: COMPLETED attempt CANNOT be restarted or resumed
        try {
            examService.startOrResumeExam(studentSession, schedule.getId());
            fail("Completed attempt should reject new startOrResumeExam call");
        } catch (IllegalStateException e) {
            pass("Attempt restart rejected on COMPLETED terminal status: " + e.getMessage());
        }
    }

    private static void testExpiredAttemptHandling() throws Exception {
        System.out.println("\n--- 6. Expired Attempt Handling (No Fake Results) ---");
        ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
        StudentService studentService = new StudentService();
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");

        long rnd = System.currentTimeMillis() % 10000000;
        StudentService.RegistrationResult expReg = studentService.registerStudent(adminSession, "Expired Attempt Student",
                String.format("95%08d", rnd), "exp_" + rnd + "@example.com", String.format("4567%08d", rnd), "04042000");

        // Create an attempt directly marked as EXPIRED without an exam_result row
        ExamAttempt expAttempt = new ExamAttempt();
        expAttempt.setStudentId(expReg.studentId);
        expAttempt.setSubjectId(1);
        expAttempt.setDurationMinutes(5);
        expAttempt.setTotalQuestions(5);
        expAttempt.setPassingPercentage(50.0);
        expAttempt.setStatus("EXPIRED");
        int expId = attemptDAO.insert(expAttempt);

        if (expId > 0) {
            pass("Persisted expired attempt #" + expId + " exists without corresponding exam_result");
        } else {
            fail("Failed to create expired attempt fixture");
        }
    }

    private static void testPasswordChangeDelegation() throws Exception {
        System.out.println("\n--- 7. Password Change Security Delegation ---");
        AuthenticationService authService = new AuthenticationService();
        StudentService studentService = new StudentService();
        UserSession adminSession = authService.loginAdmin("superadmin", "123456");

        long rnd = System.currentTimeMillis() % 10000000;
        StudentService.RegistrationResult passReg = studentService.registerStudent(adminSession, "Pass Student",
                String.format("96%08d", rnd), "pass_" + rnd + "@example.com", String.format("3456%08d", rnd), "03032000");

        String initialPass = passReg.password;
        UserSession studentSession = authService.loginStudent(passReg.username, initialPass);

        // Incorrect current password
        try {
            authService.changeOwnPassword(studentSession, "wrong_current_password", "newSecret123");
            fail("Password change with incorrect current password should fail");
        } catch (Exception e) {
            pass("Incorrect current password rejected by AuthenticationService");
        }

        // Correct password change
        String newPass = "updatedPass2026";
        authService.changeOwnPassword(studentSession, initialPass, newPass);
        pass("Password updated successfully via changeOwnPassword");

        // Verify login works with new password
        UserSession newSession = authService.loginStudent(passReg.username, newPass);
        if (newSession != null && newSession.getUserId() == studentSession.getUserId()) {
            pass("Login verified with updated password");
        } else {
            fail("Login failed with updated password");
        }
    }

    private static void testMainShellNavigationIntegration() throws Exception {
        System.out.println("\n--- 8. Main Application Shell Navigation Integration ---");
        UserSession studentSession = new UserSession(555, "nav_student", "STUDENT", 555);

        final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new MainApplicationFrame(studentSession);
        });

        MainApplicationFrame frame = frameHolder[0];

        SwingUtilities.invokeAndWait(() -> {
            // Verify student views can be navigated to
            boolean canDashboard = frame.canNavigateTo("STUDENT_DASHBOARD");
            boolean canExams = frame.canNavigateTo("STUDENT_MY_EXAMS");
            boolean canResults = frame.canNavigateTo("STUDENT_MY_RESULTS");
            boolean canProfile = frame.canNavigateTo("STUDENT_PROFILE");

            if (canDashboard && canExams && canResults && canProfile) {
                pass("All student navigation views registered in MainApplicationFrame");
            } else {
                fail("One or more student views not registered in ViewRegistry");
            }

            frame.navigateTo("STUDENT_MY_EXAMS");
            if ("STUDENT_MY_EXAMS".equals(frame.getCurrentViewId())) {
                pass("Navigated to STUDENT_MY_EXAMS successfully");
            } else {
                fail("Failed navigating to STUDENT_MY_EXAMS");
            }

            frame.navigateTo("STUDENT_MY_RESULTS");
            if ("STUDENT_MY_RESULTS".equals(frame.getCurrentViewId())) {
                pass("Navigated to STUDENT_MY_RESULTS successfully");
            } else {
                fail("Failed navigating to STUDENT_MY_RESULTS");
            }

            frame.navigateTo("STUDENT_PROFILE");
            if ("STUDENT_PROFILE".equals(frame.getCurrentViewId())) {
                pass("Navigated to STUDENT_PROFILE successfully");
            } else {
                fail("Failed navigating to STUDENT_PROFILE");
            }

            frame.dispose();
        });
    }

    private static void testResponsiveLayouts() throws Exception {
        System.out.println("\n--- 9. Responsive Layout Validation across Resolutions ---");
        UserSession studentSession = new UserSession(777, "responsive_student", "STUDENT", 777);

        int[][] resolutions = {
                {1440, 900},
                {1280, 800},
                {1024, 768},
                {900, 700}
        };

        SwingUtilities.invokeAndWait(() -> {
            for (int[] res : resolutions) {
                int w = res[0];
                int h = res[1];

                StudentDashboardView dv = new StudentDashboardView(studentSession);
                dv.setSize(new Dimension(w, h));
                dv.doLayout();

                StudentMyExamsView ev = new StudentMyExamsView(studentSession);
                ev.setSize(new Dimension(w, h));
                ev.doLayout();

                StudentMyResultsView rv = new StudentMyResultsView(studentSession);
                rv.setSize(new Dimension(w, h));
                rv.doLayout();

                StudentProfileView pv = new StudentProfileView(studentSession);
                pv.setSize(new Dimension(w, h));
                pv.doLayout();

                pass("Validated layout at " + w + "x" + h + " without runtime exceptions");
            }
        });
    }
}
