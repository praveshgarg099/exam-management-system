package exam_management_syatem.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

public class ExamAttempt {
    private int id;
    private Integer examScheduleId;
    private int studentId;
    private int subjectId;
    private int durationMinutes;
    private int totalQuestions;
    private double passingPercentage = 40.0;
    private String status = "IN_PROGRESS";
    private Instant startedAt;
    private Instant completedAt;
    private Integer remainingSeconds;

    public ExamAttempt() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getExamScheduleId() { return examScheduleId; }
    public void setExamScheduleId(Integer examScheduleId) { this.examScheduleId = examScheduleId; }

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

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = (startedAt != null) ? startedAt.toInstant() : null; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = (completedAt != null) ? completedAt.toInstant() : null; }

    public Integer getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(Integer remainingSeconds) { this.remainingSeconds = remainingSeconds; }

    /**
     * Authoritatively calculates the expiration instant as:
     * expiration = started_at + duration_minutes
     */
    public Instant getExpirationInstant() {
        if (startedAt == null) {
            return null;
        }
        return startedAt.plus(Duration.ofMinutes(durationMinutes));
    }

    /**
     * Authoritatively calculates remaining seconds from:
     * (started_at + duration_minutes) compared against the current Instant.
     * Prevents client manipulation.
     */
    public long getAuthoritativeRemainingSeconds() {
        if (startedAt == null) {
            return durationMinutes * 60L;
        }
        Instant expiration = getExpirationInstant();
        Instant now = Instant.now();
        long remainingSeconds = Duration.between(now, expiration).getSeconds();
        return Math.max(0, remainingSeconds);
    }

    /**
     * An attempt is expired if current instant is at or after expiration instant.
     */
    public boolean isExpired() {
        if (startedAt == null) {
            return false;
        }
        return !Instant.now().isBefore(getExpirationInstant());
    }
}
