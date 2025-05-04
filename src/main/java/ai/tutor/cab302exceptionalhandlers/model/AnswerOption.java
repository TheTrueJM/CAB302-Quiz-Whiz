package ai.tutor.cab302exceptionalhandlers.model;

public class AnswerOption {
    private final int messageId;
    private final int questionNumber;
    private final String option;
    private final String value;
    private final boolean isAnswer;

    public AnswerOption(int messageId, int questionNumber, String option, String value, boolean isAnswer) {
        this.messageId = messageId;
        this.questionNumber = questionNumber;
        this.option = option;
        this.value = value;
        this.isAnswer = isAnswer;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getOption() {
        return option;
    }

    public String getValue() {
        return value;
    }

    public boolean getIsAnswer() {
        return isAnswer;
    }
}
