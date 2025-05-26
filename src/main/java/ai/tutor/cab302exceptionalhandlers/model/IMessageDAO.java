package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code Message} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations on messages within chat
 * sessions in the SQLite database. Messages represent user inputs or AI responses, associated
 * with a chat session via a chat ID.
 *
 * @author Jack
 */

public interface IMessageDAO {

    /**
     * Saves a new {@code Message} entity to the database.
     * <p>
     * This method inserts a {@code Message} entity into the {@code messages} table, storing
     * its chat ID, content, sender type (user or AI), and quiz flag. Implementations must
     * validate the message to ensure it has valid fields before saving.
     *
     * @param message the {@code Message} entity to save
     * @throws IllegalArgumentException if {@code message} is {@code null} or contains invalid fields
     * @throws SQLException if a database error occurs during insertion
     */

    public void createMessage(Message message) throws SQLException;

    /**
     * Retrieves all {@code Message} entities associated with a specific chat session.
     * <p>
     * This method fetches all messages associated a given chat ID from the {@code messages}
     * table. It returns a list of {@code Message} entities, which may be empty if no messages
     * are found for the specified chat ID.
     *
     * @param chatId the ID of the chat session
     * @return a {@code List} of {@code Message} entities for the specified chat, or an empty list if none exist
     * @throws IllegalArgumentException if {@code chatId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<Message> getAllChatMessages(int chatId) throws SQLException;
}
