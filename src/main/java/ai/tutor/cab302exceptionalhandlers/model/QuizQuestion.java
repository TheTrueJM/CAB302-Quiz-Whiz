package ai.tutor.cab302exceptionalhandlers.model;

public class QuizQuestion {
    private final int messageId;
    private final int number;
    private final String question;


    public QuizQuestion(int messageId, int number, String question) {
        if (messageId < 1) { throw new IllegalArgumentException("Invalid Message Id: Must be greater than 1"); }
        this.messageId = messageId;

        if (number < 1) { throw new IllegalArgumentException("Invalid Question Number: Must be greater than 1"); }
        this.number = number;

        if (question == null || question.isEmpty() || 100 < question.length()) { throw new IllegalArgumentException("Invalid Question: Must only contain 1-100 characters"); }
        this.question = question;
    }


    public int getMessageId() { return messageId; }

    public int getNumber() { return number; }

    public String getQuestion() { return question; }
}
