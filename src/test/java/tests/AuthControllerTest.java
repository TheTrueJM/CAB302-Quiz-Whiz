package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;

public class AuthControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private AuthController authController;

    private static final Map<String, String> User = new HashMap<>();
    static {
        User.put("username", "TestUser");
        User.put("password", "password");
    }


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
        User newUser = authController.signUp(User.get("username"), User.get("password"));
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
        assertEquals(User.get("username"), newUser.getUsername());
        assertTrue(newUser.verifyPassword(User.get("password")));
    }

    @Test
    public void testSignUpExistingUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                IllegalStateException.class,
                () -> authController.signUp(User.get("username"), User.get("password"))
        );
    }

    @Test
    public void testSignUpEmptyUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp("", User.get("password"))
        );
    }

    @Test
    public void testSignUpNullUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(null, User.get("password"))
        );
    }

    @Test
    public void testSignUpEmptyPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(User.get("username"), "")
        );
    }

    @Test
    public void testSignUpNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(User.get("username"), null)
        );
    }


    @Test
    public void testValidLogin() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = authController.signUp(User.get("username"), User.get("password"));
        assertNotNull(newUser);
        User loggedInUser = authController.login(User.get("username"), User.get("password"));
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
        assertEquals(newUser.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void testLoginIncorrectPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> authController.login(User.get("username"), "WrongPassword")
        );
    }

    @Test
    public void testLoginEmptyUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> authController.login("", User.get("password"))
        );
    }

    @Test
    public void testLoginNullUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> authController.login(null, User.get("password"))
        );
    }

    @Test
    public void testLoginEmptyPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> authController.login(User.get("username"), "")
        );
    }

    @Test
    public void testLoginNullPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        authController.signUp(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> authController.login(User.get("username"), null)
        );
    }
}
