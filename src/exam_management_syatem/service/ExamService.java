package exam_management_syatem.service;

import exam_management_syatem.dao.ExamAttemptDAO;
import exam_management_syatem.dao.ExamAttemptQuestionDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExamService {
    private final ExamScheduleDAO scheduleDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;
    private final QuestionDAO questionDAO;
    private final ExamAttemptDAO attemptDAO;
    private final ExamAttemptQuestionDAO attemptQuestionDAO;

    public ExamService() {
        this.scheduleDAO = new ExamScheduleDAO();
        this.studentDAO = new StudentDAO();
        this.subjectDAO = new SubjectDAO();
        this.questionDAO = new QuestionDAO();
        this.attemptDAO = new ExamAttemptDAO();
        this.attemptQuestionDAO = new ExamAttemptQuestionDAO();
    }

    public ExamSchedule scheduleExam(UserSession session, String username, String subjectName, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Exam duration must be greater than 0 minutes.");
        }
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("Total questions must be greater than 0.");
        }
        if (passingPercentage < 0 || passingPercentage > 100) {
            throw new IllegalArgumentException("Passing percentage must be between 0 and 100.");
        }

        Student student = studentDAO.findByAadhar(username);
        int studentId = -1;
        if (student != null) {
            studentId = student.getId();
        } else {
            List<Student> students = studentDAO.listAll();
            for (Student s : students) {
                String cleanName = s.getName().trim().replaceAll("\\s+", "");
                if (cleanName.length() < 4) cleanName = (cleanName + "aaaa").substring(0, 4);
                else cleanName = cleanName.substring(0, 4);
                String last4 = s.getAadharNo().length() >= 4 ? s.getAadharNo().substring(s.getAadharNo().length() - 4) : "0000";
                String uName = (cleanName + last4).toLowerCase();
                if (uName.equalsIgnoreCase(username.trim())) {
                    studentId = s.getId();
                    break;
                }
            }
        }

        if (studentId == -1) {
            throw new IllegalArgumentException("Student with username '" + username + "' not found.");
        }

        Student s = studentDAO.findById(studentId);
        if (s == null || !s.isActive()) {
            throw new IllegalArgumentException("Student is not active or does not exist.");
        }

        Subject subject = subjectDAO.findByName(subjectName.trim());
        if (subject == null || !subject.isActive()) {
            throw new IllegalArgumentException("Subject '" + subjectName + "' is not active or does not exist.");
        }

        int availableQ = questionDAO.countActiveBySubjectId(subject.getId());
        if (availableQ < totalQuestions) {
            throw new IllegalArgumentException("Insufficient active questions available. Requested: " + totalQuestions + ", Available: " + availableQ);
        }

        ExamSchedule es = new ExamSchedule();
        es.setStudentId(studentId);
        es.setSubjectId(subject.getId());
        es.setDurationMinutes(durationMinutes);
        es.setTotalQuestions(totalQuestions);
        es.setPassingPercentage(passingPercentage);
        es.setStatus("SCHEDULED");

        int id = scheduleDAO.insert(es);
        es.setId(id);
        return es;
    }

    public ExamSchedule scheduleExam(UserSession session, int studentId, int subjectId, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Exam duration must be greater than 0 minutes.");
        }
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("Total questions must be greater than 0.");
        }
        if (passingPercentage < 0 || passingPercentage > 100) {
            throw new IllegalArgumentException("Passing percentage must be between 0 and 100.");
        }

        Student student = studentDAO.findById(studentId);
        if (student == null || !student.isActive()) {
            throw new IllegalArgumentException("Student does not exist or is inactive.");
        }

        Subject subject = subjectDAO.findById(subjectId);
        if (subject == null || !subject.isActive()) {
            throw new IllegalArgumentException("Subject does not exist or is inactive.");
        }

        int availableQ = questionDAO.countActiveBySubjectId(subjectId);
        if (availableQ < totalQuestions) {
            throw new IllegalArgumentException("Insufficient active questions available. Requested: " + totalQuestions + ", Available: " + availableQ);
        }

        ExamSchedule es = new ExamSchedule();
        es.setStudentId(studentId);
        es.setSubjectId(subjectId);
        es.setDurationMinutes(durationMinutes);
        es.setTotalQuestions(totalQuestions);
        es.setPassingPercentage(passingPercentage);
        es.setStatus("SCHEDULED");

        int id = scheduleDAO.insert(es);
        es.setId(id);
        return es;
    }

    public void updateExamSchedule(UserSession session, int scheduleId, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        ExamSchedule existing = scheduleDAO.findById(scheduleId);
        if (existing == null) {
            throw new IllegalArgumentException("Exam schedule not found.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Only exams with status 'SCHEDULED' can be edited. Current status: " + existing.getStatus());
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Exam duration must be greater than 0 minutes.");
        }
        if (totalQuestions <= 0) {
            throw new IllegalArgumentException("Total questions must be greater than 0.");
        }
        if (passingPercentage < 0 || passingPercentage > 100) {
            throw new IllegalArgumentException("Passing percentage must be between 0 and 100.");
        }

        int availableQ = questionDAO.countActiveBySubjectId(existing.getSubjectId());
        if (availableQ < totalQuestions) {
            throw new IllegalArgumentException("Insufficient active questions available. Requested: " + totalQuestions + ", Available: " + availableQ);
        }

        existing.setDurationMinutes(durationMinutes);
        existing.setTotalQuestions(totalQuestions);
        existing.setPassingPercentage(passingPercentage);
        scheduleDAO.update(existing);
    }

    public void cancelExam(UserSession session, int scheduleId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        ExamSchedule existing = scheduleDAO.findById(scheduleId);
        if (existing == null) {
            throw new IllegalArgumentException("Exam schedule not found.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Cannot cancel exam. Only SCHEDULED exams can be cancelled (current status: " + existing.getStatus() + ").");
        }
        scheduleDAO.updateStatus(scheduleId, "CANCELLED");
    }

    public void completeExamSchedule(UserSession session, int scheduleId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        scheduleDAO.updateStatus(scheduleId, "COMPLETED");
    }

    /**
     * Resilient, concurrent-safe attempt creation and resumption.
     * Strictly requires STUDENT role.
     * Enforces Anti-IDOR schedule ownership (schedule.student_id == session.student_id).
     * Enforces single-completion invariant via PostgreSQL pessimistic row locking (FOR UPDATE)
     * and partial unique index (idx_one_active_or_completed_attempt_per_schedule).
     */
    public ExamAttempt startOrResumeExam(UserSession session, int scheduleId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudent();
        int studentId = session.getStudentId();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Pessimistic row-lock on schedule to serialize concurrent attempts by same student
                ExamSchedule schedule = scheduleDAO.findByIdForUpdate(scheduleId, conn);
                if (schedule == null) {
                    throw new IllegalArgumentException("Exam schedule not found: " + scheduleId);
                }

                // 2. Anti-IDOR: verify this schedule belongs to the authenticated student
                if (schedule.getStudentId() != studentId) {
                    throw new SecurityException("Access Denied: Student cannot start an exam scheduled for another student.");
                }

                if ("CANCELLED".equalsIgnoreCase(schedule.getStatus())) {
                    throw new IllegalStateException("Exam schedule is cancelled.");
                }

                // 3. Check for any existing attempt regardless of status (ONE STUDENT + ONE SCHEDULE = ONE ATTEMPT)
                ExamAttempt existing = attemptDAO.findExistingAttempt(studentId, scheduleId, conn);
                if (existing != null) {
                    if ("COMPLETED".equalsIgnoreCase(existing.getStatus())) {
                        conn.rollback();
                        throw new IllegalStateException("Exam has already been completed.");
                    }
                    if ("EXPIRED".equalsIgnoreCase(existing.getStatus())) {
                        conn.rollback();
                        throw new IllegalStateException("Exam attempt has expired.");
                    }
                    if ("IN_PROGRESS".equalsIgnoreCase(existing.getStatus())) {
                        if (existing.isExpired()) {
                            attemptDAO.updateStatus(existing.getId(), "EXPIRED", conn);
                            conn.commit();
                            throw new IllegalStateException("Exam attempt has expired.");
                        }
                        conn.commit();
                        return existing;
                    }
                    conn.rollback();
                    throw new IllegalStateException("Exam attempt is in terminal state: " + existing.getStatus());
                }

                int subjectId = schedule.getSubjectId();
                int totalQuestions = schedule.getTotalQuestions();
                int durationMinutes = schedule.getDurationMinutes();
                double passingPercentage = schedule.getPassingPercentage();

                // 4. Prepare assigned question set from active bank
                List<Question> activeQuestions = questionDAO.findActiveBySubjectId(subjectId);
                if (activeQuestions.size() < totalQuestions) {
                    throw new IllegalStateException("Insufficient active questions in bank. Required: " + totalQuestions + ", Available: " + activeQuestions.size());
                }
                Collections.shuffle(activeQuestions, new Random());
                List<Question> selectedQuestions = activeQuestions.subList(0, totalQuestions);

                // 5. Create attempt and persist assigned question set in this transaction
                ExamAttempt attempt = new ExamAttempt();
                attempt.setExamScheduleId(scheduleId);
                attempt.setStudentId(studentId);
                attempt.setSubjectId(subjectId);
                attempt.setDurationMinutes(durationMinutes);
                attempt.setTotalQuestions(totalQuestions);
                attempt.setPassingPercentage(passingPercentage);
                attempt.setStatus("IN_PROGRESS");

                int attemptId = attemptDAO.insert(attempt, conn);
                attempt.setId(attemptId);

                List<ExamAttemptQuestion> attemptQuestions = new ArrayList<>();
                for (int i = 0; i < selectedQuestions.size(); i++) {
                    ExamAttemptQuestion eq = new ExamAttemptQuestion();
                    eq.setAttemptId(attemptId);
                    eq.setQuestionId(selectedQuestions.get(i).getId());
                    eq.setQuestionOrder(i + 1);
                    attemptQuestions.add(eq);
                }
                attemptQuestionDAO.insertBatch(attemptQuestions, conn);

                if ("SCHEDULED".equalsIgnoreCase(schedule.getStatus())) {
                    scheduleDAO.updateStatus(scheduleId, "IN_PROGRESS", conn);
                }

                conn.commit();
                return attempt;
            } catch (SQLException e) {
                conn.rollback();
                // 23505 is PostgreSQL's unique_violation code (caught by unique index on student_id, exam_schedule_id)
                if ("23505".equals(e.getSQLState())) {
                    ExamAttempt existingAttempt = attemptDAO.findExistingAttempt(studentId, scheduleId);
                    if (existingAttempt != null) {
                        if ("COMPLETED".equalsIgnoreCase(existingAttempt.getStatus())) {
                            throw new IllegalStateException("Exam has already been completed.");
                        }
                        if ("EXPIRED".equalsIgnoreCase(existingAttempt.getStatus()) || existingAttempt.isExpired()) {
                            throw new IllegalStateException("Exam attempt has expired.");
                        }
                        if ("IN_PROGRESS".equalsIgnoreCase(existingAttempt.getStatus())) {
                            return existingAttempt;
                        }
                    }
                    throw new IllegalStateException("An attempt already exists for this exam schedule.");
                }
                throw e;
            }
        }
    }

    public List<ExamAttemptQuestion> getAttemptQuestions(UserSession session, int attemptId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();

        ExamAttempt attempt = attemptDAO.findById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found: " + attemptId);
        }
        session.requireStudentAccess(attempt.getStudentId());

        return attemptQuestionDAO.listByAttemptId(attemptId);
    }

    public void saveAnswer(UserSession session, int attemptId, int questionId, String selectedOption) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudent();

        ExamAttempt attempt = attemptDAO.findById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found: " + attemptId);
        }

        // Anti-IDOR: verify caller owns this attempt
        if (attempt.getStudentId() != session.getStudentId()) {
            throw new SecurityException("Access Denied: Student cannot save answer to another student's exam attempt.");
        }

        if (!"IN_PROGRESS".equalsIgnoreCase(attempt.getStatus())) {
            throw new IllegalStateException("Cannot save answer: attempt is not in progress (status: " + attempt.getStatus() + ").");
        }

        if (attempt.isExpired()) {
            attemptDAO.updateStatus(attemptId, "EXPIRED");
            throw new IllegalStateException("Cannot save answer: exam time has expired.");
        }

        // Database-level conditional update provides defense-in-depth
        boolean updated = attemptQuestionDAO.saveAnswer(attemptId, questionId, selectedOption, session.getStudentId());
        if (!updated) {
            throw new IllegalStateException("Answer rejected: attempt not active or expired.");
        }
    }

    public void updateRemainingTime(UserSession session, int attemptId, int clientRemainingSeconds) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudent();

        ExamAttempt attempt = attemptDAO.findById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found: " + attemptId);
        }

        if (attempt.getStudentId() != session.getStudentId()) {
            throw new SecurityException("Access Denied: Cannot update timer for another student's attempt.");
        }

        if ("IN_PROGRESS".equalsIgnoreCase(attempt.getStatus()) && !attempt.isExpired()) {
            long authoritativeMax = attempt.getAuthoritativeRemainingSeconds();
            int safeRemaining = (int) Math.min(clientRemainingSeconds, authoritativeMax);
            attemptDAO.updateRemainingSeconds(attemptId, safeRemaining);
        }
    }

    public ExamSchedule getExamScheduleById(UserSession session, int id) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        ExamSchedule s = scheduleDAO.findById(id);
        if (s != null) {
            session.requireStudentAccess(s.getStudentId());
        }
        return s;
    }

    public List<ExamSchedule> getSchedulesByStudentId(UserSession session, int studentId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudentAccess(studentId);
        return scheduleDAO.listByStudentId(studentId);
    }

    public List<ExamSchedule> getAllSchedules(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return scheduleDAO.listAll();
    }

    /**
     * Read-only helper to inspect the latest persisted attempt for an exam schedule.
     * Used strictly for UI status display. Never creates, mutates, or resumes attempts.
     */
    public ExamAttempt getExistingAttempt(UserSession session, int scheduleId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudent();
        return attemptDAO.findExistingAttempt(session.getStudentId(), scheduleId);
    }

    /**
     * Read-only helper to fetch all persisted attempts for the authenticated student.
     * Used strictly for UI status mapping and telemetry. Never creates or mutates attempts.
     */
    public List<ExamAttempt> getAttemptsByStudent(UserSession session, int studentId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudentAccess(studentId);
        return attemptDAO.listByStudent(studentId);
    }
}
