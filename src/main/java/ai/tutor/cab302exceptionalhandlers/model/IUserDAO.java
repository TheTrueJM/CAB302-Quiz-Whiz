package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Defines the interface for a User DAO.
 *
 * Any class implementing {@code IUserDAO} must provide implementations for
 * the CRUD methods and other user-related operations in this interface.
 *
 * @author Joshua M.
 */
public interface IUserDAO {

    /**
     * Creates a new User in the database.
     *
     * @param user The User to create.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void createUser(User user) throws SQLException;

    /**
     * Updates an existing User in the database.
     *
     * @param user The User with updated information.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void updateUser(User user) throws SQLException;

    /**
     * Deletes a User from the database.
     *
     * @param user The User to delete.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public void deleteUser(User user) throws SQLException;

    /**
     * Retrieves a User from the database by their ID.
     *
     * @param id The ID of the User to retrieve.
     * @return The {@link User} matching the ID, or null if not found.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public User getUser(int id) throws SQLException;

    /**
     * Retrieves a User from the database by their username.
     *
     * @param username The username of the User to retrieve.
     * @return The {@link User} matching the username, or null if not found.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public User getUser(String username) throws SQLException;

    /**
     * Retrieves all Users from the database.
     *
     * @return A list of all {@link User} objects in the database.
     * @throws SQLException If an SQL error occurs during the operation.
     */
    public List<User> getAllUsers() throws SQLException;
}
