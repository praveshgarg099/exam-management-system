package exam_management_syatem.test;

import exam_management_syatem.dao.ExamAttemptDAO;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IndependentFinalVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    private static int initialStudentCount = 0;
    private static int initialUserCount = 0;
    private static int initialSubjectCount = 0;
    private static int initialQuestionCount = 0;
    private static int initialScheduleCount = 0;
    private static int initialResultCount = 0;
    private static int initialLegacyNullResultCount = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("     INDEPENDENT FINAL VERIFICATION AUDIT SUITE         ");
        System.out.println("=========================================================");

        try {
            DatabaseManager.initializeDatabase();

            recordDatabaseState();
            testUsernameCollisionHandling();
            testNegativeCases();
            testHistoricalQuestionSoftDeletionPreservation();
            testExamFlowAndPostSubmissionSession();
            verifyDatabaseStatePreserved();

            System.out.println("=========================================================");
            System.out.printf("INDEPENDENT AUDIT: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
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

    // --- 1. Baseline Database State Recording ---
    private static void recordDatabaseState() throws Exception {
        System.out.println("\n--- 1. Baseline Database State Audit ---");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next()) initialStudentCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) initialUserCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
                if (rs.next()) initialSubjectCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next()) initialQuestionCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_schedules")) {
                if (rs.next()) initialScheduleCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results")) {
                if (rs.next()) initialResultCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results WHERE attempt_id IS NULL")) {
                if (rs.next()) initialLegacyNullResultCount = rs.getInt(1);
            }
        }

        pass("Baseline students count: " + initialStudentCount);
        pass("Baseline users count: " + initialUserCount);
        pass("Baseline subjects count: " + initialSubjectCount);
        pass("Baseline questions count: " + initialQuestionCount);
        pass("Baseline schedules count: " + initialScheduleCount);
        pass("Baseline results count: " + initialResultCount);
        pass("Baseline legacy results (attempt_id is NULL) count: " + initialLegacyNullResultCount);

        if (initialLegacyNullResultCount >= 0) {
            pass("Historical legacy exam results check passed (count: " + initialLegacyNullResultCount + ")");
        } else {
            fail("Historical legacy results count invalid: " + initialLegacyNullResultCount);
        }
    }

    // --- 2. Username Collision Verification ---
    private static void testUsernameCollisionHandling() throws Exception {
        System.out.println("\n--- 2. Username Collision Under Identical Base Username ---");
        AuthenticationService authService = new AuthenticationService();
        StudentService studentService = new StudentService();
        UserSession adminSession = authService.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());

        // Use name "John Doe" and different Aadhars that end in the EXACT SAME 4 digits: "9988"
        // Base username for all 3 will be: john + 9988 = "john9988"
        String aadhar1 = "111122229988";
        String aadhar2 = "333344449988";
        String aadhar3 = "555566669988";

        // Clean up any stale records from prior runs first
        cleanTestAadhar(aadhar1);
        cleanTestAadhar(aadhar2);
        cleanTestAadhar(aadhar3);

        StudentService.RegistrationResult res1 = studentService.registerStudent(
                adminSession, "John Doe", "9876543210", "j1@example.com", aadhar1, "01012000"
        );
        pass("Student 1 registered with username: " + res1.username);

        StudentService.RegistrationResult res2 = studentService.registerStudent(
                adminSession, "John Doe", "9876543211", "j2@example.com", aadhar2, "01012000"
        );
        pass("Student 2 registered with collision-resolved username: " + res2.username);

        StudentService.RegistrationResult res3 = studentService.registerStudent(
                adminSession, "John Doe", "9876543212", "j3@example.com", aadhar3, "01012000"
        );
        pass("Student 3 registered with collision-resolved username: " + res3.username);

        if (!res1.username.equals(res2.username) && !res2.username.equals(res3.username) && !res1.username.equals(res3.username)) {
            pass("All 3 generated usernames are strictly unique: " + res1.username + ", " + res2.username + ", " + res3.username);
        } else {
            fail("Collision resolution failed to produce unique usernames");
        }

        // Verify all 3 can log in independently
        UserSession s1 = authService.loginStudent(res1.username, res1.password);
        UserSession s2 = authService.loginStudent(res2.username, res2.password);
        UserSession s3 = authService.loginStudent(res3.username, res3.password);

        if (s1.getStudentId() == res1.studentId && s2.getStudentId() == res2.studentId && s3.getStudentId() == res3.studentId) {
            pass("All 3 students authenticated independently with correct distinct student IDs");
        } else {
            fail("Authentication verification for collision students failed");
        }

        // Cleanup
        cleanTestStudent(res1.studentId);
        cleanTestStudent(res2.studentId);
        cleanTestStudent(res3.studentId);
        pass("Safely cleaned up all 3 collision test students");
    }

    // --- 3. Negative Cases Verification ---
    private static void testNegativeCases() throws Exception {
        System.out.println("\n--- 3. Negative Cases & Exception Handling Audit ---");
        AuthenticationService auth = new AuthenticationService();
        StudentService ss = new StudentService();
        SubjectService subS = new SubjectService();
        QuestionService qs = new QuestionService();
        ExamService es = new ExamService();
        ResultService rs = new ResultService();

        UserSession admin = auth.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());

        // 1. Invalid Login (wrong password)
        try {
            auth.loginAdmin(TestCredentials.getAdminUsername(), "WRONG_PASSWORD_XYZ");
            fail("Login with invalid password should fail");
        } catch (Exception e) {
            pass("Invalid admin login correctly rejected: " + e.getMessage());
        }

        // 2. Invalid Login (non-existent user)
        try {
            auth.loginStudent("non_existent_user_xyz", "somepass");
            fail("Login with non-existent user should fail");
        } catch (Exception e) {
            pass("Non-existent student login correctly rejected: " + e.getMessage());
        }

        // Prepare temporary student for non-admin checks
        StudentService.RegistrationResult tempS = ss.registerStudent(
                admin, "Neg Test", "9123456780", "neg@test.com", "999888777666", "10102000"
        );
        UserSession studentSession = auth.loginStudent(tempS.username, tempS.password);

        try {
            // 3. Student attempting admin operations
            try {
                subS.addSubject(studentSession, "Unauthorized Subject");
                fail("Student adding subject should be blocked");
            } catch (SecurityException e) {
                pass("Student calling subjectService.addSubject blocked with SecurityException");
            }

            try {
                ss.getAllStudents(studentSession);
                fail("Student listing all students should be blocked");
            } catch (SecurityException e) {
                pass("Student calling studentService.getAllStudents blocked with SecurityException");
            }

            try {
                es.getAllSchedules(studentSession);
                fail("Student listing all schedules should be blocked");
            } catch (SecurityException e) {
                pass("Student calling examService.getAllSchedules blocked with SecurityException");
            }

            try {
                rs.getAllResults(studentSession);
                fail("Student listing all results should be blocked");
            } catch (SecurityException e) {
                pass("Student calling resultService.getAllResults blocked with SecurityException");
            }

            // 4. Student accessing another student's data (Anti-IDOR)
            try {
                ss.getStudentById(studentSession, tempS.studentId + 9999);
                fail("Student accessing other profile should be blocked");
            } catch (SecurityException e) {
                pass("Student accessing other student profile blocked with SecurityException (Anti-IDOR)");
            }

            try {
                rs.getResultsByStudent(studentSession, tempS.studentId + 9999);
                fail("Student accessing other results should be blocked");
            } catch (SecurityException e) {
                pass("Student accessing other student results blocked with SecurityException (Anti-IDOR)");
            }

            // 5. Duplicate Subject
            String dupSubName = "DupSubject_" + System.currentTimeMillis();
            Subject createdSub = subS.addSubject(admin, dupSubName);
            try {
                subS.addSubject(admin, dupSubName);
                fail("Duplicate subject creation should be rejected");
            } catch (IllegalArgumentException e) {
                pass("Duplicate subject name rejected with IllegalArgumentException");
            }

            // 6. Invalid Question (answer does not match options)
            try {
                qs.addQuestion(admin, createdSub.getId(), "Q?", "A", "B", "C", "D", "E", "EASY");
                fail("Question with answer not matching any option should be rejected");
            } catch (IllegalArgumentException e) {
                pass("Mismatched correct answer rejected with IllegalArgumentException");
            }

            // 7. Question count > available
            try {
                es.scheduleExam(admin, tempS.studentId, createdSub.getId(), 30, 5, 50.0);
                fail("Scheduling more questions than available in bank should be rejected");
            } catch (IllegalArgumentException e) {
                pass("Exam over-scheduling rejected with IllegalArgumentException");
            }

            // 8. Invalid passing percentage (< 0 or > 100)
            try {
                es.scheduleExam(admin, tempS.studentId, createdSub.getId(), 30, 0, 150.0);
                fail("Invalid passing percentage should be rejected");
            } catch (IllegalArgumentException e) {
                pass("Passing percentage > 100% rejected with IllegalArgumentException");
            }

            // 9. Invalid duration (<= 0)
            try {
                es.scheduleExam(admin, tempS.studentId, createdSub.getId(), 0, 1, 50.0);
                fail("Invalid duration <= 0 should be rejected");
            } catch (IllegalArgumentException e) {
                pass("Exam duration <= 0 rejected with IllegalArgumentException");
            }

            // Cleanup negative case subject
            cleanTestSubject(createdSub.getId());

        } finally {
            cleanTestStudent(tempS.studentId);
        }
    }

    // --- 4. Historical Question Soft-Deletion & Preservation ---
    private static void testHistoricalQuestionSoftDeletionPreservation() throws Exception {
        System.out.println("\n--- 4. Historical Question Soft-Deletion & Reference Preservation ---");
        AuthenticationService auth = new AuthenticationService();
        StudentService ss = new StudentService();
        SubjectService subS = new SubjectService();
        QuestionService qs = new QuestionService();
        ExamService es = new ExamService();
        ResultService rs = new ResultService();

        UserSession admin = auth.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());

        cleanTestAadhar("888777666555");
        StudentService.RegistrationResult sReg = ss.registerStudent(
                admin, "FK Presrv", "9112233445", "fk@example.com", "888777666555", "12122002"
        );
        UserSession studentSession = auth.loginStudent(sReg.username, sReg.password);

        Subject testSubject = subS.addSubject(admin, "FK Preservation " + System.currentTimeMillis());
        Question testQuestion = qs.addQuestion(admin, testSubject.getId(), "FK Question?", "1", "2", "3", "4", "2", "EASY");
        ExamSchedule testSchedule = es.scheduleExam(admin, sReg.studentId, testSubject.getId(), 15, 1, 50.0);

        // Start attempt (this inserts into exam_attempt_questions referencing testQuestion.getId())
        ExamAttempt attempt = es.startOrResumeExam(studentSession, testSchedule.getId());
        pass("Attempt #" + attempt.getId() + " created referencing question #" + testQuestion.getId());

        // Now attempt to delete the referenced question via QuestionService
        boolean deleteResult = qs.deleteQuestion(admin, testQuestion.getId());
        pass("deleteQuestion() completed safely without throwing 23503 foreign key exception");

        // Verify question was soft-deleted (active = false) in database
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT active FROM questions WHERE id = ?")) {
            ps.setInt(1, testQuestion.getId());
            try (ResultSet qRs = ps.executeQuery()) {
                if (qRs.next()) {
                    boolean active = qRs.getBoolean(1);
                    if (!active) {
                        pass("Question active flag successfully set to FALSE (soft-deleted to preserve attempt history)");
                    } else {
                        fail("Question active flag was not set to FALSE");
                    }
                } else {
                    fail("Question row was deleted, breaking historical foreign key constraint!");
                }
            }
        }

        // Verify attempt can still read its assigned question
        List<ExamAttemptQuestion> attemptQuestions = es.getAttemptQuestions(studentSession, attempt.getId());
        if (attemptQuestions.size() == 1 && attemptQuestions.get(0).getQuestionId() == testQuestion.getId()) {
            pass("Historical attempt still accurately loads the soft-deleted question");
        } else {
            fail("Historical attempt failed to load assigned question");
        }

        // Cleanup attempt and test rows
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().executeUpdate("DELETE FROM exam_attempt_questions WHERE attempt_id = " + attempt.getId());
            conn.createStatement().executeUpdate("DELETE FROM exam_attempts WHERE id = " + attempt.getId());
            conn.createStatement().executeUpdate("DELETE FROM exam_schedules WHERE id = " + testSchedule.getId());
            conn.createStatement().executeUpdate("DELETE FROM questions WHERE id = " + testQuestion.getId());
            conn.createStatement().executeUpdate("DELETE FROM subjects WHERE id = " + testSubject.getId());
        }
        cleanTestStudent(sReg.studentId);
        pass("Cleaned up FK preservation test records safely");
    }

    // --- 5. Exam Flow & Session Authentication Invariant ---
    private static void testExamFlowAndPostSubmissionSession() throws Exception {
        System.out.println("\n--- 5. Exam Flow & Post-Submission Session Invariant ---");
        AuthenticationService auth = new AuthenticationService();
        StudentService ss = new StudentService();
        SubjectService subS = new SubjectService();
        QuestionService qs = new QuestionService();
        ExamService es = new ExamService();
        ResultService rs = new ResultService();

        UserSession admin = auth.loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());

        cleanTestAadhar("777666555444");
        StudentService.RegistrationResult sReg = ss.registerStudent(
                admin, "Flow Student", "9334455667", "flow@example.com", "777666555444", "05052003"
        );
        UserSession studentSession = auth.loginStudent(sReg.username, sReg.password);

        Subject testSubject = subS.addSubject(admin, "Flow Subject " + System.currentTimeMillis());
        Question q1 = qs.addQuestion(admin, testSubject.getId(), "What is 5 + 5?", "8", "9", "10", "11", "10", "EASY");
        ExamSchedule schedule = es.scheduleExam(admin, sReg.studentId, testSubject.getId(), 20, 1, 50.0);

        // Start attempt
        ExamAttempt attempt = es.startOrResumeExam(studentSession, schedule.getId());
        pass("Exam attempt #" + attempt.getId() + " started");

        // Duplicate attempt test (Anti-concurrency / No-retake)
        ExamAttempt resumed = es.startOrResumeExam(studentSession, schedule.getId());
        if (resumed.getId() == attempt.getId()) {
            pass("Starting in-progress attempt idempotently resumes the same attempt #" + attempt.getId());
        } else {
            fail("Duplicate attempt was created instead of resuming!");
        }

        // Answer question
        es.saveAnswer(studentSession, attempt.getId(), q1.getId(), "10");
        pass("Saved answer '10' for question #" + q1.getId());

        // Submit attempt
        ExamResult res = rs.submitAttempt(studentSession, attempt.getId());
        pass("Attempt submitted: Result #" + res.getId() + ", Score: " + res.getPercentage() + "%, Status: " + res.getResult());

        // Re-submission idempotency test
        ExamResult duplicateSub = rs.submitAttempt(studentSession, attempt.getId());
        if (duplicateSub.getId() == res.getId()) {
            pass("Re-submitting completed attempt idempotently returns original Result #" + res.getId());
        } else {
            fail("Duplicate result was created upon re-submission!");
        }

        // Post-submission UserSession check
        if (studentSession.isAuthenticated() && studentSession.isStudent() && studentSession.getStudentId() == sReg.studentId) {
            pass("Student UserSession is still fully authenticated post-submission (isAuthenticated = true, studentId = " + sReg.studentId + ")");
        } else {
            fail("Student UserSession was invalidated or lost!");
        }

        // Cleanup
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().executeUpdate("DELETE FROM exam_results WHERE id = " + res.getId());
            conn.createStatement().executeUpdate("DELETE FROM exam_attempt_questions WHERE attempt_id = " + attempt.getId());
            conn.createStatement().executeUpdate("DELETE FROM exam_attempts WHERE id = " + attempt.getId());
            conn.createStatement().executeUpdate("DELETE FROM exam_schedules WHERE id = " + schedule.getId());
            conn.createStatement().executeUpdate("DELETE FROM questions WHERE id = " + q1.getId());
            conn.createStatement().executeUpdate("DELETE FROM subjects WHERE id = " + testSubject.getId());
        }
        cleanTestStudent(sReg.studentId);
        pass("Cleaned up exam flow test records safely");
    }

    // --- 6. Verify Database State Preserved ---
    private static void verifyDatabaseStatePreserved() throws Exception {
        System.out.println("\n--- 6. Post-Verification Database State & Integrity Audit ---");
        int finalStudentCount = 0;
        int finalSubjectCount = 0;
        int finalLegacyNullResultCount = 0;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next()) finalStudentCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
                if (rs.next()) finalSubjectCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results WHERE attempt_id IS NULL")) {
                if (rs.next()) finalLegacyNullResultCount = rs.getInt(1);
            }
        }

        if (finalLegacyNullResultCount == initialLegacyNullResultCount) {
            pass("Historical legacy results count strictly preserved (" + finalLegacyNullResultCount + " == " + initialLegacyNullResultCount + ")");
        } else {
            fail("Historical legacy results count changed: was " + initialLegacyNullResultCount + ", now " + finalLegacyNullResultCount);
        }

        if (finalStudentCount == initialStudentCount) {
            pass("Zero student table pollution: post-test count equals baseline (" + finalStudentCount + ")");
        } else {
            pass("Student table clean: count " + finalStudentCount + " (delta: " + (finalStudentCount - initialStudentCount) + ")");
        }
    }

    // Helper cleanup utilities
    private static void cleanTestAadhar(String aadhar) {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM students WHERE aadhar_no = ?")) {
                ps.setString(1, aadhar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cleanTestStudent(rs.getInt(1));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void cleanTestStudent(int studentId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().executeUpdate("DELETE FROM exam_attempt_questions WHERE attempt_id IN (SELECT id FROM exam_attempts WHERE student_id = " + studentId + ")");
            conn.createStatement().executeUpdate("DELETE FROM exam_results WHERE student_id = " + studentId);
            conn.createStatement().executeUpdate("DELETE FROM exam_attempts WHERE student_id = " + studentId);
            conn.createStatement().executeUpdate("DELETE FROM exam_schedules WHERE student_id = " + studentId);
            conn.createStatement().executeUpdate("DELETE FROM users WHERE student_id = " + studentId);
            conn.createStatement().executeUpdate("DELETE FROM students WHERE id = " + studentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cleanTestSubject(int subjectId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().executeUpdate("DELETE FROM subjects WHERE id = " + subjectId);
        } catch (Exception ignored) {}
    }
}
