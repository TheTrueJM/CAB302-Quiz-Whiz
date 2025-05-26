package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code UserAnswer} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations and queries on user answers
 * stored in the SQLite database. User answers are linked to quiz questions via a composite key
 * of message ID, attempt number, and question number, representing user responses to quiz
 * questions and supporting features such as tracking multiple attempts and quiz evaluation.
 *
 * @author Jack
 */

public interface IUserAnswerDAO {

    /**
     * Saves a new {@code UserAnswer} entity to the database.
     * <p>
     * This method inserts a {@code UserAnswer} entity into the {@code userAnswers} table,
     * storing its message ID, attempt number, question number, and answer option. Implementations
     * must validate the user answer to ensure it has valid fields and is associated with an
     * existing quiz question.
     *
     * @param userAnswer the {@code UserAnswer} entity to save
     * @throws SQLException if a database error occurs during insertion
     */

    public void createUserAnswer(UserAnswer userAnswer) throws SQLException;

    /**
     * Retrieves a {@code UserAnswer} entity by its message ID, attempt number, and question number.
     * <p>
     * This method fetches a single {@code UserAnswer} entity from the {@code userAnswers} table
     * that matches the specified composite key. Returns {@code null} if no user answer is found
     * for the given key.
     *
     * @param messageId the ID of the associated quiz
     * @param attempt the attempt number for the quiz
     * @param questionNumber the question number within the quiz
     * @return the {@code UserAnswer} entity, or {@code null} if none exists
     * @throws SQLException if a database error occurs during retrieval
     */

    public UserAnswer getUserQuestionAnswer(int messageId, int attempt, int questionNumber) throws SQLException;

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz question across all attempts.
     * <p>
     * This method fetches all user answers associated with the specified message ID and question
     * number from the {@code userAnswers} table. Returns a list of {@code UserAnswer} entities,
     * which may be empty if no answers are found for the question.
     *
     * @param messageId the ID of the associated quiz
     * @param questionNumber the question number within the quiz
     * @return a {@code List} of {@code UserAnswer} entities for the question, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<UserAnswer> getAllUserQuestionAttempts(int messageId, int questionNumber) throws SQLException;

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz attempt.
     * <p>
     * This method fetches all user answers associated with the specified message ID and attempt
     * number from the {@code userAnswers} table. Returns a list of {@code UserAnswer} entities,
     * which may be empty if no answers are found for the attempt.
     *
     * @param messageId the ID of the associated quiz
     * @param attempt the attempt number for the quiz
     * @return a {@code List} of {@code UserAnswer} entities for the attempt, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) throws SQLException;

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz across all attempts.
     * <p>
     * This method fetches all user answers associated with the specified message ID from the
     * {@code userAnswers} table. Returns a list of {@code UserAnswer} entities, which may be
     * empty if no answers are found for the quiz.
     *
     * @param messageId the ID of the associated quiz
     * @return a {@code List} of {@code UserAnswer} entities for the quiz, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<UserAnswer> getAllUserQuizAttempts(int messageId) throws SQLException;
}
