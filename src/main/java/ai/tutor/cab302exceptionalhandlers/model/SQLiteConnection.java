package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    private Connection instance = null;


    public SQLiteConnection() throws SQLException {
        String url = "jdbc:sqlite:tutor.db";
        instance = DriverManager.getConnection(url);
    }

    public SQLiteConnection(String databaseName) throws SQLException {
        String url = "jdbc:sqlite:" + databaseName + ".db";
        instance = DriverManager.getConnection(url);
    }


    public Connection getInstance() throws RuntimeException {
        if (instance == null) {
            throw new RuntimeException("Failed to connect to the database");
        }
        return instance;
    }
}
