package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    public int insert(Question q) throws SQLException {
        String sql = "INSERT INTO questions (subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, q.getSubjectId());
            pstmt.setString(2, q.getQuestionText());
            pstmt.setString(3, q.getOption1());
            pstmt.setString(4, q.getOption2());
            pstmt.setString(5, q.getOption3());
            pstmt.setString(6, q.getOption4());
            pstmt.setString(7, q.getCorrectAnswer());
            pstmt.setString(8, q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");
            pstmt.setInt(9, q.isActive() ? 1 : 0);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for question.");
    }

    public void update(Question q) throws SQLException {
        String sql = "UPDATE questions SET question_text = ?, option1 = ?, option2 = ?, option3 = ?, option4 = ?, correct_answer = ?, difficulty = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getQuestionText());
            pstmt.setString(2, q.getOption1());
            pstmt.setString(3, q.getOption2());
            pstmt.setString(4, q.getOption3());
            pstmt.setString(5, q.getOption4());
            pstmt.setString(6, q.getCorrectAnswer());
            pstmt.setString(7, q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");
            pstmt.setInt(8, q.isActive() ? 1 : 0);
            pstmt.setInt(9, q.getId());
            pstmt.executeUpdate();
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Question> findBySubjectId(int subjectId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active, created_at, updated_at FROM questions WHERE subject_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Question> findActiveBySubjectId(int subjectId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active, created_at, updated_at FROM questions WHERE subject_id = ? AND (active = 1 OR active IS NULL) ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Question> search(int subjectId, String keyword) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, subject_id, question_text, option1, option2, option3, option4, correct_answer, difficulty, active, created_at, updated_at FROM questions WHERE subject_id = ? AND LOWER(question_text) LIKE ? ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.setString(2, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countBySubjectId(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countActiveBySubjectId(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject_id = ? AND (active = 1 OR active IS NULL)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void setActive(int questionId, boolean active) throws SQLException {
        String sql = "UPDATE questions SET active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, active ? 1 : 0);
            pstmt.setInt(2, questionId);
            pstmt.executeUpdate();
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setSubjectId(rs.getInt("subject_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOption1(rs.getString("option1"));
        q.setOption2(rs.getString("option2"));
        q.setOption3(rs.getString("option3"));
        q.setOption4(rs.getString("option4"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        try {
            q.setDifficulty(rs.getString("difficulty"));
            if (q.getDifficulty() == null) q.setDifficulty("MEDIUM");
        } catch (SQLException ignored) {
            q.setDifficulty("MEDIUM");
        }
        try {
            q.setActive(rs.getInt("active") == 1);
        } catch (SQLException ignored) {
            q.setActive(true);
        }
        q.setCreatedAt(rs.getTimestamp("created_at"));
        q.setUpdatedAt(rs.getTimestamp("updated_at"));
        return q;
    }
}
