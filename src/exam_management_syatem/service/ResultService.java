package exam_management_syatem.service;

import exam_management_syatem.dao.ExamResultDAO;
import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;

import java.sql.SQLException;
import java.util.List;

public class ResultService {
    private final ExamResultDAO resultDAO;
    private final ExamScheduleDAO scheduleDAO;

    public ResultService() {
        this.resultDAO = new ExamResultDAO();
        this.scheduleDAO = new ExamScheduleDAO();
    }

    public ResultService(ExamResultDAO resultDAO, ExamScheduleDAO scheduleDAO) {
        this.resultDAO = resultDAO;
        this.scheduleDAO = scheduleDAO;
    }

    public ExamResult submitExamResult(Integer scheduleId, int studentId, int subjectId, int totalQuestions, int correctAnswers, double passingPercentage) throws Exception {
        if (totalQuestions <= 0) {
            totalQuestions = 1;
        }

        // Calculate score accurately
        double marks = correctAnswers;
        double percentage = (correctAnswers * 100.0) / totalQuestions;
        String passOrFail = percentage >= passingPercentage ? "Pass" : "Fail";

        ExamResult er = new ExamResult();
        er.setExamScheduleId(scheduleId);
        er.setStudentId(studentId);
        er.setSubjectId(subjectId);
        er.setTotalQuestions(totalQuestions);
        er.setCorrectAnswers(correctAnswers);
        er.setMarks(marks);
        er.setPercentage(percentage);
        er.setResult(passOrFail);

        int resultId = resultDAO.insert(er);
        er.setId(resultId);

        if (scheduleId != null) {
            scheduleDAO.updateStatus(scheduleId, "COMPLETED");
        }

        return er;
    }

    public boolean deleteResultById(int resultId) throws SQLException {
        return resultDAO.deleteById(resultId);
    }

    public ExamResult getResultById(int resultId) throws SQLException {
        return resultDAO.findById(resultId);
    }

    public List<ExamResult> searchAndFilter(String studentKeyword, String subjectName, String passFail) throws SQLException {
        return resultDAO.searchAndFilter(studentKeyword, subjectName, passFail);
    }

    public List<ExamResult> getAllResults() throws SQLException {
        return resultDAO.listAll();
    }

    public List<ExamResult> getResultsByStudent(int studentId) throws SQLException {
        return resultDAO.listByStudentId(studentId);
    }
}

