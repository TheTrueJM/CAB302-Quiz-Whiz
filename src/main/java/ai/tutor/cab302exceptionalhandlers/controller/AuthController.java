package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import java.sql.SQLException;

public class AuthController {
    private UserDAO userDAO;

    public AuthController(SQLiteConnection db) {
        try {
            this.userDAO = new UserDAO(db);
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }

    public User signUp(String username, String password) {
        return null;
    }

    public User login(String username, String password) {
        return null;
    }
}
