package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
	private static Connection instance = null;

	public SQLiteConnection() {
		String url = "jdbc:sqlite:tutor.db";
		try {
			instance = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage() + " - " + e.toString());
			throw new RuntimeException("Failed to connect to the database", e);
		}
	}

	public static  Connection getInstance() {
		if (instance == null) {
			new SQLiteConnection();
		}
		return instance;
	}
}
