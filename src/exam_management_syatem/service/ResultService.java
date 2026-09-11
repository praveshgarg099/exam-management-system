package exam_management_syatem.service;

import exam_management_syatem.dao.ExamAttemptDAO;
import exam_management_syatem.dao.ExamAttemptQuestionDAO;
import exam_management_syatem.dao.ExamResultDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.security.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ResultService {
    private final ExamResultDAO resultDAO;
    private final ExamScheduleDAO scheduleDAO;
    private final ExamAttemptDAO attemptDAO;
    private final ExamAttemptQuestionDAO attemptQuestionDAO;

    public ResultService() {
        this.resultDAO = new ExamResultDAO();
        this.scheduleDAO = new ExamScheduleDAO();
        this.attemptDAO = new ExamAttemptDAO();
        this.attemptQuestionDAO = new ExamAttemptQuestionDAO();
    }

    public ResultService(ExamResultDAO resultDAO, ExamScheduleDAO scheduleDAO) {
        this.resultDAO = resultDAO;
        this.scheduleDAO = scheduleDAO;
        this.attemptDAO = new ExamAttemptDAO();
        this.attemptQuestionDAO = new ExamAttemptQuestionDAO();
    }

    /**
     * Authoritative, atomic, and idempotent exam attempt submission.
     * Strictly requires STUDENT role and verifies attempt ownership (attempt.studentId == session.studentId).
     * Uses row-level lock (FOR UPDATE) to prevent race conditions or duplicate submissions.
     */
    public ExamResult submitAttempt(UserSession session, int attemptId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudent();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Row-level lock on attempt
                ExamAttempt attempt = attemptDAO.findByIdForUpdate(attemptId, conn);
                if (attempt == null) {
                    throw new IllegalArgumentException("Attempt not found: " + attemptId);
                }

                // Strict student ownership check: admin cannot submit on behalf of student
                if (attempt.getStudentId() != session.getStudentId()) {
                    throw new SecurityException("Access Denied: Student cannot submit another student's exam attempt.");
                }

                // Idempotency: If already completed or expired, return existing result without re-grading
                if ("COMPLETED".equalsIgnoreCase(attempt.getStatus())) {
                    ExamResult existing = resultDAO.findByAttemptId(attemptId, conn);
                    if (existing != null) {
                        conn.rollback();
                        return existing;
                    }
                }

                // Grade persisted answers in exam_attempt_questions
                int correctAnswers = attemptQuestionDAO.gradeAttempt(attemptId, conn);
                int totalQuestions = attempt.getTotalQuestions() > 0 ? attempt.getTotalQuestions() : 1;
                double marks = correctAnswers;
                double percentage = (correctAnswers * 100.0) / totalQuestions;
                String resultText = percentage >= attempt.getPassingPercentage() ? "Pass" : "Fail";

                ExamResult er = new ExamResult();
                er.setAttemptId(attemptId);
                er.setExamScheduleId(attempt.getExamScheduleId());
                er.setStudentId(attempt.getStudentId());
                er.setSubjectId(attempt.getSubjectId());
                er.setTotalQuestions(totalQuestions);
                er.setCorrectAnswers(correctAnswers);
                er.setMarks(marks);
                er.setPercentage(percentage);
                er.setResult(resultText);

                int resultId = resultDAO.insert(er, conn);
                er.setId(resultId);

                // Update attempt status to COMPLETED
                attemptDAO.updateStatus(attemptId, "COMPLETED", conn);

                conn.commit();
                return er;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean deleteResultById(UserSession session, int resultId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return resultDAO.deleteById(resultId);
    }

    public ExamResult getResultById(UserSession session, int resultId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        ExamResult er = resultDAO.findById(resultId);
        if (er != null) {
            session.requireStudentAccess(er.getStudentId());
        }
        return er;
    }

    public ExamResult getResultByAttemptId(UserSession session, int attemptId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        ExamResult er = resultDAO.findByAttemptId(attemptId);
        if (er != null) {
            session.requireStudentAccess(er.getStudentId());
        }
        return er;
    }

    public List<ExamResult> searchAndFilter(UserSession session, String studentKeyword, String subjectName, String passFail) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return resultDAO.searchAndFilter(studentKeyword, subjectName, passFail);
    }

    public List<ExamResult> getAllResults(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return resultDAO.listAll();
    }

    public List<ExamResult> getRecentResults(UserSession session, int limit) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return resultDAO.listRecent(limit);
    }

    public List<ExamResult> getResultsByStudent(UserSession session, int studentId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudentAccess(studentId);
        return resultDAO.listByStudentId(studentId);
    }
}
