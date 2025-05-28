package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a QuizQuestion DAO.
 *
 * Any class implementing {@code IQuizQuestionDAO} must provide implementations for
 * creating and retrieving quiz questions.
 *
 * @author Joshua M.
 */
public interface IQuizQuestionDAO {

    /**
     * Creates a new QuizQuestion in the database.
     *
     * @param quizQuestion The QuizQuestion to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createQuizQuestion(QuizQuestion quizQuestion) throws SQLException;

    /**
     * Retrieves a specific QuizQuestion from the database.
     *
     * @param messageId The ID of the message (quiz) associated with the question.
     * @param number The question number within the quiz.
     * @return The {@link QuizQuestion} matching the criteria, or null if not found.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public QuizQuestion getQuizQuestion(int messageId, int number) throws SQLException;

    /**
     * Retrieves all QuizQuestions associated with a specific quiz (message).
     *
     * @param messageId The ID of the message (quiz) whose questions are to be retrieved.
     * @return A list of {@link QuizQuestion} objects for the specified quiz.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<QuizQuestion> getAllQuizQuestions(int messageId) throws SQLException;
}
