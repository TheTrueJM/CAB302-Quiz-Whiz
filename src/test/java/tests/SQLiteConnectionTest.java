package tests;

import static org.junit.jupiter.api.Assertions.*;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.*;

public class SQLiteConnectionTest {
    private SQLiteConnection db;
    private Connection connection;

    @BeforeEach
    public void setUp() throws SQLException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();
    }

    @AfterEach
    public void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // SQLiteConnection(":memory:")
    @Test
    public void testDatabaseConnectionParameterized() throws SQLException {
        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    // default constructor (tutor.db)
    @Test
    public void testDatabaseConnectionDefault() throws SQLException {
        SQLiteConnection defaultDb = new SQLiteConnection();
        Connection defaultConnection = defaultDb.getInstance();
        assertNotNull(defaultConnection);
        assertFalse(defaultConnection.isClosed());
        defaultConnection.close();
    }
}
