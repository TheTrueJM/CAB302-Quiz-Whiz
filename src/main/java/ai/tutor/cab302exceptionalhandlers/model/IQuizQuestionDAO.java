package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code QuizQuestion} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations on quiz questions
 * stored in the SQLite database. Quiz questions are linked to quizzes via a message ID
 * and a question number, representing individual questions within a quiz.
 *
 * @author Jack
 */

public interface IQuizQuestionDAO {

    /**
     * Saves a new {@code QuizQuestion} entity to the database.
     * <p>
     * This method inserts a {@code QuizQuestion} entity into the {@code quizQuestions}
     * table, storing its message ID, question number, and question content. Implementations
     * must validate the quiz question to ensure it has valid fields and is associated with
     * an existing quiz.
     *
     * @param quizQuestion the {@code QuizQuestion} entity to save
     * @throws IllegalArgumentException if {@code quizQuestion} is {@code null} or contains invalid fields
     * @throws SQLException if a database error occurs during insertion
     */

    public void createQuizQuestion(QuizQuestion quizQuestion) throws SQLException;


    /**
     * Retrieves a {@code QuizQuestion} entity by its message ID and question number.
     * <p>
     * This method fetches a single {@code QuizQuestion} entity from the {@code quizQuestions}
     * table that matches the specified message ID and question number. Returns {@code null}
     * if no quiz question is found for the given key.
     *
     * @param messageId the ID of the associated quiz
     * @param number the question number within the quiz
     * @return the {@code QuizQuestion} entity, or {@code null} if none exists
     * @throws IllegalArgumentException if {@code messageId} or {@code number} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public QuizQuestion getQuizQuestion(int messageId, int number) throws SQLException;

    /**
     * Retrieves all {@code QuizQuestion} entities for a specific quiz.
     * <p>
     * This method fetches all quiz questions associated with the specified message ID
     * from the {@code quizQuestions} table. Returns a list of {@code QuizQuestion} entities,
     * which may be empty if no questions are found for the quiz.
     *
     * @param messageId the ID of the associated quiz
     * @return a {@code List} of {@code QuizQuestion} entities for the quiz, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<QuizQuestion> getAllQuizQuestions(int messageId) throws SQLException;
}
