package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttempt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamAttemptDAO {

    public int insert(ExamAttempt attempt) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return insert(attempt, conn);
        }
    }

    public int insert(ExamAttempt attempt, Connection conn) throws SQLException {
        String sql = "INSERT INTO exam_attempts (exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (attempt.getExamScheduleId() != null) {
                pstmt.setInt(1, attempt.getExamScheduleId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, attempt.getStudentId());
            pstmt.setInt(3, attempt.getSubjectId());
            pstmt.setInt(4, attempt.getDurationMinutes());
            pstmt.setInt(5, attempt.getTotalQuestions());
            pstmt.setDouble(6, attempt.getPassingPercentage());
            pstmt.setString(7, attempt.getStatus() != null ? attempt.getStatus() : "IN_PROGRESS");
            if (attempt.getStartedAt() != null) {
                pstmt.setTimestamp(8, Timestamp.from(attempt.getStartedAt()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    attempt.setId(genId);
                    return genId;
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for exam attempt.");
    }

    public ExamAttempt findActiveAttempt(int studentId, int scheduleId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return findActiveAttempt(studentId, scheduleId, conn);
        }
    }

    public ExamAttempt findActiveAttempt(int studentId, int scheduleId, Connection conn) throws SQLException {
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE student_id = ? AND exam_schedule_id = ? AND status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, scheduleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ExamAttempt findCompletedAttempt(int studentId, int scheduleId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return findCompletedAttempt(studentId, scheduleId, conn);
        }
    }

    public ExamAttempt findCompletedAttempt(int studentId, int scheduleId, Connection conn) throws SQLException {
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE student_id = ? AND exam_schedule_id = ? AND status = 'COMPLETED' ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, scheduleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ExamAttempt findExistingAttempt(int studentId, int scheduleId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return findExistingAttempt(studentId, scheduleId, conn);
        }
    }

    public ExamAttempt findExistingAttempt(int studentId, int scheduleId, Connection conn) throws SQLException {
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE student_id = ? AND exam_schedule_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, scheduleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ExamAttempt findById(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return findById(id, conn);
        }
    }

    public ExamAttempt findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ExamAttempt findByIdForUpdate(int id, Connection conn) throws SQLException {
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE id = ? FOR UPDATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            updateStatus(id, status, conn);
        }
    }

    public void updateStatus(int id, String status, Connection conn) throws SQLException {
        String sql;
        if ("COMPLETED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
            sql = "UPDATE exam_attempts SET status = ?, completed_at = CURRENT_TIMESTAMP WHERE id = ?";
        } else {
            sql = "UPDATE exam_attempts SET status = ? WHERE id = ?";
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void updateRemainingSeconds(int id, int seconds) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            updateRemainingSeconds(id, seconds, conn);
        }
    }

    public void updateRemainingSeconds(int id, int seconds, Connection conn) throws SQLException {
        String sql = "UPDATE exam_attempts SET remaining_seconds = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seconds);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public List<ExamAttempt> listByStudent(int studentId) throws SQLException {
        List<ExamAttempt> list = new ArrayList<>();
        String sql = "SELECT id, exam_schedule_id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, started_at, completed_at, remaining_seconds "
                + "FROM exam_attempts WHERE student_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private ExamAttempt mapRow(ResultSet rs) throws SQLException {
        ExamAttempt a = new ExamAttempt();
        a.setId(rs.getInt("id"));
        int schedId = rs.getInt("exam_schedule_id");
        if (!rs.wasNull()) a.setExamScheduleId(schedId);
        a.setStudentId(rs.getInt("student_id"));
        a.setSubjectId(rs.getInt("subject_id"));
        a.setDurationMinutes(rs.getInt("duration_minutes"));
        a.setTotalQuestions(rs.getInt("total_questions"));
        a.setPassingPercentage(rs.getDouble("passing_percentage"));
        a.setStatus(rs.getString("status"));
        Timestamp startedTs = rs.getTimestamp("started_at");
        if (startedTs != null) a.setStartedAt(startedTs.toInstant());
        Timestamp completedTs = rs.getTimestamp("completed_at");
        if (completedTs != null) a.setCompletedAt(completedTs.toInstant());
        int remSec = rs.getInt("remaining_seconds");
        if (!rs.wasNull()) a.setRemainingSeconds(remSec);
        return a;
    }
}
