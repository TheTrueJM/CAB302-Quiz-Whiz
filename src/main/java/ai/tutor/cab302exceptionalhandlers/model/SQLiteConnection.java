package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Defines a connection to the SQLite database.
 * <p>
 * By default, it connects to a database named "tutor.db". However,
 * you can specify a different database name or use an in-memory database.
 * <p>
 * Usage:
 * <pre>
 * * SQLiteConnection connection = new SQLiteConnection();
 * * // or with a specific database name
 * * SQLiteConnection connection = new SQLiteConnection("myDatabase");
 * * // or for an in-memory database
 * * SQLiteConnection connection = new SQLiteConnection(true);
 * * * Connection conn = connection.getInstance();
 * * </pre>
 *
 * @author Joshua M.
 */
public class SQLiteConnection {
    private Connection instance = null;


    /**
     * Creates a connection to the default SQLite database "tutor.db".
     *
     * @throws SQLException if a database connection error occurs
    */
    public SQLiteConnection() throws SQLException {
        String url = "jdbc:sqlite:tutor.db";
        instance = DriverManager.getConnection(url);
    }

    /**
     * Creates a connection to a specified SQLite database.
     *
     * @param databaseName the name of the database to connect to
     * @throws SQLException if a database connection error occurs
     */
    public SQLiteConnection(String databaseName) throws SQLException {
        String url = "jdbc:sqlite:" + databaseName + ".db";
        instance = DriverManager.getConnection(url);
    }

    /**
     * Creates a connection to an in-memory SQLite database or the default "tutor.db".
     *
     * @param inMemory if true, connects to an in-memory database; otherwise, connects to "tutor.db"
     * @throws SQLException if a database connection error occurs
     */
    public SQLiteConnection(boolean inMemory) throws SQLException {
        String url = "jdbc:sqlite:" + (inMemory ? ":memory:" : "tutor.db");
        instance = DriverManager.getConnection(url);
    }


    /**
     * Returns the defined instance of the SQLite database connection.
     *
     * @return the {@link Connection} instance
     * @throws RuntimeException if the connection has not been established
     */
    public Connection getInstance() throws RuntimeException {
        if (instance == null) {
            throw new RuntimeException("Failed to connect to the database");
        }
        return instance;
    }
}
