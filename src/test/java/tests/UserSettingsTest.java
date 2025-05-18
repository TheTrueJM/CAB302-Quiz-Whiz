package tests;

import ai.tutor.cab302exceptionalhandlers.controller.UserSettingsController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.UserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserSettingsTest {
    private SQLiteConnection db;
    private Connection connection;
    private UserSettingsController userSettingsController;
    private UserDAO userDAO;

    private static final String UserPassword = "password";
    private static final User[] Users = {
            new User("TestUser1", User.hashPassword(UserPassword)),
            new User("TestUser2", User.hashPassword(UserPassword))
    };


    private static final int UserId = 1;
    private static final Map<String, String> UpdatedUser = new HashMap<>();

    static {
        UpdatedUser.put("username", "UpdatedTestUser");
        UpdatedUser.put("password", "updatedPassword");
    }


    @BeforeEach
    public void setUp(TestInfo testInfo) throws RuntimeException, SQLException {
        System.out.println("Running test: " + testInfo.getDisplayName());
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        userDAO = new UserDAO(db);
        for (User user : Users) {
            userDAO.createUser(user);
        }

        userSettingsController = new UserSettingsController(db, Users[UserId - 1]);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testValidUpdateUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        userSettingsController.updateUsername(UpdatedUser.get("username"));

        User updatedUser = userDAO.getUser(UserId);

        assertNotNull(updatedUser);
        assertEquals(UserId, updatedUser.getId());
        assertEquals(UpdatedUser.get("username"), updatedUser.getUsername());
    }


    @Test
    public void testUpdateExistingUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        String existingUsername = Users[(UserId - 1) + 1].getUsername();
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsController.updateUsername(existingUsername)
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getUsername(), originalUser.getUsername());
    }

    @Test
    public void testUpdateEmptyUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsController.updateUsername("")
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getUsername(), originalUser.getUsername());
    }

    @Test
    public void testUpdateNullUsername() throws IllegalStateException, IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsController.updateUsername(null)
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getUsername(), originalUser.getUsername());
    }


    @Test
    public void testValidUpdatePassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        userSettingsController.updatePassword(UpdatedUser.get("password"), UserPassword);

        User updatedUser = userDAO.getUser(UserId);

        assertNotNull(updatedUser);
        assertEquals(UserId, updatedUser.getId());
        assertTrue(updatedUser.verifyPassword(UpdatedUser.get("password")));
    }

    @Test
    public void testUpdateWrongPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        assertThrows(
                SecurityException.class,
                () -> userSettingsController.updatePassword(UpdatedUser.get("password"), "WrongPassword")
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getPasswordHash(), originalUser.getPasswordHash());
    }

    @Test
    public void testUpdateEmptyPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsController.updatePassword("", UserPassword)
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getPasswordHash(), originalUser.getPasswordHash());
    }

    @Test
    public void testUpdateNullPassword() throws IllegalStateException, IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsController.updatePassword(null, UserPassword)
        );

        User originalUser = userDAO.getUser(UserId);
        assertNotNull(originalUser);
        assertEquals(Users[UserId - 1].getPasswordHash(), originalUser.getPasswordHash());
    }
}
