package ai.tutor.cab302exceptionalhandlers.model;

public class UserAnswer {
    private final int messageId;
    private final int attempt;
    private final int questionNumber;
    private final String answerOption;


    public UserAnswer(int messageId, int attempt, int questionNumber, String answerOption) {
        this.messageId = messageId;
        this.attempt = attempt;
        this.questionNumber = questionNumber;
        this.answerOption = answerOption;
    }


    public int getMessageId() { return messageId; }

    public int getAttempt() { return attempt; }

    public int getQuestionNumber() { return questionNumber; }

    public String getAnswerOption() { return answerOption; }
}
