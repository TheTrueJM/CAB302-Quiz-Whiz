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

import javax.naming.AuthenticationException;

@Disabled("AuthController not implemented yet")
public class AuthControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private AuthController authController;

    private static final String PasswordPlaintext = "password";
    private static final Map<String, User> Users = new HashMap<>();
    static {
        Users.put("user1", new User("TestUser1", User.hashPassword(PasswordPlaintext)));
        Users.put("user2", new User("TestUser2", User.hashPassword(PasswordPlaintext)));
    }


    @BeforeEach
    public void setUp() throws SQLException {
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
    public void testValidSignUp() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
        assertEquals(user.getUsername(), newUser.getUsername());
        assertEquals(user.getPasswordHash(), newUser.getPasswordHash());
    }

    @Test
    public void testSignUpExistingUsername() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        assertThrows(
                Exception.class,
                () -> authController.signUp(user.getUsername(), PasswordPlaintext)
        );
    }

    @Test
    public void testSignUpEmptyUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp("", PasswordPlaintext)
        );
    }

    @Test
    public void testSignUpNullUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(null, PasswordPlaintext)
        );
    }

    @Test
    public void testSignUpEmptyPassword() {
        User user = Users.get("user1");
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(user.getUsername(), "")
        );
    }

    @Test
    public void testSignUpNullPassword() {
        User user = Users.get("user1");
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.signUp(user.getUsername(), null)
        );
    }

    @Test
    public void testValidLogin() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), PasswordPlaintext);
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
        assertEquals(newUser.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void testLoginIncorrectPassword() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        assertThrows(
                AuthenticationException.class,
                () -> authController.login(user.getUsername(), "WrongPassword")
        );
    }

    @Test
    public void testLoginEmptyUsername() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        assertThrows(
                AuthenticationException.class,
                () -> authController.login("", PasswordPlaintext)
        );
    }

    @Test
    public void testLoginNullUsername() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), PasswordPlaintext);
        assertNotNull(newUser);
        assertThrows(
                AuthenticationException.class,
                () -> authController.login(null, PasswordPlaintext)
        );
    }

    @Test
    public void testLoginEmptyPassword() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        assertThrows(
                AuthenticationException.class,
                () -> authController.login(user.getUsername(), "")
        );
    }

    @Test
    public void testLoginNullPassword() {
        User user = Users.get("user1");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        assertThrows(
                AuthenticationException.class,
                () -> authController.login(user.getUsername(), null)
        );
    }
}
