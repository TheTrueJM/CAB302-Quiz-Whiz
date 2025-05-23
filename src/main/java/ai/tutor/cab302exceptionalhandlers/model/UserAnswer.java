package ai.tutor.cab302exceptionalhandlers.model;

public class UserAnswer {
    private final int messageId;
    private final int attempt;
    private final int questionNumber;
    private final String answerOption;


    public UserAnswer(int messageId, int attempt, int questionNumber, String answerOption) throws IllegalArgumentException {
        if (messageId < 1) { throw new IllegalArgumentException("Invalid Message Id: Must be greater than 1"); }
        this.messageId = messageId;

        if (attempt < 1) { throw new IllegalArgumentException("Invalid Attempt: Must be greater than 1"); }
        this.attempt = attempt;

        if (questionNumber < 1) { throw new IllegalArgumentException("Invalid Question Number: Must be greater than 1"); }
        this.questionNumber = questionNumber;

        if (answerOption != null &&  25 < answerOption.length()) { throw new IllegalArgumentException("Invalid Answer Option: Must be less than 25 characters"); }
        this.answerOption = answerOption;
    }


    public int getMessageId() { return messageId; }

    public int getAttempt() { return attempt; }

    public int getQuestionNumber() { return questionNumber; }

    public String getAnswerOption() { return answerOption; }
}
