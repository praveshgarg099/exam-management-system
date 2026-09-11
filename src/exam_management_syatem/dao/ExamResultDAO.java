package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ExamResultDAO {

    public int insert(ExamResult result) throws SQLException {
        String sql = "INSERT INTO exam_results (exam_schedule_id, student_id, subject_id, total_questions, correct_answers, marks, percentage, result, submitted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (result.getExamScheduleId() != null) {
                pstmt.setInt(1, result.getExamScheduleId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, result.getStudentId());
            pstmt.setInt(3, result.getSubjectId());
            pstmt.setInt(4, result.getTotalQuestions());
            pstmt.setInt(5, result.getCorrectAnswers());
            pstmt.setDouble(6, result.getMarks());
            pstmt.setDouble(7, result.getPercentage());
            pstmt.setString(8, result.getResult());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for exam result.");
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM exam_results WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public ExamResult findById(int id) throws SQLException {
        String sql = "SELECT r.id, r.exam_schedule_id, r.student_id, r.subject_id, r.total_questions, r.correct_answers, r.marks, r.percentage, r.result, r.started_at, r.submitted_at, "
                + "st.name AS student_name, sub.name AS subject_name "
                + "FROM exam_results r "
                + "JOIN students st ON r.student_id = st.id "
                + "JOIN subjects sub ON r.subject_id = sub.id "
                + "WHERE r.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ExamResult er = mapRow(rs);
                    er.setStudentName(rs.getString("student_name"));
                    er.setSubjectName(rs.getString("subject_name"));
                    return er;
                }
            }
        }
        return null;
    }

    public List<ExamResult> listAll() throws SQLException {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT r.id, r.exam_schedule_id, r.student_id, r.subject_id, r.total_questions, r.correct_answers, r.marks, r.percentage, r.result, r.started_at, r.submitted_at, "
                + "st.name AS student_name, sub.name AS subject_name "
                + "FROM exam_results r "
                + "JOIN students st ON r.student_id = st.id "
                + "JOIN subjects sub ON r.subject_id = sub.id "
                + "ORDER BY r.id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ExamResult er = mapRow(rs);
                er.setStudentName(rs.getString("student_name"));
                er.setSubjectName(rs.getString("subject_name"));
                list.add(er);
            }
        }
        return list;
    }

    public List<ExamResult> listByStudentId(int studentId) throws SQLException {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT r.id, r.exam_schedule_id, r.student_id, r.subject_id, r.total_questions, r.correct_answers, r.marks, r.percentage, r.result, r.started_at, r.submitted_at, "
                + "st.name AS student_name, sub.name AS subject_name "
                + "FROM exam_results r "
                + "JOIN students st ON r.student_id = st.id "
                + "JOIN subjects sub ON r.subject_id = sub.id "
                + "WHERE r.student_id = ? "
                + "ORDER BY r.id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExamResult er = mapRow(rs);
                    er.setStudentName(rs.getString("student_name"));
                    er.setSubjectName(rs.getString("subject_name"));
                    list.add(er);
                }
            }
        }
        return list;
    }

    public List<ExamResult> searchAndFilter(String studentKeyword, String subjectName, String passFail) throws SQLException {
        List<ExamResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT r.id, r.exam_schedule_id, r.student_id, r.subject_id, r.total_questions, r.correct_answers, r.marks, r.percentage, r.result, r.started_at, r.submitted_at, ")
                .append("st.name AS student_name, sub.name AS subject_name ")
                .append("FROM exam_results r ")
                .append("JOIN students st ON r.student_id = st.id ")
                .append("JOIN subjects sub ON r.subject_id = sub.id ")
                .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (studentKeyword != null && !studentKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(st.name) LIKE ? OR LOWER(st.email) LIKE ?) ");
            params.add("%" + studentKeyword.trim().toLowerCase() + "%");
            params.add("%" + studentKeyword.trim().toLowerCase() + "%");
        }
        if (subjectName != null && !subjectName.trim().isEmpty() && !subjectName.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(sub.name) = ? ");
            params.add(subjectName.trim().toLowerCase());
        }
        if (passFail != null && !passFail.trim().isEmpty() && !passFail.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(r.result) = ? ");
            params.add(passFail.trim().toLowerCase());
        }
        sql.append("ORDER BY r.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExamResult er = mapRow(rs);
                    er.setStudentName(rs.getString("student_name"));
                    er.setSubjectName(rs.getString("subject_name"));
                    list.add(er);
                }
            }
        }
        return list;
    }

    public int countByResult(String passFail) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exam_results WHERE LOWER(result) = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passFail.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public double getAveragePercentage() throws SQLException {
        String sql = "SELECT AVG(percentage) FROM exam_results";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private ExamResult mapRow(ResultSet rs) throws SQLException {
        ExamResult er = new ExamResult();
        er.setId(rs.getInt("id"));
        int scheduleId = rs.getInt("exam_schedule_id");
        if (!rs.wasNull()) {
            er.setExamScheduleId(scheduleId);
        }
        er.setStudentId(rs.getInt("student_id"));
        er.setSubjectId(rs.getInt("subject_id"));
        er.setTotalQuestions(rs.getInt("total_questions"));
        er.setCorrectAnswers(rs.getInt("correct_answers"));
        er.setMarks(rs.getDouble("marks"));
        er.setPercentage(rs.getDouble("percentage"));
        er.setResult(rs.getString("result"));
        er.setStartedAt(rs.getTimestamp("started_at"));
        er.setSubmittedAt(rs.getTimestamp("submitted_at"));
        return er;
    }
}
