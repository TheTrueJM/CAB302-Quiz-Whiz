package ai.tutor.cab302exceptionalhandlers.model;

/**
 * Represents a singular quiz question.
 * <p>
 * This class encapsulates the properties of a quiz question, each
 * quiz question must have a unique message ID, a question number,
 * and the question content itself.
 *
 * @author Joshua M.
 * @see ai.tutor.cab302exceptionalhandlers.model.Quiz
 */
public class QuizQuestion {
    private final int messageId;
    private final int number;
    private final String question;


    /**
     * Constructs a QuizQuestion object.
     *
     * @param messageId The message ID that this question belongs to, must be greater than 0
     * @param number The question number within the quiz, must be greater than 0
     * @param question The content of the question, must not be null or empty and must be between 1 and 100 characters
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public QuizQuestion(int messageId, int number, String question) throws IllegalArgumentException {
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
