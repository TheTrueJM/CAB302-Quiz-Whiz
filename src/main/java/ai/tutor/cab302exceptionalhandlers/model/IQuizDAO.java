package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code Quiz} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations and queries on quizzes
 * stored in the SQLite database. Quizzes are linked to messages via a message ID,
 * representing AI-generated quiz content within chat sessions.
 *
 * @author Jack
 */

public interface IQuizDAO {

    /**
     * Saves a new {@code Quiz} entity to the database.
     * <p>
     * This method inserts a {@code Quiz} entity into the {@code quizzes} table, storing
     * its message ID, name, and difficulty. Implementations must validate the quiz to
     * ensure it has valid fields and is associated with an existing message.
     *
     * @param quiz the {@code Quiz} entity to save
     * @throws IllegalArgumentException if {@code quiz} is {@code null} or contains invalid fields
     * @throws SQLException if a database error occurs during insertion
     */

    public void createQuiz(Quiz quiz) throws SQLException;

    /**
     * Retrieves a {@code Quiz} entity by its associated message ID.
     * <p>
     * This method fetches a single {@code Quiz} entity from the {@code quizzes} table
     * that matches the specified message ID. Returns {@code null} if no quiz is found
     * for the given message ID.
     *
     * @param messageId the ID of the associated message
     * @return the {@code Quiz} entity, or {@code null} if none exists
     * @throws IllegalArgumentException if {@code messageId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public Quiz getQuiz(int messageId) throws SQLException;

    /**
     * Retrieves all {@code Quiz} entities for a specific chat session.
     * <p>
     * This method fetches all quizzes associated with AI-generated messages marked as
     * quizzes within the specified chat session. Returns a list of {@code Quiz} entities,
     * which may be empty if no quizzes are found for the chat ID.
     *
     * @param chatId the ID of the chat session
     * @return a {@code List} of {@code Quiz} entities for the chat, or an empty list if none exist
     * @throws IllegalArgumentException if {@code chatId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<Quiz> getAllChatQuizzes(int chatId) throws SQLException;

    /**
     * Retrieves all {@code Quiz} entities for a specific user across all their chat sessions.
     * <p>
     * This method fetches all quizzes associated with AI-generated messages marked as
     * quizzes from all chat sessions linked to the specified user ID. Returns a list of
     * {@code Quiz} entities, which may be empty if no quizzes are found for the user.
     *
     * @param userId the ID of the user
     * @return a {@code List} of {@code Quiz} entities for the user, or an empty list if none exist
     * @throws IllegalArgumentException if {@code userId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<Quiz> getAllUserQuizzes(int userId) throws SQLException;
}
