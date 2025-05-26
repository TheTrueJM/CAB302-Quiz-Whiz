package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a Message DAO.
 *
 * Any class implementing {@code IMessageDAO} must provide implementations for
 * creating messages and retrieving messages associated with a chat.
 *
 * @author Joshua M.
 */
public interface IMessageDAO {

    /**
     * Creates a new Message in the database.
     *
     * @param message The Message to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createMessage(Message message) throws SQLException;

    /**
     * Retrieves all Messages associated with a specific chat.
     *
     * @param chatId The ID of the chat whose messages are to be retrieved.
     * @return A list of {@link Message} objects associated with the chat.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<Message> getAllChatMessages(int chatId) throws SQLException;
}
