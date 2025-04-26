package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class AuthControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private AuthController authController;

    private static final String Username = "TestUser";
    private static final String Password = "password";


    @BeforeEach
    public void setUp() throws RuntimeException, SQLException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();
        authController = new AuthController(db);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testValidSignUp() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
        assertEquals(Username, newUser.getUsername());
        assertTrue(newUser.verifyPassword(Password));
    }

    @Test
    public void testSignUpExistingUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                IllegalStateException.class,
                () -> authController.signUp(Username, Password)
        );
    }

    @Test
    public void testSignUpEmptyUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp("", Password)
        );
    }

    @Test
    public void testSignUpNullUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(null, Password)
        );
    }

    @Test
    public void testSignUpEmptyPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(Username, "")
        );
    }

    @Test
    public void testSignUpNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(Username, null)
        );
    }

    @Test
    public void testValidLogin() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        User loggedInUser = authController.login(Username, Password);
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
        assertEquals(newUser.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void testLoginIncorrectPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                SecurityException.class,
                () -> authController.login(Username, "WrongPassword")
        );
    }

    @Test
    public void testLoginEmptyUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                SecurityException.class,
                () -> authController.login("", Password)
        );
    }

    @Test
    public void testLoginNullUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                SecurityException.class,
                () -> authController.login(null, Password)
        );
    }

    @Test
    public void testLoginEmptyPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                SecurityException.class,
                () -> authController.login(Username, "")
        );
    }

    @Test
    public void testLoginNullPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(Username, Password);
        assertNotNull(newUser);
        assertThrows(
                SecurityException.class,
                () -> authController.login(Username, null)
        );
    }
}
