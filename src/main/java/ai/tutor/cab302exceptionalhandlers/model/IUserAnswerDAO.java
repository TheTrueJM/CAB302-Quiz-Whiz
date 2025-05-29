package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a UserAnswer DAO.
 *
 * Any class implementing {@code IUserAnswerDAO} must provide implementations for
 * the CRUD methods in this interface.
 *
 * @author Joshua M.
 */
public interface IUserAnswerDAO {

    /**
     * Creates a new UserAnswer in the database.
     *
     * @param userAnswer The UserAnswer to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createUserAnswer(UserAnswer userAnswer) throws SQLException;

    /**
     * Retrieves a UserAnswer from the database
     *
     * @param messageId The ID of the message associated with the UserAnswer.
     * @param attempt The question attempt associated with the UserAnswer.
     * @param questionNumber The question number associated with the UserAnswer.
     * @return The {@link UserAnswer} matching the implemented criteria.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public UserAnswer getUserQuestionAnswer(int messageId, int attempt, int questionNumber) throws SQLException;

    /**
     * Retrieves all attempts of UserAnswers for a specific question in a quiz.
     *
     * @param messageId The ID of the message associated with the quiz.
     * @param questionNumber The question number for which to retrieve answer options.
     * @return A list of {@link UserAnswer} objects for the specified question.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<UserAnswer> getAllUserQuestionAttempts(int messageId, int questionNumber) throws SQLException;

    /**
     * Retrieves all question UserAnswers for a specific attempt of a quiz.
     *
     * @param messageId The ID of the message associated with the quiz.
     * @param attempt The attempt for which to retrieve answer options.
     * @return A list of {@link UserAnswer} objects for the specified question.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) throws SQLException;

    /**
     * Retrieves all attempts of question UserAnswers for a specific quiz.
     *
     * @param messageId The ID of the message associated with the quiz.
     * @return A list of {@link UserAnswer} objects for the specified question.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<UserAnswer> getAllUserQuizAttempts(int messageId) throws SQLException;
}
