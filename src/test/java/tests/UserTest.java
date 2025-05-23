package tests;

import ai.tutor.cab302exceptionalhandlers.model.User;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static final Map<String, String> UserUsernames = new HashMap<>();
    static {
        UserUsernames.put("valid", "TestUser1");
        UserUsernames.put("invalidCharacters", "Test User");
        UserUsernames.put("invalidLength", "LongUsernameOver25Characters");
        UserUsernames.put("invalidEmpty", "");
        UserUsernames.put("invalidNull", null);
    }

    private static final Map<String, String> UserPasswords = new HashMap<>();
    static {
        UserPasswords.put("valid", "password1");
        UserPasswords.put("invalidEmpty", "");
        UserPasswords.put("invalidNull", null);
    }

    private static final Map<String, String> UserPasswordHashes = new HashMap<>();
    static {
        UserPasswordHashes.put("invalidEmpty", "");
        UserPasswordHashes.put("invalidNull", null);
    }

    private static final Map<String, Integer> UserIds = new HashMap<>();
    static {
        UserIds.put("valid", 1);
        UserIds.put("invalidLow", 0);
    }


    @Test
    void validPasswordHash() {
        String passwordHash = User.hashPassword(UserPasswords.get("valid"));

        assertNotNull(passwordHash);
        assertFalse(passwordHash.isEmpty());
    }

    @Test
    void invalidPasswordHashPasswordEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.hashPassword(UserPasswords.get("invalidEmpty"))
        );
    }

    @Test
    void invalidPasswordHashPasswordNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.hashPassword(UserPasswords.get("invalidNull"))
        );
    }


    @Test
    void validUserObject() {
        String username = UserUsernames.get("valid");
        String password = UserPasswords.get("valid");
        User user = new User(username, User.hashPassword(password));

        assertNotNull(user);
        assertEquals(user.getUsername(), username);
        assertTrue(user.verifyPassword(password));
    }


    @Test
    void invalidUserObjectUsernameCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("invalidCharacters"), User.hashPassword(UserPasswords.get("valid")))
        );
    }

    @Test
    void invalidUserObjectUsernameLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("invalidLength"), User.hashPassword(UserPasswords.get("valid")))
        );
    }

    @Test
    void invalidUserObjectUsernameEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("invalidEmpty"), User.hashPassword(UserPasswords.get("valid")))
        );
    }

    @Test
    void invalidUserObjectUsernameNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("invalidNull"), User.hashPassword(UserPasswords.get("valid")))
        );
    }


    @Test
    void invalidUserObjectPasswordHashEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("valid"), User.hashPassword(UserPasswordHashes.get("invalidEmpty")))
        );
    }

    @Test
    void invalidUserObjectPasswordHashNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserUsernames.get("valid"), User.hashPassword(UserPasswordHashes.get("invalidNull")))
        );
    }


    @Test
    void validUserSetId() {
        User user = new User(UserUsernames.get("valid"), User.hashPassword(UserPasswords.get("valid")));
        assertNotNull(user);

        int id = UserIds.get("valid");
        user.setId(id);
        assertEquals(user.getId(), id);
    }

    @Test
    void validUserSetIdInvalidLow() {
        User user = new User(UserUsernames.get("valid"), User.hashPassword(UserPasswords.get("valid")));
        assertNotNull(user);

        assertThrows(
                IllegalArgumentException.class,
                () -> user.setId(UserIds.get("invalidLow"))
        );
    }
}
