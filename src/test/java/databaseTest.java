import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class databaseTest {
	private Connection db = null;

	@BeforeEach
	public void setUp() {
		db = SQLiteConnection.getInstance();
	}

	@Test
	public void testDatabaseConnection() {
		assertEquals(true, db != null);
	}
}
