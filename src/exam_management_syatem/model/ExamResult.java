package exam_management_syatem.model;

import java.sql.Timestamp;
import java.time.Instant;

public class ExamResult {
    private int id;
    private Integer attemptId;
    private Integer examScheduleId;
    private int studentId;
    private int subjectId;
    private String studentName; // Helper for display
    private String subjectName; // Helper for display
    private int totalQuestions;
    private int correctAnswers;
    private double marks;
    private double percentage;
    private String result; // Pass or Fail
    private Instant startedAt;
    private Instant submittedAt;

    public ExamResult() {}

    public ExamResult(int id, Integer examScheduleId, int studentId, int subjectId, int totalQuestions, int correctAnswers, double marks, double percentage, String result) {
        this.id = id;
        this.examScheduleId = examScheduleId;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.marks = marks;
        this.percentage = percentage;
        this.result = result;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getAttemptId() { return attemptId; }
    public void setAttemptId(Integer attemptId) { this.attemptId = attemptId; }

    public Integer getExamScheduleId() { return examScheduleId; }
    public void setExamScheduleId(Integer examScheduleId) { this.examScheduleId = examScheduleId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = (startedAt != null) ? startedAt.toInstant() : null; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = (submittedAt != null) ? submittedAt.toInstant() : null; }
    public Instant getCreatedAt() { return submittedAt; }
}
