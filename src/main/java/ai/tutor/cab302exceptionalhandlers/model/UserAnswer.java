package ai.tutor.cab302exceptionalhandlers.model;

/**
 * Represents a user's answer to a quiz question.
 * <p>
 * All variables here once set, are immutable.
 *
 * @author Joshua M.
 * @see ai.tutor.cab302exceptionalhandlers.model.Quiz
 * @see ai.tutor.cab302exceptionalhandlers.model.QuizQuestion
 * @see ai.tutor.cab302exceptionalhandlers.model.UserAnswerDAO
 */
public class UserAnswer {
    private final int messageId;
    private final int attempt;
    private final int questionNumber;
    private final String answerOption;


    /**
     * Constructs a UserAnswer object.
     *
     * @param messageId The message ID that this answer belongs to, must be greater than 0
     * @param attempt The attempt number for this answer, must be greater than 0
     * @param questionNumber The question number within the quiz, must be greater than 0
     * @param answerOption The answer option selected by the user, must not be null or empty and must be less than 25 characters
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
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
