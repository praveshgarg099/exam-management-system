package exam_management_syatem.model;

public class ExamAttemptQuestion {
    private int id;
    private int attemptId;
    private int questionId;
    private int questionOrder;
    private String selectedOption;
    private Boolean isCorrect;

    // Associated question details for rendering
    private Question question;

    public ExamAttemptQuestion() {}

    public ExamAttemptQuestion(int attemptId, int questionId, int questionOrder) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.questionOrder = questionOrder;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }

    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
