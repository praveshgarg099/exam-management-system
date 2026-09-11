package exam_management_syatem.service;

import exam_management_syatem.dao.ExamScheduleDAO;
import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamService {
    private final ExamScheduleDAO scheduleDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;
    private final QuestionDAO questionDAO;

    public ExamService() {
        this.scheduleDAO = new ExamScheduleDAO();
        this.studentDAO = new StudentDAO();
        this.subjectDAO = new SubjectDAO();
        this.questionDAO = new QuestionDAO();
    }

    public ExamSchedule scheduleExam(String username, String subjectName, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
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

        // Resolve student & subject IDs
        Student student = studentDAO.findByAadhar(username); // Fallback lookup if passed aadhar or username
        int studentId = -1;
        if (student != null) {
            studentId = student.getId();
        } else {
            // Find by username matching from users or studentDAO
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

        // Verify student & subject active
        Student s = studentDAO.findById(studentId);
        if (s == null || !s.isActive()) {
            throw new IllegalArgumentException("Student is not active or does not exist.");
        }

        Subject subject = subjectDAO.findByName(subjectName.trim());
        if (subject == null || !subject.isActive()) {
            throw new IllegalArgumentException("Subject '" + subjectName + "' is not active or does not exist.");
        }

        // Verify subject active question bank count
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

    public ExamSchedule scheduleExam(int studentId, int subjectId, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
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

    public void updateExamSchedule(int scheduleId, int durationMinutes, int totalQuestions, double passingPercentage) throws Exception {
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

    public void cancelExam(int scheduleId) throws Exception {
        ExamSchedule existing = scheduleDAO.findById(scheduleId);
        if (existing == null) {
            throw new IllegalArgumentException("Exam schedule not found.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Cannot cancel exam. Only SCHEDULED exams can be cancelled (current status: " + existing.getStatus() + ").");
        }
        scheduleDAO.updateStatus(scheduleId, "CANCELLED");
    }

    public void startExam(int scheduleId) throws Exception {
        ExamSchedule existing = scheduleDAO.findById(scheduleId);
        if (existing == null) {
            throw new IllegalArgumentException("Exam schedule not found.");
        }
        if (!"SCHEDULED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Cannot start exam. Current status is " + existing.getStatus());
        }
        scheduleDAO.updateStatus(scheduleId, "IN_PROGRESS");
    }

    public ExamSchedule getExamScheduleById(int id) throws SQLException {
        return scheduleDAO.findById(id);
    }

    public ExamSchedule getScheduledExam(int studentId, int subjectId) throws SQLException {
        return scheduleDAO.findByStudentAndSubject(studentId, subjectId);
    }

    public List<ExamSchedule> getSchedulesByStudentId(int studentId) throws SQLException {
        return scheduleDAO.listByStudentId(studentId);
    }

    public List<ExamSchedule> getAllSchedules() throws SQLException {
        return scheduleDAO.listAll();
    }

    public List<Question> prepareExamQuestions(int subjectId) throws SQLException {
        return questionDAO.findActiveBySubjectId(subjectId);
    }

    public List<Question> prepareExamQuestions(int scheduleId, int subjectId, int totalQuestions) throws SQLException {
        List<Question> activeQuestions = questionDAO.findActiveBySubjectId(subjectId);
        if (activeQuestions.isEmpty()) {
            return activeQuestions;
        }

        // Deterministic stable shuffle per scheduleId
        long seed = scheduleId > 0 ? ((long) scheduleId * 31337L) : 42L;
        java.util.Collections.shuffle(activeQuestions, new java.util.Random(seed));

        if (totalQuestions > 0 && activeQuestions.size() > totalQuestions) {
            return new ArrayList<>(activeQuestions.subList(0, totalQuestions));
        }
        return activeQuestions;
    }

    public void completeExamSchedule(int scheduleId) throws SQLException {
        scheduleDAO.updateStatus(scheduleId, "COMPLETED");
    }
}

