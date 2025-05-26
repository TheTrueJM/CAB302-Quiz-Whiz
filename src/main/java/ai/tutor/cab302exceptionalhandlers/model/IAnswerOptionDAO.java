package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for an AnswerOption DAO.
 *
 * Any class implementing {@code IAnswerOptionDAO} must provide implementations for
 * the CRUD methods in this interface.
 *
 * @author Joshua M.
 */
public interface IAnswerOptionDAO {

    /**
     * Creates a new AnswerOption in the database.
     *
     * @param answerOption The AnswerOption to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createAnswerOption(AnswerOption answerOption) throws SQLException;

    /**
     * Retrieves an AnswerOption from the database
     *
     * @param messageId The ID of the message associated with the AnswerOption.
     * @param questionNumber The question number associated with the AnswerOption.
     * @param option The specific option text to retrieve.
     * @return The {@link AnswerOption} matching the implemented criteria.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) throws SQLException;

    /**
     * Retrieves all AnswerOptions for a specific question in a quiz.
     *
     * @param messageId The ID of the message associated with the quiz.
     * @param questionNumber The question number for which to retrieve answer options.
     * @return A list of {@link AnswerOption} objects for the specified question.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) throws SQLException;
}
