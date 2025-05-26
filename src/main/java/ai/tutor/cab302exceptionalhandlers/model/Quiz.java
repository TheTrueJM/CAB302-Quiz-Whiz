package ai.tutor.cab302exceptionalhandlers.model;

/**
 * Represents a Quiz
 * <p>
 * Quizzes must have have a name and difficulty. Within a quiz, there will be
 * multiple options for the student to choose from. See {@link QuizQuestion}
 * @author Joshua M.
 * @see {@link ai.tutor.cab302exceptionalhandlers.model.IQuizDAO}
 * @see {@link ai.tutor.cab302exceptionalhandlers.model.QuizQuestion}
 */
public class Quiz {
    private final int messageId;
    private final String name;
    private final String difficulty;


    /**
     * Constructor for a Quiz object
     *
     * @param messageId The message id that this quiz belongs to, must be greater than 0
     * @param name The name of the quiz, must not be null or empty and must be less than 50 characters
     * @param difficulty The difficulty of the quiz, must not be null or empty and must be less than 25 characters
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public Quiz(int messageId, String name, String difficulty) throws IllegalArgumentException {
        if (messageId < 1) { throw new IllegalArgumentException("Invalid Message Id: Must be greater than 1"); }
        this.messageId = messageId;

        if (name == null || name.isEmpty() || 50 < name.length()) { throw new IllegalArgumentException("Invalid Quiz Name: Must only contain 1-50 characters"); }
        this.name = name;

        if (difficulty == null || difficulty.isEmpty() || 25 < difficulty.length()) { throw new IllegalArgumentException("Invalid Quiz Difficulty: Must only contain 1-25 characters"); }
        this.difficulty = difficulty;
    }


    public int getMessageId() { return messageId; }

    public String getName() { return name; }

    public String getDifficulty() { return difficulty; }
}
