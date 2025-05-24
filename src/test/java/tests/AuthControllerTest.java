package tests;

import static org.junit.jupiter.api.Assertions.*;

import ai.tutor.cab302exceptionalhandlers.controller.LoginController;
import ai.tutor.cab302exceptionalhandlers.controller.SignUpController;
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
    private SignUpController signUpController;
    private LoginController loginController;

    private static final Map<String, String> User = new HashMap<>();
    static {
        User.put("username", "TestUser");
        User.put("password", "password");
    }


    @BeforeEach
    public void setUp(TestInfo testInfo) throws RuntimeException, SQLException {
        System.out.println("Running test: " + testInfo.getDisplayName());
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        signUpController = new SignUpController(db);
        loginController = new LoginController(db);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testValidSignUp() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertNotNull(newUser);
        assertEquals(1, newUser.getId());
        assertEquals(User.get("username"), newUser.getUsername());
        assertTrue(newUser.verifyPassword(User.get("password")));
    }

    @Test
    public void testSignUpExistingUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> signUpController.authenticateUser(User.get("username"), User.get("password"))
        );
    }

    @Test
    public void testSignUpEmptyUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> signUpController.authenticateUser("", User.get("password"))
        );
    }

    @Test
    public void testSignUpNullUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> signUpController.authenticateUser(null, User.get("password"))
        );
    }

    @Test
    public void testSignUpEmptyPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> signUpController.authenticateUser(User.get("username"), "")
        );
    }

    @Test
    public void testSignUpNullPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> signUpController.authenticateUser(User.get("username"), null)
        );
    }


    @Test
    public void testValidLogin() throws IllegalStateException, IllegalArgumentException, SQLException {
        User newUser = signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertNotNull(newUser);
        User loggedInUser = loginController.authenticateUser(User.get("username"), User.get("password"));
        assertNotNull(loggedInUser);
        assertEquals(newUser.getId(), loggedInUser.getId());
        assertEquals(newUser.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void testLoginIncorrectPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                SecurityException.class,
                () -> loginController.authenticateUser(User.get("username"), "WrongPassword")
        );
    }

    @Test
    public void testLoginEmptyUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> loginController.authenticateUser("", User.get("password"))
        );
    }

    @Test
    public void testLoginNullUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> loginController.authenticateUser(null, User.get("password"))
        );
    }

    @Test
    public void testLoginEmptyPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> loginController.authenticateUser(User.get("username"), "")
        );
    }

    @Test
    public void testLoginNullPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        signUpController.authenticateUser(User.get("username"), User.get("password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> loginController.authenticateUser(User.get("username"), null)
        );
    }
}
