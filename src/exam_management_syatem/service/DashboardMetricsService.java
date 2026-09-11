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

    public Metrics getMetrics() throws SQLException {
        Metrics m = new Metrics();

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
