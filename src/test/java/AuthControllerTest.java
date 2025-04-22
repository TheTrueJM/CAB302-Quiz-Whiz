package ai.tutor.cab302exceptionalhandlers;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.UserDAO;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class AuthControllerTest {
    private Connection connection;
    private SQLiteConnection db;
    private UserDAO userDAO;
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        db = new SQLiteConnection("testing");
        connection = db.getInstance();
        userDAO = new UserDAO(db);
        authController = new AuthController(userDAO);
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

    @Test
    public void testValidSignUp() {
        User newUser = authController.signUp("testuser", "password123");
        assertNotNull(newUser, "User should be created successfully.");
        assertEquals("testuser", newUser.getUsername(), "Username should match.");
        assertNotNull(userDAO.getUser("testuser"), "User should exist in the database.");
    }

    @Test
    public void testSignUpExistingUsername() {
        authController.signUp("existinguser", "password123");
        User newUser = authController.signUp("existinguser", "anotherpassword");
        assertNull(newUser, "Sign up should fail for existing username.");
    }

    @Test
    public void testSignUpEmptyUsername() {
        User newUser = authController.signUp("", "password123");
        assertNull(newUser, "Sign up should fail for empty username.");
    }

    @Test
    public void testSignUpNullUsername() {
        User newUser = authController.signUp(null, "password123");
        assertNull(newUser, "Sign up should fail for null username.");
    }

    @Test
    public void testSignUpEmptyPassword() {
        User newUser = authController.signUp("newuser", "");
        assertNull(newUser, "Sign up should fail for empty password.");
    }

    @Test
    public void testSignUpNullPassword() {
        User newUser = authController.signUp("newuser", null);
        assertNull(newUser, "Sign up should fail for null password.");
    }

    @Test
    public void testValidLogin() {
        authController.signUp("loginuser", "correctpassword");
        User loggedInUser = authController.login("loginuser", "correctpassword");
        assertNotNull(loggedInUser, "Login should be successful.");
        assertEquals("loginuser", loggedInUser.getUsername(), "Logged in username should match.");
    }

    @Test
    public void testLoginInvalidUsername() {
        User loggedInUser = authController.login("nonexistentuser", "password123");
        assertNull(loggedInUser, "Login should fail for non-existent username.");
    }

    @Test
    public void testLoginIncorrectPassword() {
        authController.signUp("loginuser", "correctpassword");
        User loggedInUser = authController.login("loginuser", "wrongpassword");
        assertNull(loggedInUser, "Login should fail for incorrect password.");
    }

    @Test
    public void testLoginEmptyUsername() {
        User loggedInUser = authController.login("", "password123");
        assertNull(loggedInUser, "Login should fail for empty username.");
    }

    @Test
    public void testLoginNullUsername() {
        User loggedInUser = authController.login(null, "password123");
        assertNull(loggedInUser, "Login should fail for null username.");
    }

    @Test
    public void testLoginEmptyPassword() {
        authController.signUp("loginuser", "correctpassword");
        User loggedInUser = authController.login("loginuser", "");
        assertNull(loggedInUser, "Login should fail for empty password.");
    }

    @Test
    public void testLoginNullPassword() {
        authController.signUp("loginuser", "correctpassword");
        User loggedInUser = authController.login("loginuser", null);
        assertNull(loggedInUser, "Login should fail for null password.");
    }

    // TODO: do case sensitivity tests???
}
