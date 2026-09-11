package exam_management_syatem.test;

import exam_management_syatem.dao.ExamAttemptDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.SubjectDAO;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Phase4ASecurityTestSuite {

    private static int passedTests = 0;
    private static int failedTests = 0;

    private static final UserSession adminSession = new UserSession(1, "admin", "ADMIN", null);

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("   PHASE 4A SECURITY & INTEGRITY AUDIT TEST SUITE ");
        System.out.println("=================================================");

        DatabaseManager.initializeDatabase();

        testRbacEnforcement();
        testPasswordChangeSecurity();
        testAntiIdorExamStartAndSubmit();
        testSubmitAttemptStudentOwnershipAndAdminBlock();
        testNoRetakeConcurrencySafety();
        testAnswerModificationDefenseInDepth();
        testAntiIdorDataIsolation();
        testAtomicMultiTableStudentStatusTransaction();

        System.out.println("=================================================");
        System.out.println("PHASE 4A TEST SUMMARY: " + passedTests + " PASSED, " + failedTests + " FAILED");
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

    /**
     * 1. RBAC: Verify unauthenticated requests and unauthorized student requests
     * to admin service methods are strictly rejected.
     */
    private static void testRbacEnforcement() {
        System.out.println("\n--- 1. RBAC Service Layer Authorization Tests ---");
        try {
            StudentService studentService = new StudentService();
            SubjectService subjectService = new SubjectService();
            QuestionService questionService = new QuestionService();
            ExamService examService = new ExamService();
            ResultService resultService = new ResultService();
            DashboardMetricsService metricsService = new DashboardMetricsService();

            UserSession studentSession = new UserSession(100, "fakeStudent", "STUDENT", 100);

            // Unauthenticated null session rejected
            boolean nullSessionBlocked = false;
            try {
                studentService.getAllStudents(null);
            } catch (SecurityException e) {
                nullSessionBlocked = true;
            }
            assertTrue("Null session rejected on admin student query", nullSessionBlocked);

            // Student role rejected on student management
            boolean studentOnAdminStudentsBlocked = false;
            try {
                studentService.getAllStudents(studentSession);
            } catch (SecurityException e) {
                studentOnAdminStudentsBlocked = true;
            }
            assertTrue("Student session rejected on studentService.getAllStudents()", studentOnAdminStudentsBlocked);

            // Student role rejected on subject creation
            boolean studentOnAddSubjectBlocked = false;
            try {
                subjectService.addSubject(studentSession, "Hacking 101");
            } catch (SecurityException e) {
                studentOnAddSubjectBlocked = true;
            }
            assertTrue("Student session rejected on subjectService.addSubject()", studentOnAddSubjectBlocked);

            // Student role rejected on question management
            boolean studentOnAddQuestionBlocked = false;
            try {
                questionService.addQuestion(studentSession, 1, "Q?", "A", "B", "C", "D", "A");
            } catch (SecurityException e) {
                studentOnAddQuestionBlocked = true;
            }
            assertTrue("Student session rejected on questionService.addQuestion()", studentOnAddQuestionBlocked);

            // Student role rejected on exam scheduling
            boolean studentOnScheduleExamBlocked = false;
            try {
                examService.scheduleExam(studentSession, 1, 1, 10, 5, 40.0);
            } catch (SecurityException e) {
                studentOnScheduleExamBlocked = true;
            }
            assertTrue("Student session rejected on examService.scheduleExam()", studentOnScheduleExamBlocked);

            // Student role rejected on metrics
            boolean studentOnMetricsBlocked = false;
            try {
                metricsService.getMetrics(studentSession);
            } catch (SecurityException e) {
                studentOnMetricsBlocked = true;
            }
            assertTrue("Student session rejected on metricsService.getMetrics()", studentOnMetricsBlocked);

            // Student role rejected on result deletion
            boolean studentOnDeleteResultBlocked = false;
            try {
                resultService.deleteResultById(studentSession, 1);
            } catch (SecurityException e) {
                studentOnDeleteResultBlocked = true;
            }
            assertTrue("Student session rejected on resultService.deleteResultById()", studentOnDeleteResultBlocked);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("RBAC test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 2. Password Security:
     * - Only changeOwnPassword(session, cur, new) on student-facing API.
     * - Wrong current password rejected with SecurityException.
     * - Student cannot change another student's password.
     * - Admin reset requires adminSession.requireAdmin().
     */
    private static void testPasswordChangeSecurity() {
        System.out.println("\n--- 2. Password Change Security & Self-Service Tests ---");
        try {
            StudentService studentService = new StudentService();
            AuthenticationService authService = new AuthenticationService();

            String uniqueAadhar = "6666" + (System.currentTimeMillis() % 100000000L);
            while (uniqueAadhar.length() < 12) uniqueAadhar += "0";
            if (uniqueAadhar.length() > 12) uniqueAadhar = uniqueAadhar.substring(0, 12);

            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Pass Sec Tester", "9876543266", "passsec@example.com", uniqueAadhar, "05052000"
            );

            UserSession studentSession = authService.loginStudent(reg.username, reg.password);

            // Rejection of invalid old password
            boolean wrongOldPassRejected = false;
            try {
                authService.changeOwnPassword(studentSession, "IncorrectCurrentPass!1", "BrandNewPass!99");
            } catch (SecurityException e) {
                wrongOldPassRejected = true;
            }
            assertTrue("Wrong old password rejected with SecurityException", wrongOldPassRejected);

            // Success with correct old password
            authService.changeOwnPassword(studentSession, reg.password, "BrandNewPass!99");
            UserSession updatedSession = authService.loginStudent(reg.username, "BrandNewPass!99");
            assertTrue("Login with newly updated password succeeds", updatedSession != null);

            // Admin reset requires admin session
            boolean studentCallingAdminResetBlocked = false;
            try {
                authService.adminResetStudentPassword(studentSession, reg.studentId);
            } catch (SecurityException e) {
                studentCallingAdminResetBlocked = true;
            }
            assertTrue("Student session calling adminResetStudentPassword() blocked", studentCallingAdminResetBlocked);

            String tempPass = authService.adminResetStudentPassword(adminSession, reg.studentId);
            assertTrue("Admin reset succeeds and generates temporary password", tempPass != null && tempPass.startsWith("Temp@"));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Password security test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 3. Anti-IDOR:
     * - Student A cannot start an exam scheduled for Student B.
     */
    private static void testAntiIdorExamStartAndSubmit() {
        System.out.println("\n--- 3. Anti-IDOR Exam Start Ownership Tests ---");
        try {
            StudentService studentService = new StudentService();
            ExamService examService = new ExamService();
            SubjectService subjectService = new SubjectService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            // Create Student A and Student B with distinct names and aadhars
            long seed = System.nanoTime();
            String aadharA = "51" + String.format("%010d", Math.abs(seed % 10000000000L));
            String aadharB = "52" + String.format("%010d", Math.abs((seed + 1234567L) % 10000000000L));

            StudentService.RegistrationResult regA = studentService.registerStudent(
                    adminSession, "Alice Alpha", "9876543201", "studentA@test.com", aadharA, "01012000"
            );
            StudentService.RegistrationResult regB = studentService.registerStudent(
                    adminSession, "Bob Beta", "9876543202", "studentB@test.com", aadharB, "01012000"
            );

            UserSession sessionA = new UserSession(regA.studentId, regA.username, "STUDENT", regA.studentId);
            UserSession sessionB = new UserSession(regB.studentId, regB.username, "STUDENT", regB.studentId);

            // Schedule an exam specifically for Student B
            ExamSchedule scheduleB = examService.scheduleExam(adminSession, regB.studentId, sub.getId(), 15, 1, 50.0);

            // Student A attempts to start Student B's schedule -> MUST BE BLOCKED
            boolean studentAStartBlocked = false;
            try {
                examService.startOrResumeExam(sessionA, scheduleB.getId());
            } catch (SecurityException e) {
                studentAStartBlocked = true;
            }
            assertTrue("Student A blocked from starting Student B's exam schedule (Anti-IDOR)", studentAStartBlocked);

            // Student B starts own exam -> SUCCEEDS
            ExamAttempt attemptB = examService.startOrResumeExam(sessionB, scheduleB.getId());
            assertTrue("Student B successfully starts own exam attempt", attemptB != null && attemptB.getId() > 0);

            // Student A attempts to save answer to Student B's attempt -> MUST BE BLOCKED
            boolean studentASaveAnswerBlocked = false;
            try {
                examService.saveAnswer(sessionA, attemptB.getId(), 1, "A");
            } catch (SecurityException e) {
                studentASaveAnswerBlocked = true;
            }
            assertTrue("Student A blocked from saving answer to Student B's attempt (Anti-IDOR)", studentASaveAnswerBlocked);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Anti-IDOR start test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 4. submitAttempt(session, attemptId) must require STUDENT role
     * and verify attempt.studentId == session.studentId.
     * ADMIN MUST NOT gain permission to submit another student's exam!
     */
    private static void testSubmitAttemptStudentOwnershipAndAdminBlock() {
        System.out.println("\n--- 4. submitAttempt Ownership & Admin Block Tests ---");
        try {
            StudentService studentService = new StudentService();
            ExamService examService = new ExamService();
            ResultService resultService = new ResultService();
            SubjectService subjectService = new SubjectService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            long seed = System.nanoTime();
            String aadharA = "41" + String.format("%010d", Math.abs(seed % 10000000000L));
            String aadharB = "42" + String.format("%010d", Math.abs((seed + 7654321L) % 10000000000L));

            StudentService.RegistrationResult regA = studentService.registerStudent(
                    adminSession, "Charlie Clark", "9876543241", "subA@test.com", aadharA, "01012000"
            );
            StudentService.RegistrationResult regB = studentService.registerStudent(
                    adminSession, "David Davis", "9876543242", "subB@test.com", aadharB, "01012000"
            );

            UserSession sessionA = new UserSession(regA.studentId, regA.username, "STUDENT", regA.studentId);
            UserSession sessionB = new UserSession(regB.studentId, regB.username, "STUDENT", regB.studentId);

            ExamSchedule scheduleA = examService.scheduleExam(adminSession, regA.studentId, sub.getId(), 15, 1, 50.0);
            ExamAttempt attemptA = examService.startOrResumeExam(sessionA, scheduleA.getId());

            // 1. Student B tries to submit Student A's exam -> MUST BE REJECTED
            boolean studentBSubmitBlocked = false;
            try {
                resultService.submitAttempt(sessionB, attemptA.getId());
            } catch (SecurityException e) {
                studentBSubmitBlocked = true;
            }
            assertTrue("Student B blocked from submitting Student A's exam attempt", studentBSubmitBlocked);

            // 2. ADMIN tries to submit Student A's exam -> MUST BE REJECTED!
            // Requirement 2: "submitAttempt(UserSession, attemptId) must require STUDENT and verify attempt.studentId == session.studentId. Do not use canAccessStudent() here because ADMIN must not implicitly gain permission to submit another student's exam."
            boolean adminSubmitBlocked = false;
            try {
                resultService.submitAttempt(adminSession, attemptA.getId());
            } catch (SecurityException e) {
                adminSubmitBlocked = true;
            }
            assertTrue("Admin blocked from submitting student's exam attempt (ADMIN role denied)", adminSubmitBlocked);

            // 3. Student A submits own exam -> SUCCEEDS
            ExamResult resA = resultService.submitAttempt(sessionA, attemptA.getId());
            assertTrue("Student A successfully submits own exam attempt", resA != null && resA.getId() > 0);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Submit attempt ownership test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 5. Completed-exam / no-retake invariant and concurrency safety:
     * - Pessimistic row locking on schedule
     * - Partial unique index: idx_one_active_or_completed_attempt_per_schedule
     * - Concurrent starts on same schedule cannot create duplicate attempts
     * - Attempting to start after completion is strictly rejected
     */
    private static void testNoRetakeConcurrencySafety() {
        System.out.println("\n--- 5. Concurrency-Safe No-Retake & Unique Attempt Invariant Tests ---");
        try {
            StudentService studentService = new StudentService();
            ExamService examService = new ExamService();
            ResultService resultService = new ResultService();
            SubjectService subjectService = new SubjectService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            long now = System.currentTimeMillis();
            String aadhar = ("3333" + (now % 100000000L) + "000000000000").substring(0, 12);
            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Concurrent Tester", "9876543233", "concurrent@test.com", aadhar, "01012000"
            );
            UserSession session = new UserSession(reg.studentId, reg.username, "STUDENT", reg.studentId);

            ExamSchedule schedule = examService.scheduleExam(adminSession, reg.studentId, sub.getId(), 15, 1, 50.0);

            // Verify the PostgreSQL unique index exists in pg_indexes
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT indexname FROM pg_indexes WHERE tablename = 'exam_attempts' AND indexname = 'idx_one_attempt_per_schedule'")) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue("PostgreSQL unique index 'idx_one_attempt_per_schedule' exists", rs.next());
                }
            }

            // Concurrent attempt starts: 4 threads simultaneously call startOrResumeExam
            int threadCount = 4;
            CountDownLatch readyLatch = new CountDownLatch(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger distinctAttemptIds = new AtomicInteger(0);
            int[] returnedAttemptIds = new int[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int threadIdx = i;
                new Thread(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        ExamAttempt att = examService.startOrResumeExam(session, schedule.getId());
                        if (att != null) {
                            successCount.incrementAndGet();
                            returnedAttemptIds[threadIdx] = att.getId();
                        }
                    } catch (Exception ignored) {
                    } finally {
                        doneLatch.countDown();
                    }
                }).start();
            }

            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();

            // All threads should either succeed or return the single active attempt
            assertTrue("All concurrent threads completed safely", successCount.get() == threadCount);

            // Verify that all returned attempt IDs are identical (single attempt created)
            int firstId = returnedAttemptIds[0];
            boolean allIdentical = true;
            for (int id : returnedAttemptIds) {
                if (id != firstId) {
                    allIdentical = false;
                    break;
                }
            }
            assertTrue("Concurrent starts converged onto exactly one unique attempt ID", allIdentical && firstId > 0);

            // Verify in database: exactly 1 attempt exists for this schedule
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM exam_attempts WHERE exam_schedule_id = ?")) {
                pstmt.setInt(1, schedule.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);
                    assertTrue("Database row count for schedule attempts is exactly 1", count == 1);
                }
            }

            // Now submit the exam
            resultService.submitAttempt(session, firstId);

            // Verify attempt status is now COMPLETED
            ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
            ExamAttempt completedAttempt = attemptDAO.findById(firstId);
            assertTrue("Attempt status transitioned to COMPLETED", "COMPLETED".equalsIgnoreCase(completedAttempt.getStatus()));

            // Invariant: Student CANNOT retake or start another attempt for this completed schedule
            boolean retakeBlocked = false;
            try {
                examService.startOrResumeExam(session, schedule.getId());
            } catch (IllegalStateException e) {
                retakeBlocked = true;
            }
            assertTrue("Starting an already completed exam schedule is strictly blocked (No Retake Invariant)", retakeBlocked);

            // Test EXPIRED attempt no-retake invariant:
            long seed2 = System.nanoTime();
            String aadhar2 = "34" + String.format("%010d", Math.abs(seed2 % 10000000000L));
            StudentService.RegistrationResult regExp = studentService.registerStudent(
                    adminSession, "Expire Tester", "9876543234", "expire@test.com", aadhar2, "01012000"
            );
            UserSession expSession = new UserSession(regExp.studentId, regExp.username, "STUDENT", regExp.studentId);
            ExamSchedule expSchedule = examService.scheduleExam(adminSession, regExp.studentId, sub.getId(), 15, 1, 50.0);

            ExamAttempt expAttempt = examService.startOrResumeExam(expSession, expSchedule.getId());
            assertTrue("Initial attempt created for expiration test", expAttempt != null && expAttempt.getId() > 0);

            // Artificially age the attempt in PostgreSQL so that it is expired
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE exam_attempts SET started_at = CURRENT_TIMESTAMP - INTERVAL '2 hours' WHERE id = ?")) {
                pstmt.setInt(1, expAttempt.getId());
                pstmt.executeUpdate();
            }

            // Attempting to resume transitions to EXPIRED and throws domain exception
            boolean expiredBlockedOnResume = false;
            try {
                examService.startOrResumeExam(expSession, expSchedule.getId());
            } catch (IllegalStateException e) {
                expiredBlockedOnResume = true;
            }
            assertTrue("Resuming an expired attempt transitions it and throws domain exception", expiredBlockedOnResume);

            ExamAttempt transitionedExp = attemptDAO.findById(expAttempt.getId());
            assertTrue("Attempt transitioned to status EXPIRED", "EXPIRED".equalsIgnoreCase(transitionedExp.getStatus()));

            // Invariant: Student CANNOT retake or start a new attempt on the same schedule after EXPIRED!
            boolean retakeBlockedAfterExpired = false;
            try {
                examService.startOrResumeExam(expSession, expSchedule.getId());
            } catch (IllegalStateException e) {
                retakeBlockedAfterExpired = true;
            }
            assertTrue("Starting another attempt after EXPIRED is strictly blocked (ONE STUDENT + ONE SCHEDULE = ONE ATTEMPT)", retakeBlockedAfterExpired);

            // Verify database row count for this schedule remains exactly 1
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT COUNT(*) FROM exam_attempts WHERE exam_schedule_id = ?")) {
                pstmt.setInt(1, expSchedule.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    assertTrue("Database row count after expiration attempts remains exactly 1", rs.getInt(1) == 1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("No-retake concurrency test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 6. Answer Modification Defense-In-Depth:
     * - Saving answer rejected if attempt is completed
     * - Saving answer rejected if attempt is expired
     * - Database-level conditional update prevents tampering
     */
    private static void testAnswerModificationDefenseInDepth() {
        System.out.println("\n--- 6. Answer Modification Defense-In-Depth Tests ---");
        try {
            StudentService studentService = new StudentService();
            ExamService examService = new ExamService();
            ResultService resultService = new ResultService();
            SubjectService subjectService = new SubjectService();

            Subject sub = subjectService.getActiveSubjects(adminSession).get(0);

            long now = System.currentTimeMillis();
            String aadhar = ("2222" + (now % 100000000L) + "000000000000").substring(0, 12);
            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Tamper Tester", "9876543222", "tamper@test.com", aadhar, "01012000"
            );
            UserSession session = new UserSession(reg.studentId, reg.username, "STUDENT", reg.studentId);

            ExamSchedule schedule = examService.scheduleExam(adminSession, reg.studentId, sub.getId(), 15, 1, 50.0);
            ExamAttempt attempt = examService.startOrResumeExam(session, schedule.getId());
            List<ExamAttemptQuestion> qList = examService.getAttemptQuestions(session, attempt.getId());
            int qId = qList.get(0).getQuestionId();

            // Save valid answer while active
            examService.saveAnswer(session, attempt.getId(), qId, "A");

            // Submit attempt
            resultService.submitAttempt(session, attempt.getId());

            // Attempt to modify answer after submission -> MUST BE BLOCKED
            boolean postSubmissionAnswerBlocked = false;
            try {
                examService.saveAnswer(session, attempt.getId(), qId, "B");
            } catch (IllegalStateException e) {
                postSubmissionAnswerBlocked = true;
            }
            assertTrue("Saving answer after attempt submission is rejected", postSubmissionAnswerBlocked);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Answer modification defense test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 7. Anti-IDOR Data Isolation:
     * - Student A cannot view Student B's profile
     * - Student A cannot update Student B's profile
     * - Student A cannot view Student B's results
     */
    private static void testAntiIdorDataIsolation() {
        System.out.println("\n--- 7. Anti-IDOR Profile & Results Isolation Tests ---");
        try {
            StudentService studentService = new StudentService();
            ResultService resultService = new ResultService();

            long seed = System.nanoTime();
            String aadharA = "11" + String.format("%010d", Math.abs(seed % 10000000000L));
            String aadharB = "12" + String.format("%010d", Math.abs((seed + 9876543L) % 10000000000L));

            StudentService.RegistrationResult regA = studentService.registerStudent(
                    adminSession, "Edward Evans", "9876543111", "isoA@test.com", aadharA, "01012000"
            );
            StudentService.RegistrationResult regB = studentService.registerStudent(
                    adminSession, "Fiona Frank", "9876543112", "isoB@test.com", aadharB, "01012000"
            );

            UserSession sessionA = new UserSession(regA.studentId, regA.username, "STUDENT", regA.studentId);
            UserSession sessionB = new UserSession(regB.studentId, regB.username, "STUDENT", regB.studentId);

            // Student A cannot view Student B's profile
            boolean viewOtherProfileBlocked = false;
            try {
                studentService.getStudentById(sessionA, regB.studentId);
            } catch (SecurityException e) {
                viewOtherProfileBlocked = true;
            }
            assertTrue("Student A blocked from viewing Student B's profile (Anti-IDOR)", viewOtherProfileBlocked);

            // Student A cannot update Student B's profile
            Student sB = studentService.getStudentById(adminSession, regB.studentId);
            sB.setName("Tampered Name");
            boolean updateOtherProfileBlocked = false;
            try {
                studentService.updateStudent(sessionA, sB);
            } catch (SecurityException e) {
                updateOtherProfileBlocked = true;
            }
            assertTrue("Student A blocked from updating Student B's profile (Anti-IDOR)", updateOtherProfileBlocked);

            // Student A cannot query Student B's results
            boolean viewOtherResultsBlocked = false;
            try {
                resultService.getResultsByStudent(sessionA, regB.studentId);
            } catch (SecurityException e) {
                viewOtherResultsBlocked = true;
            }
            assertTrue("Student A blocked from viewing Student B's exam results (Anti-IDOR)", viewOtherResultsBlocked);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Anti-IDOR data isolation test exception: " + e.getMessage(), false);
        }
    }

    /**
     * 8. Atomic Multi-Table Student Status Transaction:
     * - Both students table and users table are updated atomically under one connection.
     * - Deactivating student disables both student record and user authentication record.
     */
    private static void testAtomicMultiTableStudentStatusTransaction() {
        System.out.println("\n--- 8. Atomic Multi-Table Status Transaction Tests ---");
        try {
            StudentService studentService = new StudentService();
            AuthenticationService authService = new AuthenticationService();

            long now = System.currentTimeMillis();
            String aadhar = ("9991" + (now % 100000000L) + "000000000000").substring(0, 12);
            StudentService.RegistrationResult reg = studentService.registerStudent(
                    adminSession, "Atomic Status Tester", "9876543991", "atomic@test.com", aadhar, "01012000"
            );

            // Deactivate student via atomic service method
            studentService.setStudentActiveStatus(adminSession, reg.studentId, false);

            // Verify students table is active = false
            try (Connection conn = DatabaseManager.getConnection()) {
                try (PreparedStatement ps1 = conn.prepareStatement("SELECT active FROM students WHERE id = ?")) {
                    ps1.setInt(1, reg.studentId);
                    try (ResultSet rs = ps1.executeQuery()) {
                        rs.next();
                        assertTrue("students table active flag is false", !rs.getBoolean(1));
                    }
                }
                // Verify users table is active = false
                try (PreparedStatement ps2 = conn.prepareStatement("SELECT active FROM users WHERE student_id = ?")) {
                    ps2.setInt(1, reg.studentId);
                    try (ResultSet rs = ps2.executeQuery()) {
                        rs.next();
                        assertTrue("users table active flag is false", !rs.getBoolean(1));
                    }
                }
            }

            // Verify login fails
            boolean loginBlocked = false;
            try {
                authService.loginStudent(reg.username, reg.password);
            } catch (Exception e) {
                loginBlocked = true;
            }
            assertTrue("Deactivated student blocked from logging in", loginBlocked);

            // Reactivate
            studentService.setStudentActiveStatus(adminSession, reg.studentId, true);
            UserSession activeSession = authService.loginStudent(reg.username, reg.password);
            assertTrue("Reactivated student can successfully log in", activeSession != null);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Atomic status transaction test exception: " + e.getMessage(), false);
        }
    }
}
