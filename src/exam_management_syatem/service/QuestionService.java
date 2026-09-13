package exam_management_syatem.service;

import exam_management_syatem.dao.QuestionDAO;
import exam_management_syatem.dao.SubjectDAO;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;

import java.sql.SQLException;
import java.util.List;

public class QuestionService {
    private final QuestionDAO questionDAO;
    private final SubjectDAO subjectDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
        this.subjectDAO = new SubjectDAO();
    }

    public Question addQuestion(UserSession session, int subjectId, String questionText, String option1, String option2, String option3, String option4, String correctAnswer) throws Exception {
        return addQuestion(session, subjectId, questionText, option1, option2, option3, option4, correctAnswer, "MEDIUM");
    }

    public Question addQuestion(UserSession session, int subjectId, String questionText, String option1, String option2, String option3, String option4, String correctAnswer, String difficulty) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        validateInputs(questionText, option1, option2, option3, option4, correctAnswer);

        Subject subject = subjectDAO.findById(subjectId);
        if (subject == null) {
            throw new IllegalArgumentException("Invalid subject ID: " + subjectId);
        }

        Question q = new Question();
        q.setSubjectId(subjectId);
        q.setQuestionText(questionText.trim());
        q.setOption1(option1.trim());
        q.setOption2(option2.trim());
        q.setOption3(option3.trim());
        q.setOption4(option4.trim());
        q.setCorrectAnswer(correctAnswer.trim());
        q.setDifficulty(normalizeDifficulty(difficulty));
        q.setActive(true);

        int id = questionDAO.insert(q);
        q.setId(id);
        return q;
    }

    public void updateQuestion(UserSession session, int questionId, String questionText, String option1, String option2, String option3, String option4, String correctAnswer) throws Exception {
        updateQuestion(session, questionId, questionText, option1, option2, option3, option4, correctAnswer, "MEDIUM");
    }

    public void updateQuestion(UserSession session, int questionId, String questionText, String option1, String option2, String option3, String option4, String correctAnswer, String difficulty) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        validateInputs(questionText, option1, option2, option3, option4, correctAnswer);

        Question q = new Question();
        q.setId(questionId);
        q.setQuestionText(questionText.trim());
        q.setOption1(option1.trim());
        q.setOption2(option2.trim());
        q.setOption3(option3.trim());
        q.setOption4(option4.trim());
        q.setCorrectAnswer(correctAnswer.trim());
        q.setDifficulty(normalizeDifficulty(difficulty));

        questionDAO.update(q);
    }

    public void setQuestionActive(UserSession session, int questionId, boolean active) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        questionDAO.setActive(questionId, active);
    }

    public boolean deleteQuestion(UserSession session, int questionId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        try {
            return questionDAO.delete(questionId);
        } catch (SQLException sqle) {
            // If question is referenced in historical exam attempts (FK 23503 or RESTRICT 23001), soft-delete to preserve history
            String state = sqle.getSQLState();
            if ("23503".equals(state) || "23001".equals(state) || (sqle.getMessage() != null && sqle.getMessage().contains("exam_attempt_questions"))) {
                questionDAO.setActive(questionId, false);
                return true;
            }
            throw sqle;
        }
    }

    public List<Question> getQuestionsBySubjectId(UserSession session, int subjectId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return questionDAO.findBySubjectId(subjectId);
    }

    public List<Question> getActiveQuestionsBySubjectId(UserSession session, int subjectId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        return questionDAO.findActiveBySubjectId(subjectId);
    }

    public List<Question> searchQuestions(UserSession session, int subjectId, String keyword) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return questionDAO.search(subjectId, keyword);
    }

    public int getQuestionCountBySubjectId(UserSession session, int subjectId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return questionDAO.countBySubjectId(subjectId);
    }

    public int getActiveQuestionCountBySubjectId(UserSession session, int subjectId) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAuthenticated();
        return questionDAO.countActiveBySubjectId(subjectId);
    }

    private String normalizeDifficulty(String diff) {
        if (diff == null || diff.trim().isEmpty()) return "MEDIUM";
        String upper = diff.trim().toUpperCase();
        if (upper.equals("EASY") || upper.equals("MEDIUM") || upper.equals("HARD")) {
            return upper;
        }
        return "MEDIUM";
    }

    private void validateInputs(String qText, String opt1, String opt2, String opt3, String opt4, String ans) {
        if (qText == null || qText.trim().isEmpty()) throw new IllegalArgumentException("Question text is required.");
        if (opt1 == null || opt1.trim().isEmpty()) throw new IllegalArgumentException("Option 1 is required.");
        if (opt2 == null || opt2.trim().isEmpty()) throw new IllegalArgumentException("Option 2 is required.");
        if (opt3 == null || opt3.trim().isEmpty()) throw new IllegalArgumentException("Option 3 is required.");
        if (opt4 == null || opt4.trim().isEmpty()) throw new IllegalArgumentException("Option 4 is required.");
        if (ans == null || ans.trim().isEmpty()) throw new IllegalArgumentException("Correct answer is required.");

        String cleanAns = ans.trim();
        if (!cleanAns.equalsIgnoreCase(opt1.trim()) &&
            !cleanAns.equalsIgnoreCase(opt2.trim()) &&
            !cleanAns.equalsIgnoreCase(opt3.trim()) &&
            !cleanAns.equalsIgnoreCase(opt4.trim())) {
            throw new IllegalArgumentException("The correct answer must match one of the four options exactly.");
        }
    }
}
