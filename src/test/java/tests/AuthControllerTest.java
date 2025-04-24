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
        Users.put("validUser", new User("TestUser1", "password"));
        Users.put("emptyUsernameUser", new User("", "password"));
        Users.put("emptyPasswordUser", new User("TestUser3", ""));
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
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
    }

    @Test
    public void testSignUpExistingUsername() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        User existingUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNull(existingUser);
    }

    @Test
    public void testSignUpEmptyUsername() {
        User user = Users.get("emptyUsernameUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNull(newUser);
    }

    @Test
    public void testSignUpEmptyPassword() {
        User user = Users.get("emptyPasswordUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNull(newUser);
    }

    @Test
    public void testValidLogin() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), user.getPassword());
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
    }

    @Test
    public void testLoginIncorrectPassword() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), "WrongPassword");
        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyUsername() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        User loggedInUser = authController.login("", user.getPassword());
        assertNull(loggedInUser);
    }

    @Test
    public void testLoginEmptyPassword() {
        User user = Users.get("validUser");
        User newUser = authController.signUp(user.getUsername(), user.getPassword());
        assertNotNull(newUser);
        User loggedInUser = authController.login(user.getUsername(), "");
        assertNull(loggedInUser);
    }
}
