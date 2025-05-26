package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a Chat DAO.
 *
 * Any class implementing {@code IChatDAO} must provide implementations for
 * the CRUD methods and other chat-related operations in this interface.
 *
 * @author Joshua M.
 */
public interface IChatDAO {

    /**
     * Creates a new Chat in the database.
     *
     * @param chat The Chat to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createChat(Chat chat) throws SQLException;

    /**
     * Updates an existing Chat in the database.
     *
     * @param chat The Chat with updated information.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void updateChat(Chat chat) throws SQLException;

    /**
     * Updates the name of an existing Chat in the database.
     *
     * @param chat The Chat object containing the ID and the new name.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void updateChatName(Chat chat) throws SQLException;

    /**
     * Deletes a Chat from the database.
     *
     * @param chat The Chat to delete.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void deleteChat(Chat chat) throws SQLException;

    /**
     * Retrieves a Chat from the database by its ID.
     *
     * @param id The ID of the Chat to retrieve.
     * @return The {@link Chat} matching the ID, or null if not found.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public Chat getChat(int id) throws SQLException;

    /**
     * Retrieves all Chats associated with a specific user.
     *
     * @param userId The ID of the user whose chats are to be retrieved.
     * @return A list of {@link Chat} objects associated with the user.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<Chat> getAllUserChats(int userId) throws SQLException;
}
