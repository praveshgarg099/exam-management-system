package exam_management_syatem.model;

import java.sql.Timestamp;

public class ExamSchedule {
    private int id;
    private int studentId;
    private int subjectId;
    private int durationMinutes;
    private int totalQuestions;
    private double passingPercentage;
    private String status = "SCHEDULED"; // SCHEDULED, COMPLETED, CANCELLED
    private Timestamp createdAt;

    public ExamSchedule() {}

    public ExamSchedule(int id, int studentId, int subjectId, int durationMinutes, int totalQuestions, double passingPercentage) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.passingPercentage = passingPercentage;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public double getPassingPercentage() { return passingPercentage; }
    public void setPassingPercentage(double passingPercentage) { this.passingPercentage = passingPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
