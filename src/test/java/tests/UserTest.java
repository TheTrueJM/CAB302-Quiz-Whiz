package tests;

import ai.tutor.cab302exceptionalhandlers.model.User;
import com.password4j.ScryptFunction;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static final Map<String, String> UserDetails = new HashMap<>();
    static {
        UserDetails.put("usernameValid", "TestUser1");
        UserDetails.put("passwordValid", "password1");

        UserDetails.put("usernameInvalidCharacters", "Test User");
        UserDetails.put("usernameInvalidLength", "LongUsernameOver25Characters");
        UserDetails.put("usernameInvalidEmpty", "");
        UserDetails.put("usernameInvalidNull", null);

        UserDetails.put("passwordInvalidEmpty", "");
        UserDetails.put("passwordInvalidNull", null);

        UserDetails.put("passwordHashInvalidEmpty", "");
        UserDetails.put("passwordHashInvalidNull", null);
    }


    @Test
    void validPasswordHash() {
        String passwordHash = User.hashPassword(UserDetails.get("passwordValid"));

        assertNotNull(passwordHash);
        assertFalse(passwordHash.isEmpty());
    }

    @Test
    void invalidPasswordHashPasswordEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.hashPassword(UserDetails.get("passwordInvalidEmpty"))
        );
    }

    @Test
    void invalidPasswordHashPasswordNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.hashPassword(UserDetails.get("passwordInvalidNull"))
        );
    }


    @Test
    void validUserObject() {
        String username = UserDetails.get("usernameValid");
        String password = UserDetails.get("passwordValid");
        User user = new User(username, User.hashPassword(password));

        assertNotNull(user);
        assertEquals(user.getUsername(), username);
        assertTrue(user.verifyPassword(password));
    }

    @Test
    void invalidUserObjectUsernameCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameInvalidCharacters"), User.hashPassword(UserDetails.get("passwordValid")))
        );
    }

    @Test
    void invalidUserObjectUsernameLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameInvalidLength"), User.hashPassword(UserDetails.get("passwordValid")))
        );
    }

    @Test
    void invalidUserObjectUsernameEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameInvalidEmpty"), User.hashPassword(UserDetails.get("passwordValid")))
        );
    }

    @Test
    void invalidUserObjectUsernameNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameInvalidNull"), User.hashPassword(UserDetails.get("passwordValid")))
        );
    }


    @Test
    void invalidUserObjectPasswordHashEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameValid"), User.hashPassword(UserDetails.get("passwordHashInvalidEmpty")))
        );
    }

    @Test
    void invalidUserObjectPasswordHashNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(UserDetails.get("usernameValid"), User.hashPassword(UserDetails.get("passwordHashInvalidNull")))
        );
    }
}
