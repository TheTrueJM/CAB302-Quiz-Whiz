package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code AnswerOption} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations on answer options associated
 * with quiz questions in the SQLite database. Answer options are linked to quiz questions via
 * a composite key of message ID, question number, and option identifier.
 *
 * @author Jack
 */

public interface IAnswerOptionDAO {

    /**
     * Saves a new {@code AnswerOption} to the database.
     * <p>
     * This method inserts an {@code AnswerOption} into the database, storing its message ID,
     * question number, option identifier, value, and whether it is the correct answer. Implementations
     * must ensure the answer option is valid.
     *
     * @param answerOption the {@code AnswerOption} object to save
     * @throws SQLException if a database error occurs during insertion
     */

    public void createAnswerOption(AnswerOption answerOption) throws SQLException;

    /**
     * Retrieves a specific {@code AnswerOption} for a quiz question.
     * <p>
     * This method fetches an {@code AnswerOption} from the database using the provided message ID,
     * question number, and option identifier. It returns the matching answer option or {@code null}
     * if no option exists.
     *
     * @param messageId the ID of the quiz message
     * @param questionNumber the number of the question within the quiz
     * @param option the identifier of the answer option
     * @return the {@code AnswerOption} object if found, or {@code null} if no matching option exists
     * @throws SQLException if a database error occurs during retrieval
     */

    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) throws SQLException;


    /**
     * Retrieves all {@code AnswerOption} entities for a specific quiz question.
     * <p>
     * This method fetches all answer options associated with a quiz question identified by
     * message ID and question number. It returns a list of answer options, which may be empty
     * if no options exist, supporting the display of multiple-choice questions.
     *
     * @param messageId the ID of the quiz message
     * @param questionNumber the number of the question within the quiz
     * @return a {@code List} of {@code AnswerOption} objects for the question, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) throws SQLException;
}
