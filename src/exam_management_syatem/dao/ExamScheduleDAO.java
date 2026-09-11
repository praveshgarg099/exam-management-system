package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamScheduleDAO {

    public int insert(ExamSchedule schedule) throws SQLException {
        String sql = "INSERT INTO exam_schedules (student_id, subject_id, duration_minutes, total_questions, passing_percentage, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, schedule.getStudentId());
            pstmt.setInt(2, schedule.getSubjectId());
            pstmt.setInt(3, schedule.getDurationMinutes());
            pstmt.setInt(4, schedule.getTotalQuestions());
            pstmt.setDouble(5, schedule.getPassingPercentage());
            pstmt.setString(6, schedule.getStatus() != null ? schedule.getStatus() : "SCHEDULED");
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for exam schedule.");
    }

    public ExamSchedule findById(int id) throws SQLException {
        String sql = "SELECT id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at FROM exam_schedules WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ExamSchedule findByStudentAndSubject(int studentId, int subjectId) throws SQLException {
        String sql = "SELECT id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at FROM exam_schedules WHERE student_id = ? AND subject_id = ? AND status = 'SCHEDULED' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ExamSchedule> listAll() throws SQLException {
        List<ExamSchedule> list = new ArrayList<>();
        String sql = "SELECT id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at FROM exam_schedules ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ExamSchedule> listByStudentId(int studentId) throws SQLException {
        List<ExamSchedule> list = new ArrayList<>();
        String sql = "SELECT id, student_id, subject_id, duration_minutes, total_questions, passing_percentage, status, created_at FROM exam_schedules WHERE student_id = ? ORDER BY id DESC";
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

    public void update(ExamSchedule s) throws SQLException {
        String sql = "UPDATE exam_schedules SET duration_minutes = ?, total_questions = ?, passing_percentage = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, s.getDurationMinutes());
            pstmt.setInt(2, s.getTotalQuestions());
            pstmt.setDouble(3, s.getPassingPercentage());
            pstmt.setString(4, s.getStatus());
            pstmt.setInt(5, s.getId());
            pstmt.executeUpdate();
        }
    }

    public boolean hasActiveScheduledExams(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exam_schedules WHERE subject_id = ? AND status = 'SCHEDULED'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void updateStatus(int scheduleId, String status) throws SQLException {
        String sql = "UPDATE exam_schedules SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, scheduleId);
            pstmt.executeUpdate();
        }
    }

    private ExamSchedule mapRow(ResultSet rs) throws SQLException {
        ExamSchedule es = new ExamSchedule();
        es.setId(rs.getInt("id"));
        es.setStudentId(rs.getInt("student_id"));
        es.setSubjectId(rs.getInt("subject_id"));
        es.setDurationMinutes(rs.getInt("duration_minutes"));
        es.setTotalQuestions(rs.getInt("total_questions"));
        es.setPassingPercentage(rs.getDouble("passing_percentage"));
        es.setStatus(rs.getString("status"));
        es.setCreatedAt(rs.getTimestamp("created_at"));
        return es;
    }
}
