package exam_management_syatem.test;

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
import exam_management_syatem.service.DashboardMetricsService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.QuestionService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

import java.util.List;

public class Phase3FullTestSuite {

    private static int passedTests = 0;
    private static int failedTests = 0;

    private static final UserSession adminSession = new UserSession(1, "admin", "ADMIN", null);

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("   PHASE 3 COMPREHENSIVE AUTOMATED TEST SUITE    ");
        System.out.println("=================================================");

        DatabaseManager.initializeDatabase();

        testStudentManagementAndStatus();
        testPasswordManagement();
        testSubjectManagementAndGuards();
        testQuestionManagementAndValidation();
        testExamScheduleLifecycle();
        testDeterministicQuestionSelection();
        testExamGradingAndResultManagement();
        testDashboardMetrics();

        System.out.println("=================================================");
        System.out.println("TEST SUMMARY: " + passedTests + " PASSED, " + failedTests + " FAILED");
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failedTests++;
        }
    }

    private static void testStudentManagementAndStatus() {
        System.out.println("\n--- Testing Student Management & Status Control ---");
        try {
            StudentService studentService = new StudentService();
            AuthenticationService authService = new AuthenticationService();

            String uniqueAadhar = "8888" + (System.currentTimeMillis() % 100000000L);
            while (uniqueAadhar.length() < 12) uniqueAadhar += "0";
            if (uniqueAadhar.length() > 12) uniqueAadhar = uniqueAadhar.substring(0, 12);

            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "PhaseThree Tester", "9876543210", "p3tester@example.com", uniqueAadhar, "01012000"
            );
            assertTrue("Student registered with ID > 0", reg.studentId > 0);
            assertTrue("Generated username is not empty", reg.username != null && !reg.username.isEmpty());

            // Verify active login works
            UserSession session = authService.loginStudent(reg.username, reg.password);
            assertTrue("Active student can login", session != null && session.getStudentId() == reg.studentId);

            // Test search
            List<Student> searchResults = studentService.searchStudents(adminSession, "p3tester");
            assertTrue("Search returns registered student", !searchResults.isEmpty() && searchResults.get(0).getId() == reg.studentId);

            // Test update
            Student s = studentService.getStudentById(session, reg.studentId);
            s.setName("PhaseThree Updated");
            studentService.updateStudent(session, s);
            Student updated = studentService.getStudentById(session, reg.studentId);
            assertTrue("Student name updated successfully", "PhaseThree Updated".equals(updated.getName()));

            // Deactivate student
            studentService.setStudentActiveStatus(adminSession, reg.studentId, false);
            Student deactivated = studentService.getStudentById(adminSession, reg.studentId);
            assertTrue("Student status is inactive", !deactivated.isActive());

            // Verify deactivated student cannot login
            boolean loginFailed = false;
            try {
                authService.loginStudent(reg.username, reg.password);
            } catch (Exception e) {
                loginFailed = true;
            }
            assertTrue("Deactivated student blocked from logging in", loginFailed);

            // Reactivate student
            studentService.setStudentActiveStatus(adminSession, reg.studentId, true);
            UserSession reSession = authService.loginStudent(reg.username, reg.password);
            assertTrue("Reactivated student can login again", reSession != null);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Student management exception: " + e.getMessage(), false);
        }
    }

    private static void testPasswordManagement() {
        System.out.println("\n--- Testing Student & Admin Password Management ---");
        try {
            StudentService studentService = new StudentService();
            AuthenticationService authService = new AuthenticationService();

            String uniqueAadhar = "7777" + (System.currentTimeMillis() % 100000000L);
            while (uniqueAadhar.length() < 12) uniqueAadhar += "0";
            if (uniqueAadhar.length() > 12) uniqueAadhar = uniqueAadhar.substring(0, 12);

            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Pass Tester", "9876543211", "passtest@example.com", uniqueAadhar, "02022000"
            );

            UserSession studentSession = authService.loginStudent(reg.username, reg.password);

            // 1. Student changes their own password
            boolean changeWrongFails = false;
            try {
                authService.changeOwnPassword(studentSession, "WrongPassword123", "NewSecurePass!1");
            } catch (Exception e) {
                changeWrongFails = true;
            }
            assertTrue("Password change with incorrect current password rejected", changeWrongFails);

            authService.changeOwnPassword(studentSession, reg.password, "NewSecurePass!1");
            UserSession newPassSession = authService.loginStudent(reg.username, "NewSecurePass!1");
            assertTrue("Login with new student password succeeds", newPassSession != null);

            // 2. Admin resets student password
            String tempPass = authService.adminResetStudentPassword(adminSession, reg.studentId);
            assertTrue("Admin password reset generates temporary password", tempPass != null && tempPass.startsWith("Temp@"));

            UserSession tempPassSession = authService.loginStudent(reg.username, tempPass);
            assertTrue("Student can log in using admin-generated temporary password", tempPassSession != null);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Password management exception: " + e.getMessage(), false);
        }
    }

    private static void testSubjectManagementAndGuards() {
        System.out.println("\n--- Testing Subject Management & Guard Rules ---");
        try {
            SubjectService subjectService = new SubjectService();
            ExamService examService = new ExamService();
            QuestionService questionService = new QuestionService();

            String subName = "TestSub_" + System.currentTimeMillis();
            Subject s = subjectService.addSubject(adminSession, subName);
            assertTrue("Subject created with ID > 0", s.getId() > 0 && s.isActive());

            // Rename subject
            String updatedSubName = subName + "_Renamed";
            subjectService.updateSubject(adminSession, s.getId(), updatedSubName);
            Subject renamed = subjectService.getSubjectById(adminSession, s.getId());
            assertTrue("Subject renamed successfully", updatedSubName.equals(renamed.getName()));

            // Add questions and schedule an exam for it
            questionService.addQuestion(adminSession, s.getId(), "Sample Q1", "A", "B", "C", "D", "A");
            ExamSchedule es = examService.scheduleExam(adminSession, 1, s.getId(), 15, 1, 50.0);
            assertTrue("Exam schedule created for subject", es.getId() > 0);

            // Guard rule: Deactivating subject with active SCHEDULED exams MUST fail!
            boolean deactivationBlocked = false;
            try {
                subjectService.setSubjectActive(adminSession, s.getId(), false);
            } catch (IllegalStateException e) {
                deactivationBlocked = true;
            }
            assertTrue("Deactivating subject with active scheduled exams blocked", deactivationBlocked);

            // Cancel the scheduled exam
            examService.cancelExam(adminSession, es.getId());

            // Now deactivating subject should succeed
            subjectService.setSubjectActive(adminSession, s.getId(), false);
            Subject deactivatedSub = subjectService.getSubjectById(adminSession, s.getId());
            assertTrue("Subject successfully deactivated once active exams are cleared", !deactivatedSub.isActive());

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Subject management exception: " + e.getMessage(), false);
        }
    }

    private static void testQuestionManagementAndValidation() {
        System.out.println("\n--- Testing Question Validation & Difficulty ---");
        try {
            SubjectService subjectService = new SubjectService();
            QuestionService questionService = new QuestionService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            // 1. Validation rule: Correct answer MUST match one of the 4 options
            boolean invalidAnsBlocked = false;
            try {
                questionService.addQuestion(adminSession, sub.getId(), "What is 2+2?", "1", "2", "3", "4", "5");
            } catch (IllegalArgumentException e) {
                invalidAnsBlocked = true;
            }
            assertTrue("Question with correct answer not matching any option is rejected", invalidAnsBlocked);

            // 2. Valid question with difficulty
            Question q = questionService.addQuestion(adminSession, sub.getId(), "What is Java?", "Coffee", "Language", "Island", "All", "Language", "HARD");
            assertTrue("Question added with ID > 0", q.getId() > 0);
            assertTrue("Difficulty correctly stored as HARD", "HARD".equals(q.getDifficulty()));

            // 3. Search question
            List<Question> found = questionService.searchQuestions(adminSession, sub.getId(), "Java");
            assertTrue("Question found via search keyword", !found.isEmpty());

            // 4. Soft delete / deactivate
            questionService.setQuestionActive(adminSession, q.getId(), false);
            List<Question> activeQuestions = questionService.getActiveQuestionsBySubjectId(adminSession, sub.getId());
            boolean stillInActiveList = activeQuestions.stream().anyMatch(item -> item.getId() == q.getId());
            assertTrue("Deactivated question excluded from active questions list", !stillInActiveList);

            // Reactivate
            questionService.setQuestionActive(adminSession, q.getId(), true);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Question management exception: " + e.getMessage(), false);
        }
    }

    private static void testExamScheduleLifecycle() {
        System.out.println("\n--- Testing Exam Schedule Lifecycle & State Machine ---");
        try {
            ExamService examService = new ExamService();
            SubjectService subjectService = new SubjectService();
            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            // Guard rule: Question count cannot exceed available active questions
            int activeQCount = new QuestionService().getActiveQuestionCountBySubjectId(adminSession, sub.getId());
            boolean excessiveQBlocked = false;
            try {
                examService.scheduleExam(adminSession, 1, sub.getId(), 20, activeQCount + 50, 40.0);
            } catch (IllegalArgumentException e) {
                excessiveQBlocked = true;
            }
            assertTrue("Scheduling exam with more questions than active questions in bank is rejected", excessiveQBlocked);

            // Create valid schedule
            ExamSchedule es = examService.scheduleExam(adminSession, 1, sub.getId(), 15, Math.min(2, Math.max(1, activeQCount)), 40.0);
            assertTrue("Exam schedule created with status 'SCHEDULED'", "SCHEDULED".equalsIgnoreCase(es.getStatus()));

            // Edit schedule
            examService.updateExamSchedule(adminSession, es.getId(), 25, 1, 55.0);
            ExamSchedule updated = examService.getExamScheduleById(adminSession, es.getId());
            assertTrue("Schedule duration updated to 25 mins", updated.getDurationMinutes() == 25);
            assertTrue("Schedule passing score updated to 55%", updated.getPassingPercentage() == 55.0);

            // Start exam via student session
            UserSession studentSession = new UserSession(1, "student1", "STUDENT", 1);
            ExamAttempt attempt = examService.startOrResumeExam(studentSession, es.getId());
            ExamSchedule inProg = examService.getExamScheduleById(adminSession, es.getId());
            assertTrue("Schedule status transitions to 'IN_PROGRESS'", "IN_PROGRESS".equalsIgnoreCase(inProg.getStatus()));

            // Invalid transition: cannot cancel an IN_PROGRESS exam
            boolean cancelInProgressBlocked = false;
            try {
                examService.cancelExam(adminSession, es.getId());
            } catch (IllegalStateException e) {
                cancelInProgressBlocked = true;
            }
            assertTrue("Cancelling an IN_PROGRESS exam is rejected by state machine", cancelInProgressBlocked);

            // Complete exam
            examService.completeExamSchedule(adminSession, es.getId());
            ExamSchedule completed = examService.getExamScheduleById(adminSession, es.getId());
            assertTrue("Schedule status transitions to 'COMPLETED'", "COMPLETED".equalsIgnoreCase(completed.getStatus()));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Exam schedule lifecycle exception: " + e.getMessage(), false);
        }
    }

    private static void testDeterministicQuestionSelection() {
        System.out.println("\n--- Testing Stable Deterministic Question Selection via Attempts ---");
        try {
            ExamService examService = new ExamService();
            SubjectService subjectService = new SubjectService();
            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            // Create a schedule for student 1
            ExamSchedule es = examService.scheduleExam(adminSession, 1, sub.getId(), 10, 2, 40.0);
            UserSession studentSession = new UserSession(1, "student1", "STUDENT", 1);

            // First start creates attempt with assigned questions
            ExamAttempt attempt1 = examService.startOrResumeExam(studentSession, es.getId());
            List<ExamAttemptQuestion> run1 = examService.getAttemptQuestions(studentSession, attempt1.getId());

            // Resume attempt returns same attempt and same assigned questions
            ExamAttempt attempt2 = examService.startOrResumeExam(studentSession, es.getId());
            List<ExamAttemptQuestion> run2 = examService.getAttemptQuestions(studentSession, attempt2.getId());

            boolean identical = (attempt1.getId() == attempt2.getId());
            if (run1.size() != run2.size()) {
                identical = false;
            } else {
                for (int i = 0; i < run1.size(); i++) {
                    if (run1.get(i).getQuestionId() != run2.get(i).getQuestionId()) {
                        identical = false;
                        break;
                    }
                }
            }
            assertTrue("Question selection is persistent, deterministic and stable across attempt resumption", identical);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Question selection exception: " + e.getMessage(), false);
        }
    }

    private static void testExamGradingAndResultManagement() {
        System.out.println("\n--- Testing Accurate Grading & Result Filtering ---");
        try {
            ExamService examService = new ExamService();
            ResultService resultService = new ResultService();
            SubjectService subjectService = new SubjectService();
            StudentService studentService = new StudentService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            // Register a dedicated student for grading tests
            String uniqueAadhar = "9999" + (System.currentTimeMillis() % 100000000L);
            while (uniqueAadhar.length() < 12) uniqueAadhar += "0";
            if (uniqueAadhar.length() > 12) uniqueAadhar = uniqueAadhar.substring(0, 12);

            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Grading Tester", "9876543299", "grade@example.com", uniqueAadhar, "01012001"
            );
            UserSession studentSession = new UserSession(reg.studentId, reg.username, "STUDENT", reg.studentId);

            // Schedule an exam for this student
            ExamSchedule es = examService.scheduleExam(adminSession, reg.studentId, sub.getId(), 15, 2, 50.0);

            // Start attempt
            ExamAttempt attempt = examService.startOrResumeExam(studentSession, es.getId());
            List<ExamAttemptQuestion> questions = examService.getAttemptQuestions(studentSession, attempt.getId());

            // Answer question 1 correctly, leave question 2 or answer incorrectly
            Question q1 = questions.get(0).getQuestion();
            examService.saveAnswer(studentSession, attempt.getId(), q1.getId(), q1.getCorrectAnswer());

            // Submit attempt
            ExamResult result = resultService.submitAttempt(studentSession, attempt.getId());
            assertTrue("Submitted attempt has total questions = 2", result.getTotalQuestions() == 2);
            assertTrue("Submitted attempt has correct answers >= 1", result.getCorrectAnswers() >= 1);
            assertTrue("Submitted attempt marks match correct answers", result.getMarks() == result.getCorrectAnswers());
            assertTrue("Submitted attempt percentage is calculated accurately", result.getPercentage() == (result.getCorrectAnswers() * 100.0 / result.getTotalQuestions()));

            // Search and filter results as admin
            List<ExamResult> passOnly = resultService.searchAndFilter(adminSession, "", "All Subjects", "Pass");
            assertTrue("Filter 'Pass' returns only passing results", passOnly.stream().allMatch(r -> "Pass".equalsIgnoreCase(r.getResult())));

            List<ExamResult> failOnly = resultService.searchAndFilter(adminSession, "", "All Subjects", "Fail");
            assertTrue("Filter 'Fail' returns only failing results", failOnly.stream().allMatch(r -> "Fail".equalsIgnoreCase(r.getResult())));

            // Single attempt deletion
            int targetId = result.getId();
            boolean deleted = resultService.deleteResultById(adminSession, targetId);
            assertTrue("Result attempt deleted successfully by admin", deleted);
            assertTrue("Deleted result cannot be found", resultService.getResultById(adminSession, targetId) == null);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Grading & result management exception: " + e.getMessage(), false);
        }
    }

    private static void testDashboardMetrics() {
        System.out.println("\n--- Testing Admin Dashboard Real-Time KPI Metrics ---");
        try {
            DashboardMetricsService metricsService = new DashboardMetricsService();
            DashboardMetricsService.Metrics m = metricsService.getMetrics(adminSession);

            assertTrue("Metrics total students > 0", m.totalStudents > 0);
            assertTrue("Metrics active students <= total students", m.activeStudents <= m.totalStudents);
            assertTrue("Metrics total questions > 0", m.totalQuestions > 0);
            assertTrue("Metrics pass rate is between 0 and 100", m.passRate >= 0.0 && m.passRate <= 100.0);
            assertTrue("Metrics average score is between 0 and 100", m.averageScorePercentage >= 0.0 && m.averageScorePercentage <= 100.0);

            System.out.println("  [METRICS SUMMARY]");
            System.out.println("    Students: " + m.totalStudents + " (Active: " + m.activeStudents + ")");
            System.out.println("    Subjects: " + m.totalSubjects + " (Active: " + m.activeSubjects + ")");
            System.out.println("    Questions: " + m.totalQuestions + " (Active: " + m.activeQuestions + ")");
            System.out.println("    Exam Schedules: " + m.totalSchedules);
            System.out.println("    Completed Attempts: " + m.totalResults);
            System.out.println("    Pass Rate: " + m.passRate + "%");
            System.out.println("    Average Score: " + m.averageScorePercentage + "%");

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Dashboard metrics exception: " + e.getMessage(), false);
        }
    }
}
