import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class databaseTest {
    private Connection connection;

    @BeforeEach
    public void setUp() {
        SQLiteConnection db = new SQLiteConnection("testing");
        connection = db.getInstance();
    }

    @Test
    public void testDatabaseConnection() {
        assertEquals(true, connection != null);
    }
}
