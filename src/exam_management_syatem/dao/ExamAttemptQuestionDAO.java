package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamAttemptQuestionDAO {

    public void insertBatch(List<ExamAttemptQuestion> items, Connection conn) throws SQLException {
        String sql = "INSERT INTO exam_attempt_questions (attempt_id, question_id, question_order, selected_option, is_correct) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (ExamAttemptQuestion item : items) {
                pstmt.setInt(1, item.getAttemptId());
                pstmt.setInt(2, item.getQuestionId());
                pstmt.setInt(3, item.getQuestionOrder());
                pstmt.setString(4, item.getSelectedOption());
                if (item.getIsCorrect() == null) {
                    pstmt.setNull(5, Types.BOOLEAN);
                } else {
                    pstmt.setBoolean(5, item.getIsCorrect());
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public List<ExamAttemptQuestion> listByAttemptId(int attemptId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return listByAttemptId(attemptId, conn);
        }
    }

    public List<ExamAttemptQuestion> listByAttemptId(int attemptId, Connection conn) throws SQLException {
        List<ExamAttemptQuestion> list = new ArrayList<>();
        String sql = "SELECT eq.id, eq.attempt_id, eq.question_id, eq.question_order, eq.selected_option, eq.is_correct, "
                + "q.subject_id, q.question_text, q.option1, q.option2, q.option3, q.option4, q.correct_answer, q.difficulty, q.active "
                + "FROM exam_attempt_questions eq "
                + "JOIN questions q ON eq.question_id = q.id "
                + "WHERE eq.attempt_id = ? "
                + "ORDER BY eq.question_order ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attemptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExamAttemptQuestion item = new ExamAttemptQuestion();
                    item.setId(rs.getInt("id"));
                    item.setAttemptId(rs.getInt("attempt_id"));
                    item.setQuestionId(rs.getInt("question_id"));
                    item.setQuestionOrder(rs.getInt("question_order"));
                    item.setSelectedOption(rs.getString("selected_option"));
                    boolean isCorr = rs.getBoolean("is_correct");
                    if (!rs.wasNull()) {
                        item.setIsCorrect(isCorr);
                    }

                    Question q = new Question();
                    q.setId(rs.getInt("question_id"));
                    q.setSubjectId(rs.getInt("subject_id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setOption1(rs.getString("option1"));
                    q.setOption2(rs.getString("option2"));
                    q.setOption3(rs.getString("option3"));
                    q.setOption4(rs.getString("option4"));
                    q.setCorrectAnswer(rs.getString("correct_answer"));
                    q.setDifficulty(rs.getString("difficulty"));
                    q.setActive(rs.getBoolean("active"));
                    item.setQuestion(q);

                    list.add(item);
                }
            }
        }
        return list;
    }

    public boolean saveAnswer(int attemptId, int questionId, String selectedOption, int studentId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return saveAnswer(attemptId, questionId, selectedOption, studentId, conn);
        }
    }

    public boolean saveAnswer(int attemptId, int questionId, String selectedOption, int studentId, Connection conn) throws SQLException {
        String sql = "UPDATE exam_attempt_questions eq "
                + "SET selected_option = ? "
                + "FROM exam_attempts ea "
                + "WHERE eq.attempt_id = ea.id "
                + "AND eq.attempt_id = ? "
                + "AND eq.question_id = ? "
                + "AND ea.student_id = ? "
                + "AND ea.status = 'IN_PROGRESS' "
                + "AND (ea.started_at + (ea.duration_minutes * INTERVAL '1 minute')) > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedOption);
            pstmt.setInt(2, attemptId);
            pstmt.setInt(3, questionId);
            pstmt.setInt(4, studentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int gradeAttempt(int attemptId, Connection conn) throws SQLException {
        // Evaluate each question by comparing selected_option with questions.correct_answer (trimmed, case-insensitive)
        String evalSql = "UPDATE exam_attempt_questions eq "
                + "SET is_correct = (eq.selected_option IS NOT NULL AND LOWER(TRIM(eq.selected_option)) = LOWER(TRIM(q.correct_answer))) "
                + "FROM questions q "
                + "WHERE eq.question_id = q.id AND eq.attempt_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(evalSql)) {
            pstmt.setInt(1, attemptId);
            pstmt.executeUpdate();
        }

        // Return count of correct answers
        String countSql = "SELECT COUNT(*) FROM exam_attempt_questions WHERE attempt_id = ? AND is_correct = TRUE";
        try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setInt(1, attemptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
