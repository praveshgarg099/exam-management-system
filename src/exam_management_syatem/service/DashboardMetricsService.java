package exam_management_syatem.service;

import exam_management_syatem.dao.ExamResultDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;

import java.sql.SQLException;
import java.util.List;

public class DashboardMetricsService {
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;
    private final QuestionDAO questionDAO;
    private final ExamScheduleDAO scheduleDAO;
    private final ExamResultDAO resultDAO;

    public DashboardMetricsService() {
        this.studentDAO = new StudentDAO();
        this.subjectDAO = new SubjectDAO();
        this.questionDAO = new QuestionDAO();
        this.scheduleDAO = new ExamScheduleDAO();
        this.resultDAO = new ExamResultDAO();
    }

    public static class Metrics {
        public int totalStudents;
        public int activeStudents;
        public int totalSubjects;
        public int activeSubjects;
        public int totalQuestions;
        public int activeQuestions;
        public int totalSchedules;
        public int totalResults;
        public int passedCount;
        public int failedCount;
        public double passRate;
        public double averageScorePercentage;
    }

    public Metrics getMetrics(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        Metrics m = new Metrics();

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM students) AS total_students, "
                + "(SELECT COUNT(*) FROM students WHERE active = true) AS active_students, "
                + "(SELECT COUNT(*) FROM subjects) AS total_subjects, "
                + "(SELECT COUNT(*) FROM subjects WHERE active = true) AS active_subjects, "
                + "(SELECT COUNT(*) FROM questions) AS total_questions, "
                + "(SELECT COUNT(*) FROM questions WHERE active = true) AS active_questions, "
                + "(SELECT COUNT(*) FROM exam_schedules) AS total_schedules, "
                + "(SELECT COUNT(*) FROM exam_results) AS total_results, "
                + "(SELECT COUNT(*) FROM exam_results WHERE LOWER(result) = 'pass') AS passed_count, "
                + "(SELECT COUNT(*) FROM exam_results WHERE LOWER(result) = 'fail') AS failed_count, "
                + "COALESCE((SELECT AVG(percentage) FROM exam_results), 0.0) AS avg_percentage";

        try (java.sql.Connection conn = exam_management_syatem.db.DatabaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                m.totalStudents = rs.getInt("total_students");
                m.activeStudents = rs.getInt("active_students");
                m.totalSubjects = rs.getInt("total_subjects");
                m.activeSubjects = rs.getInt("active_subjects");
                m.totalQuestions = rs.getInt("total_questions");
                m.activeQuestions = rs.getInt("active_questions");
                m.totalSchedules = rs.getInt("total_schedules");
                m.totalResults = rs.getInt("total_results");
                m.passedCount = rs.getInt("passed_count");
                m.failedCount = rs.getInt("failed_count");

                if (m.totalResults > 0) {
                    m.passRate = Math.round((m.passedCount * 100.0 / m.totalResults) * 100.0) / 100.0;
                    m.averageScorePercentage = Math.round(rs.getDouble("avg_percentage") * 100.0) / 100.0;
                } else {
                    m.passRate = 0.0;
                    m.averageScorePercentage = 0.0;
                }
                return m;
            }
        } catch (Exception ex) {
            System.err.println("[DashboardMetricsService] Note: Fast aggregation fallback: " + ex.getMessage());
        }

        // Fallback to individual DAOs if aggregation query fails
        List<Student> students = studentDAO.listAll();
        m.totalStudents = students.size();
        m.activeStudents = (int) students.stream().filter(Student::isActive).count();

        List<Subject> subjects = subjectDAO.listAll();
        m.totalSubjects = subjects.size();
        m.activeSubjects = (int) subjects.stream().filter(Subject::isActive).count();

        int totalQ = 0;
        int activeQ = 0;
        for (Subject sub : subjects) {
            totalQ += questionDAO.countBySubjectId(sub.getId());
            activeQ += questionDAO.countActiveBySubjectId(sub.getId());
        }
        m.totalQuestions = totalQ;
        m.activeQuestions = activeQ;

        List<ExamSchedule> schedules = scheduleDAO.listAll();
        m.totalSchedules = schedules.size();

        List<ExamResult> results = resultDAO.listAll();
        m.totalResults = results.size();
        m.passedCount = resultDAO.countByResult("Pass");
        m.failedCount = resultDAO.countByResult("Fail");

        if (m.totalResults > 0) {
            m.passRate = Math.round((m.passedCount * 100.0 / m.totalResults) * 100.0) / 100.0;
            m.averageScorePercentage = Math.round(resultDAO.getAveragePercentage() * 100.0) / 100.0;
        } else {
            m.passRate = 0.0;
            m.averageScorePercentage = 0.0;
        }

        return m;
    }
}
