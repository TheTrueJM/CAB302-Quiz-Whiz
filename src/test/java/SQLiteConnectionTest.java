package ai.tutor.cab302exceptionalhandlers.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class SQLiteConnectionTest {
    private Connection connection;
    private SQLiteConnection db;

    @BeforeEach
    public void setUp() {
        db = new SQLiteConnection("testing");
        connection = db.getInstance();
    }

    // SQLiteConnection("testing")
    @Test
    public void testDatabaseConnectionParameterized() throws SQLException {
        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    // default constructor
    @Test
    public void testDatabaseConnectionDefault() throws SQLException {
        SQLiteConnection defaultDb = new SQLiteConnection();
        Connection defaultConnection = defaultDb.getInstance();
        assertNotNull(defaultConnection);
        assertFalse(defaultConnection.isClosed());
        defaultConnection.close();
    }
}
