package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class AuthControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private AuthController authController;

    private static final User[] Users = {
            new User("TestUser1", "password"),
            new User("TestUser2", "password")
    };

    @BeforeEach
    public void setUp() throws SQLException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();
        authController = new AuthController();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
    }
    }

    @Test
    public void testValidSignUp() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );

        assertNotNull(newUser);
        assertEquals(1, newUser.getId()); // 0 or 1 for first autoIncrement ID?
    }

    @Test
    public void testSignUpExistingUsername() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User existingUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );

        assertNull(existingUser);
    }

    @Test
    public void testSignUpEmptyUsername() {
        User user = Users[0];
        User newUser = authController.signUp(
                "", user.getPassword()
        );

        assertNull(newUser);
    }

    @Test
    public void testSignUpNullUsername() {
        User user = Users[0];
        User newUser = authController.signUp(
                null, user.getPassword()
        );

        assertNull(newUser);
    }

    @Test
    public void testSignUpEmptyPassword() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), ""
        );

        assertNull(newUser);
    }

    @Test
    public void testSignUpNullPassword() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), null
        );

        assertNull(newUser);
    }

    @Test
    public void testValidLogin() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                user.getUsername(), user.getPassword()
        );

        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
    }

    @Test
    public void testLoginInvalidUsername() {
        User user = Users[0];
        User loggedInUser = authController.login(
                user.getUsername(), user.getPassword()
        );

        assertNull(loggedInUser);
    }

    @Test
    public void testLoginIncorrectPassword() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                user.getUsername(), "WrongPassword"
        );

        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyUsername() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                "", user.getPassword()
        );

        assertNull(loggedInUser);
    }

    @Test
    public void testLoginNullUsername() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                null, user.getPassword()
        );

        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyPassword() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                user.getUsername(), ""
        );

        assertNull(loggedInUser);
    }

    @Test
    public void testLoginNullPassword() {
        User user = Users[0];
        User newUser = authController.signUp(
                user.getUsername(), user.getPassword()
        );
        assertNotNull(newUser);

        User loggedInUser = authController.login(
                user.getUsername(), null
        );

        assertNull(loggedInUser);
    }

    // TODO: do case sensitivity tests???
}
