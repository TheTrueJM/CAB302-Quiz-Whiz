package ai.tutor.cab302exceptionalhandlers.model;

public class AnswerOption {
    private final int messageId;
    private final int questionNumber;
    private final String option;
    private final String value;
    private final boolean isAnswer;


    public AnswerOption(int messageId, int questionNumber, String option, String value, boolean isAnswer) throws IllegalArgumentException {
        if (messageId < 1) { throw new IllegalArgumentException("Invalid Message Id: Must be greater than 1"); }
        this.messageId = messageId;

        if (questionNumber < 1) { throw new IllegalArgumentException("Invalid Question Number: Must be greater than 1"); }
        this.questionNumber = questionNumber;

        if (option == null || option.isEmpty() || 25 < option.length()) { throw new IllegalArgumentException("Invalid Answer Option: Must only contain 1-25 characters"); }
        this.option = option;

        if (value == null || value.isEmpty() || 100 < value.length()) { throw new IllegalArgumentException("Invalid Answer Value: Must only contain 1-100 characters"); }
        this.value = value;

        this.isAnswer = isAnswer;
    }


    public int getMessageId() { return messageId; }

    public int getQuestionNumber() { return questionNumber; }

    public String getOption() { return option; }

    public String getValue() { return value; }

    public boolean getIsAnswer() { return isAnswer; }
}
