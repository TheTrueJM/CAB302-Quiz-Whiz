package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a Quiz DAO.
 *
 * Any class implementing {@code IQuizDAO} must provide implementations for
 * CRUD methods and other quiz-related operations in this interface.
 *
 * @author Joshua M.
 */
public interface IQuizDAO {

    /**
     * Creates a new Quiz in the database.
     *
     * @param quiz The Quiz to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createQuiz(Quiz quiz) throws SQLException;

    /**
     * Retrieves a Quiz from the database by its associated message ID.
     *
     * @param messageId The ID of the message associated with the Quiz.
     * @return The {@link Quiz} matching the message ID, or null if not found.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public Quiz getQuiz(int messageId) throws SQLException;

    /**
     * Retrieves all Quizzes associated with a specific chat.
     *
     * @param chatId The ID of the chat whose quizzes are to be retrieved.
     * @return A list of {@link Quiz} objects associated with the chat.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<Quiz> getAllChatQuizzes(int chatId) throws SQLException;

    /**
     * Retrieves all Quizzes associated with a specific user.
     *
     * @param userId The ID of the user whose quizzes are to be retrieved.
     * @return A list of {@link Quiz} objects associated with the user.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<Quiz> getAllUserQuizzes(int userId) throws SQLException;
}
