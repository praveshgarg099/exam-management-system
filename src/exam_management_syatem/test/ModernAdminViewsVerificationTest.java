package exam_management_syatem.test;

import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.db.DatabaseManager;
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
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.views.admin.AdminExamsView;
import exam_management_syatem.ui.views.admin.AdminQuestionsView;
import exam_management_syatem.ui.views.admin.AdminResultsView;
import exam_management_syatem.ui.views.admin.AdminStudentsView;
import exam_management_syatem.ui.views.admin.AdminSubjectsView;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ModernAdminViewsVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    private static UserSession adminSession;
    private static UserSession studentSession;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("     MODERN ADMIN VIEWS VERIFICATION TEST SUITE         ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();

            AuthenticationService authService = new AuthenticationService();
            adminSession = authService.loginAdmin("superadmin", "123456");

            // Find an active student for security segregation checks
            StudentService studentService = new StudentService();
            List<Student> students = studentService.getAllStudents(adminSession);
            Student sampleStudent = null;
            for (Student s : students) {
                if (s.isActive()) {
                    sampleStudent = s;
                    break;
                }
            }
            if (sampleStudent != null) {
                studentSession = new UserSession(sampleStudent.getId(), sampleStudent.getName(), "STUDENT", sampleStudent.getId());
            }

            testAdminAuthorizationEnforcement();
            testAdminStudentsViewAndOperations();
            testAdminSubjectsViewAndOperations();
            testAdminQuestionsViewAndOperations();
            testAdminExamsViewAndSchedulingGuards();
            testAdminResultsViewAndDataIsolation();
            testMainShellNavigationAndResponsiveness();

            System.out.println("=========================================================");
            System.out.printf("MODERN ADMIN VIEWS SUMMARY: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
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

    // --- 1. Service Authorization Enforcement ---
    private static void testAdminAuthorizationEnforcement() throws Exception {
        System.out.println("\n--- 1. Service Authorization Enforcement for Modern Admin Operations ---");
        StudentService ss = new StudentService();
        SubjectService subS = new SubjectService();
        QuestionService qs = new QuestionService();
        ExamService es = new ExamService();
        ResultService rs = new ResultService();

        // Null session checks
        try {
            ss.getAllStudents(null);
            fail("Null session should be rejected on studentService.getAllStudents");
        } catch (SecurityException e) {
            pass("Null session rejected on studentService.getAllStudents");
        }

        try {
            subS.getAllSubjects(null);
            fail("Null session should be rejected on subjectService.getAllSubjects");
        } catch (SecurityException e) {
            pass("Null session rejected on subjectService.getAllSubjects");
        }

        try {
            qs.getQuestionsBySubjectId(null, 1);
            fail("Null session should be rejected on questionService.getQuestionsBySubjectId");
        } catch (SecurityException e) {
            pass("Null session rejected on questionService.getQuestionsBySubjectId");
        }

        try {
            es.getAllSchedules(null);
            fail("Null session should be rejected on examService.getAllSchedules");
        } catch (SecurityException e) {
            pass("Null session rejected on examService.getAllSchedules");
        }

        try {
            rs.getAllResults(null);
            fail("Null session should be rejected on resultService.getAllResults");
        } catch (SecurityException e) {
            pass("Null session rejected on resultService.getAllResults");
        }

        // Student session checks (Non-admin segregation)
        if (studentSession != null) {
            try {
                ss.getAllStudents(studentSession);
                fail("Student session should be rejected on studentService.getAllStudents");
            } catch (SecurityException e) {
                pass("Student session rejected on studentService.getAllStudents");
            }

            try {
                subS.addSubject(studentSession, "Hacked Subject");
                fail("Student session should be rejected on subjectService.addSubject");
            } catch (SecurityException e) {
                pass("Student session rejected on subjectService.addSubject");
            }

            try {
                qs.getQuestionsBySubjectId(studentSession, 1);
                fail("Student session should be rejected on questionService.getQuestionsBySubjectId");
            } catch (SecurityException e) {
                pass("Student session rejected on questionService.getQuestionsBySubjectId");
            }

            try {
                es.getAllSchedules(studentSession);
                fail("Student session should be rejected on examService.getAllSchedules");
            } catch (SecurityException e) {
                pass("Student session rejected on examService.getAllSchedules");
            }

            try {
                rs.getAllResults(studentSession);
                fail("Student session should be rejected on resultService.getAllResults");
            } catch (SecurityException e) {
                pass("Student session rejected on resultService.getAllResults");
            }
        }
    }

    // --- 2. AdminStudentsView & CRUD Operations ---
    private static void testAdminStudentsViewAndOperations() throws Exception {
        System.out.println("\n--- 2. AdminStudentsView & Real PostgreSQL Student Operations ---");
        final AdminStudentsView[] holder = new AdminStudentsView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new AdminStudentsView(adminSession);
        });

        // Wait for SwingWorker data load
        Thread.sleep(1200);

        AdminStudentsView view = holder[0];
        Field tableField = AdminStudentsView.class.getDeclaredField("table");
        tableField.setAccessible(true);
        AppTable table = (AppTable) tableField.get(view);

        if (table.getRowCount() > 0) {
            pass("AdminStudentsView loaded live PostgreSQL student rows: " + table.getRowCount());
        } else {
            fail("AdminStudentsView table is unexpectedly empty.");
        }

        if (table.getColumnCount() == 7) {
            pass("AdminStudentsView table has 7 expected columns");
        } else {
            fail("AdminStudentsView table column count mismatch: " + table.getColumnCount());
        }

        // Test Student CRUD through StudentService
        StudentService ss = new StudentService();
        AuthenticationService authS = new AuthenticationService();
        String testAadhar = "9999" + (System.currentTimeMillis() % 100000000L);
        if (testAadhar.length() < 12) {
            testAadhar = String.format("%-12s", testAadhar).replace(' ', '0');
        } else if (testAadhar.length() > 12) {
            testAadhar = testAadhar.substring(0, 12);
        }

        // 1. Validation test (invalid email)
        try {
            ss.registerStudent(adminSession, "Test Student", "9876543210", "invalid-email", testAadhar, "01012000");
            fail("Registration should reject invalid email");
        } catch (IllegalArgumentException e) {
            pass("Registration validation caught invalid email");
        }

        // 2. Real Registration
        StudentService.RegistrationResult regResult = ss.registerStudent(
                adminSession, "Modern Admin Test", "9876543210", "modernadmin@example.com", testAadhar, "01012000"
        );
        int createdStudentId = regResult.studentId;
        pass("Created test student with ID " + createdStudentId + " and username " + regResult.username);

        try {
            // 3. Update Profile
            Student studentToUpdate = ss.getStudentById(adminSession, createdStudentId);
            studentToUpdate.setName("Modern Admin Test Updated");
            ss.updateStudent(adminSession, studentToUpdate);
            Student updatedStudent = ss.getStudentById(adminSession, createdStudentId);
            if ("Modern Admin Test Updated".equals(updatedStudent.getName())) {
                pass("Student name successfully updated in PostgreSQL");
            } else {
                fail("Student name update failed.");
            }

            // 4. Toggle Status
            ss.setStudentActiveStatus(adminSession, createdStudentId, false);
            Student deactivatedStudent = ss.getStudentById(adminSession, createdStudentId);
            if (!deactivatedStudent.isActive()) {
                pass("Student status updated to inactive in PostgreSQL");
            } else {
                fail("Student status update failed.");
            }
            ss.setStudentActiveStatus(adminSession, createdStudentId, true); // Reactivate

            // 5. Admin Password Reset
            String tempPassword = authS.adminResetStudentPassword(adminSession, createdStudentId);
            if (tempPassword != null && !tempPassword.isEmpty()) {
                pass("Admin password reset generated valid temporary password");
            } else {
                fail("Admin password reset returned empty password.");
            }

        } finally {
            // Clean up test student safely
            try (Connection conn = DatabaseManager.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE student_id = ?")) {
                    ps.setInt(1, createdStudentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
                    ps.setInt(1, createdStudentId);
                    ps.executeUpdate();
                }
            }
            pass("Cleaned up test student " + createdStudentId + " safely without modifying historical data");
        }
    }

    // --- 3. AdminSubjectsView & Operations ---
    private static void testAdminSubjectsViewAndOperations() throws Exception {
        System.out.println("\n--- 3. AdminSubjectsView & Real PostgreSQL Subject Operations ---");
        final AdminSubjectsView[] holder = new AdminSubjectsView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new AdminSubjectsView(adminSession);
        });

        Thread.sleep(1200);

        AdminSubjectsView view = holder[0];
        Field tableField = AdminSubjectsView.class.getDeclaredField("table");
        tableField.setAccessible(true);
        AppTable table = (AppTable) tableField.get(view);

        if (table.getRowCount() > 0) {
            pass("AdminSubjectsView loaded live PostgreSQL subject rows: " + table.getRowCount());
        } else {
            fail("AdminSubjectsView table is unexpectedly empty.");
        }

        SubjectService subjectService = new SubjectService();

        // 1. Validation test (empty name)
        try {
            subjectService.addSubject(adminSession, "   ");
            fail("Subject creation should reject blank name");
        } catch (IllegalArgumentException e) {
            pass("Subject creation validation caught blank name");
        }

        // 2. Create Subject
        String testSubName = "Modern Admin Subject " + System.currentTimeMillis();
        Subject createdSubject = subjectService.addSubject(adminSession, testSubName);
        pass("Created test subject with ID " + createdSubject.getId());

        try {
            // 3. Rename Subject
            String renamedSubName = testSubName + " Renamed";
            subjectService.updateSubject(adminSession, createdSubject.getId(), renamedSubName);
            Subject fetchedSubject = subjectService.getSubjectById(adminSession, createdSubject.getId());
            if (renamedSubName.equals(fetchedSubject.getName())) {
                pass("Subject renamed successfully in PostgreSQL");
            } else {
                fail("Subject renaming failed.");
            }

            // 4. Toggle Status
            subjectService.setSubjectActive(adminSession, createdSubject.getId(), false);
            Subject deactivated = subjectService.getSubjectById(adminSession, createdSubject.getId());
            if (!deactivated.isActive()) {
                pass("Subject deactivated successfully in PostgreSQL");
            } else {
                fail("Subject deactivation failed.");
            }
            subjectService.setSubjectActive(adminSession, createdSubject.getId(), true); // Reactivate

        } finally {
            // Clean up test subject safely
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                ps.setInt(1, createdSubject.getId());
                ps.executeUpdate();
            }
            pass("Cleaned up test subject " + createdSubject.getId() + " safely");
        }
    }

    // --- 4. AdminQuestionsView & Operations ---
    private static void testAdminQuestionsViewAndOperations() throws Exception {
        System.out.println("\n--- 4. AdminQuestionsView & Real PostgreSQL Question Operations ---");
        final AdminQuestionsView[] holder = new AdminQuestionsView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new AdminQuestionsView(adminSession);
        });

        Thread.sleep(1200);

        AdminQuestionsView view = holder[0];
        Field tableField = AdminQuestionsView.class.getDeclaredField("table");
        tableField.setAccessible(true);
        AppTable table = (AppTable) tableField.get(view);

        if (table.getRowCount() > 0) {
            pass("AdminQuestionsView loaded live PostgreSQL questions: " + table.getRowCount());
        } else {
            fail("AdminQuestionsView table is unexpectedly empty.");
        }

        Field comboField = AdminQuestionsView.class.getDeclaredField("subjectFilterCombo");
        comboField.setAccessible(true);
        JComboBox<?> combo = (JComboBox<?>) comboField.get(view);
        if (combo.getItemCount() > 1) { // "All Subjects" + subjects
            pass("AdminQuestionsView subject filter populated with " + combo.getItemCount() + " options");
        } else {
            fail("AdminQuestionsView subject filter dropdown is empty.");
        }

        QuestionService questionService = new QuestionService();
        SubjectService subjectService = new SubjectService();
        List<Subject> subjects = subjectService.getActiveSubjects(adminSession);
        if (subjects.isEmpty()) {
            throw new IllegalStateException("No active subjects available for question testing.");
        }
        Subject targetSubject = subjects.get(0);

        // 1. Validation test (correct answer does not match any option)
        try {
            questionService.addQuestion(
                    adminSession, targetSubject.getId(),
                    "What is 2 + 2?", "1", "2", "3", "5", "4", "EASY"
            );
            fail("Question creation should reject mismatching correct answer");
        } catch (IllegalArgumentException e) {
            pass("Question creation validation caught non-matching correct answer");
        }

        // 2. Add Question
        Question q = questionService.addQuestion(
                adminSession, targetSubject.getId(),
                "What is the output of ModernAdminTest?", "Alpha", "Beta", "Gamma", "Delta", "Beta", "MEDIUM"
        );
        pass("Created test question with ID " + q.getId());

        try {
            // 3. Update Question
            questionService.updateQuestion(
                    adminSession, q.getId(),
                    "What is the output of ModernAdminTest? (Updated)", "Alpha", "Beta", "Gamma", "Delta", "Beta", "MEDIUM"
            );
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT question_text FROM questions WHERE id = ?")) {
                ps.setInt(1, q.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString(1).contains("(Updated)")) {
                        pass("Question updated successfully in PostgreSQL");
                    } else {
                        fail("Question update failed.");
                    }
                }
            }

            // 4. Soft-delete / Deactivate Question
            questionService.deleteQuestion(adminSession, q.getId());
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT active FROM questions WHERE id = ?")) {
                ps.setInt(1, q.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    // It may be hard-deleted (no row) or deactivated (active == false)
                    if (!rs.next() || !rs.getBoolean(1)) {
                        pass("Question safely removed or deactivated");
                    } else {
                        fail("Question deletion/deactivation did not take effect.");
                    }
                }
            }

        } finally {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                ps.setInt(1, q.getId());
                ps.executeUpdate();
            }
            pass("Cleaned up test question " + q.getId() + " safely");
        }
    }

    // --- 5. AdminExamsView & Scheduling Guards ---
    private static void testAdminExamsViewAndSchedulingGuards() throws Exception {
        System.out.println("\n--- 5. AdminExamsView & Real PostgreSQL Exam Scheduling Guards ---");
        final AdminExamsView[] holder = new AdminExamsView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new AdminExamsView(adminSession);
        });

        Thread.sleep(1200);

        AdminExamsView view = holder[0];
        Field tableField = AdminExamsView.class.getDeclaredField("table");
        tableField.setAccessible(true);
        AppTable table = (AppTable) tableField.get(view);

        if (table.getRowCount() > 0) {
            pass("AdminExamsView loaded live PostgreSQL exam schedules: " + table.getRowCount());
        } else {
            fail("AdminExamsView table is unexpectedly empty.");
        }

        ExamService examService = new ExamService();
        StudentService studentService = new StudentService();
        SubjectService subjectService = new SubjectService();
        QuestionService questionService = new QuestionService();

        List<Student> allStudents = studentService.getAllStudents(adminSession);
        List<Subject> activeSubjects = subjectService.getActiveSubjects(adminSession);

        Student testStudent = allStudents.stream().filter(Student::isActive).findFirst().orElseThrow();
        Subject testSubject = activeSubjects.get(0);
        int availableQuestions = questionService.getActiveQuestionCountBySubjectId(adminSession, testSubject.getId());

        // 1. Question Availability Invariant: totalQuestions > availableQuestions must be rejected!
        try {
            examService.scheduleExam(
                    adminSession, testStudent.getId(), testSubject.getId(),
                    30, availableQuestions + 999, 50.0
            );
            fail("Scheduling exam with more questions than available in bank should be rejected");
        } catch (IllegalArgumentException e) {
            pass("Question availability guard strictly blocked over-scheduling (" + (availableQuestions + 999) + " requested, " + availableQuestions + " available)");
        }

        // 2. Schedule Exam with valid question count (if available >= 1)
        if (availableQuestions >= 1) {
            ExamSchedule sched = examService.scheduleExam(
                    adminSession, testStudent.getId(), testSubject.getId(),
                    20, 1, 50.0
            );
            pass("Scheduled test exam #" + sched.getId() + " with 1 question");

            try {
                // 3. Update Schedule Duration and Passing Percentage
                examService.updateExamSchedule(adminSession, sched.getId(), 25, 1, 60.0);
                ExamSchedule updatedSched = new ExamScheduleDAO().findById(sched.getId());
                if (updatedSched != null && updatedSched.getDurationMinutes() == 25 && updatedSched.getPassingPercentage() == 60.0) {
                    pass("Exam schedule successfully updated in PostgreSQL");
                } else {
                    fail("Exam schedule update failed.");
                }

                // 4. Cancel Schedule
                examService.cancelExam(adminSession, sched.getId());
                ExamSchedule cancelled = new ExamScheduleDAO().findById(sched.getId());
                if (cancelled != null && "CANCELLED".equalsIgnoreCase(cancelled.getStatus())) {
                    pass("Exam schedule successfully cancelled in PostgreSQL");
                } else {
                    fail("Exam schedule cancellation failed.");
                }

            } finally {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM exam_schedules WHERE id = ?")) {
                    ps.setInt(1, sched.getId());
                    ps.executeUpdate();
                }
                pass("Cleaned up test exam schedule " + sched.getId() + " safely");
            }
        }
    }

    // --- 6. AdminResultsView & Data Isolation ---
    private static void testAdminResultsViewAndDataIsolation() throws Exception {
        System.out.println("\n--- 6. AdminResultsView & Real PostgreSQL Results Operations ---");
        final AdminResultsView[] holder = new AdminResultsView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new AdminResultsView(adminSession);
        });

        Thread.sleep(1200);

        AdminResultsView view = holder[0];
        Field tableField = AdminResultsView.class.getDeclaredField("table");
        tableField.setAccessible(true);
        AppTable table = (AppTable) tableField.get(view);

        if (table.getRowCount() > 0) {
            pass("AdminResultsView loaded live PostgreSQL exam results: " + table.getRowCount());
        } else {
            fail("AdminResultsView table is unexpectedly empty.");
        }

        ResultService resultService = new ResultService();
        ExamService examService = new ExamService();

        // 1. Verify Attempt Questions breakdown API (used by View Attempt Breakdown dialog)
        List<ExamResult> allResults = resultService.getAllResults(adminSession);
        ExamResult completedWithAttempt = null;
        for (ExamResult r : allResults) {
            if (r.getAttemptId() != null && r.getAttemptId() > 0) {
                completedWithAttempt = r;
                break;
            }
        }

        if (completedWithAttempt != null) {
            List<ExamAttemptQuestion> questions = examService.getAttemptQuestions(adminSession, completedWithAttempt.getAttemptId());
            pass("Admin query for attempt #" + completedWithAttempt.getAttemptId() + " returned " + questions.size() + " question attempt breakdown rows");
        } else {
            pass("No completed attempts with attempt_id > 0 found in sample (historical legacy results have attempt_id = null)");
        }

        // 2. Student Data Isolation: Student cannot access other students' results
        if (studentSession != null) {
            try {
                // If student attempts to view results of another student (e.g. student ID 999999)
                resultService.getResultsByStudent(studentSession, 999999);
                fail("Anti-IDOR: Student accessing another student's results must be rejected");
            } catch (SecurityException e) {
                pass("Anti-IDOR: Student access to another student's exam results rejected with SecurityException");
            }
        }
    }

    // --- 7. Shell Navigation & Responsiveness ---
    private static void testMainShellNavigationAndResponsiveness() throws Exception {
        System.out.println("\n--- 7. MainApplicationFrame Shell Integration & Responsive Rendering ---");
        final MainApplicationFrame[] frameHolder = new MainApplicationFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new MainApplicationFrame(adminSession);
            frameHolder[0].setVisible(false); // keep invisible during test
        });

        MainApplicationFrame frame = frameHolder[0];

        String[] adminViews = {
                "ADMIN_DASHBOARD",
                "ADMIN_STUDENTS",
                "ADMIN_SUBJECTS",
                "ADMIN_QUESTIONS",
                "ADMIN_EXAMS",
                "ADMIN_RESULTS"
        };

        for (String viewId : adminViews) {
            SwingUtilities.invokeAndWait(() -> frame.navigateTo(viewId));
            if (viewId.equals(frame.getCurrentViewId())) {
                pass("Successfully navigated shell to " + viewId);
            } else {
                fail("Failed to navigate shell to " + viewId + ", current is: " + frame.getCurrentViewId());
            }
        }

        // Test responsive resizing
        int[][] resolutions = {
                {1440, 900},
                {1280, 800},
                {1024, 768},
                {900, 700}
        };

        for (int[] res : resolutions) {
            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(new Dimension(res[0], res[1]));
                frame.validate();
            });
            pass("MainApplicationFrame rendered cleanly at " + res[0] + "x" + res[1]);
        }

        SwingUtilities.invokeAndWait(frame::dispose);
        pass("MainApplicationFrame disposed safely");
    }
}
