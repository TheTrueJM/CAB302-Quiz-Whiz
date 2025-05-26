package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

/**
 * Outlines the contract for managing {@code User} entities.
 * <p>
 * This interface specifies methods for performing CRUD operations on users stored in
 * the SQLite database. Users are identified by a unique ID and username, with a stored
 * password hash, supporting authentication and user management within the application.
 *
 * @author Jack
 */

public interface IUserDAO {

    /**
     * Saves a new {@code User} entity to the database.
     * <p>
     * This method inserts a {@code User} entity into the {@code users} table, storing
     * its username and password hash. Implementations must validate the user to ensure
     * it has valid fields and a unique username.
     *
     * @param user the {@code User} entity to save
     * @throws SQLException if a database error occurs during insertion
     */

    public void createUser(User user) throws SQLException;

    /**
     * Updates an existing {@code User} entity in the database.
     * <p>
     * This method updates the username and password hash of a {@code User} entity in the
     * {@code users} table, identified by the user’s ID. Implementations must ensure the
     * username remains unique.
     *
     * @param user the {@code User} entity to update
     * @throws SQLException if a database error occurs during update
     */

    public void updateUser(User user) throws SQLException;

    /**
     * Deletes a {@code User} entity from the database.
     * <p>
     * This method removes a {@code User} entity from the {@code users} table based on
     * the user’s ID. Implementations should ensure cascading deletions in related tables.
     *
     * @param user the {@code User} entity to delete
     * @throws SQLException if a database error occurs during deletion
     */

    public void deleteUser(User user) throws SQLException;

    /**
     * Retrieves a {@code User} entity by its ID.
     * <p>
     * This method fetches a single {@code User} entity from the {@code users} table that
     * matches the specified ID. Returns {@code null} if no user is found for the given ID.
     *
     * @param id the ID of the user
     * @return the {@code User} entity, or {@code null} if none exists
     * @throws SQLException if a database error occurs during retrieval
     */

    public User getUser(int id) throws SQLException;

    /**
     * Retrieves a {@code User} entity by its username.
     * <p>
     * This method fetches a single {@code User} entity from the {@code users} table that
     * matches the specified username. Returns {@code null} if no user is found for the
     * given username.
     *
     * @param username the username of the user
     * @return the {@code User} entity, or {@code null} if none exists
     * @throws SQLException if a database error occurs during retrieval
     */

    public User getUser(String username) throws SQLException;

    /**
     * Retrieves all {@code User} entities from the database.
     * <p>
     * This method fetches all users from the {@code users} table. Returns a list of
     * {@code User} entities, which may be empty if no users are found.
     *
     * @return a {@code List} of {@code User} entities, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    public List<User> getAllUsers() throws SQLException;
}
