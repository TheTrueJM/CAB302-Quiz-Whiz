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

@Disabled("AuthController not implemented yet")
public class AuthControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private AuthController authController;

    private static final Map<String, User> Users = new HashMap<>();
    static {
        Users.put("validUser", new User("TestUser1", User.hashPassword("password")));
        Users.put("emptyUsernameUser", new User("", User.hashPassword("password")));
        Users.put("emptyPasswordUser", new User("TestUser3", User.hashPassword("")));
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
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
    }

    @Test
    public void testSignUpExistingUsername() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        User existingUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNull(existingUser);
    }

    @Test
    public void testSignUpEmptyUsername() {
        User user = Users.get("emptyUsernameUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNull(newUser);
    }

    @Test
    public void testSignUpEmptyPassword() {
        User user = Users.get("emptyPasswordUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNull(newUser);
    }

    @Test
    public void testValidLogin() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), user.getPasswordHash());
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
    }

    @Test
    public void testLoginIncorrectPassword() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), "WrongPassword");
        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyUsername() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        User loggedInUser = authController.login("", user.getPasswordHash());
        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyPassword() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPasswordHash());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), "");
        assertNull(loggedInUser);
    }
}
