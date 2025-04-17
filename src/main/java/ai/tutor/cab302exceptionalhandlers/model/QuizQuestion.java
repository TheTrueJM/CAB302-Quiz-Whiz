package ai.tutor.cab302exceptionalhandlers.model;

public class QuizQuestion {
    private final int messageId;
    private final int number;
    private final String question;
    // NOTE: Question Answer Here?


    public QuizQuestion(int messageId, int number, String question) {
        this.messageId = messageId;
        this.number = number;
        this.question = question;
    }


    public int getMessageId() { return messageId; }

    public int getNumber() { return number; }

    public String getQuestion() { return question; }
}
