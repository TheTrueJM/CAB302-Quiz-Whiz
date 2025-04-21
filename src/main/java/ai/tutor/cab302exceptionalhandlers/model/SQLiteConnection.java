package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    private Connection instance = null;


    public SQLiteConnection() {
        String url = "jdbc:sqlite:tutor.db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println(e.getMessage() + " - " + e.toString());
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    public SQLiteConnection(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName + ".db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println(e.getMessage() + " - " + e.toString());
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }


    public Connection getInstance() {
        if (instance == null) {
            throw new RuntimeException("Failed to connect to the database");
        }
        return instance;
    }
}
