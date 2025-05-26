package ai.tutor.cab302exceptionalhandlers.model;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.ScryptFunction;

/**
 * Represents a user in the QuizWhiz application.
 * <p>
 * A user must have a unique username and a password hash.
 * Passwords are hashed and salted using the Scrypt algorithm
 *
 * @author Joshua M.
 * @see {#link ai.tutor.cab302exceptionalhandlers.model.UserDAO}
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private static final ScryptFunction scrypt = ScryptFunction.getInstance(65536, 8, 1, 64);


    /**
     * Constructs a User object.
     *
     * @param username The username of the user, must be 1-25 alphanumeric characters
     * @param passwordHash The hashed password of the user, must not be null or empty
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @see {#link ai.tutor.cab302exceptionalhandlers.model.User#hashPassword(String)}
     */
    public User(String username, String passwordHash) throws IllegalArgumentException {
        setUsername(username);
        setPasswordHash(passwordHash);
    }


    public int getId() { return id; }

    public void setId(int id) throws IllegalArgumentException {
        if (id < 1) {
            throw new IllegalArgumentException("Invalid Id: Must be greater than 1");
        }

        this.id = id;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) throws IllegalArgumentException {
        if (username == null || !username.matches("^[a-zA-Z0-9]{1,25}$")) {
            throw new IllegalArgumentException("Invalid Username: Must only contain 1-25 alphanumeric characters");
        }

        this.username = username;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) throws IllegalArgumentException {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Invalid Password Hash: Cannot be empty");
        }

        this.passwordHash = passwordHash;
    }


    /**
     * Verifies whether the plaintext password matches the stored password hash.
     * <p>
     * This method uses the Scrypt algorithm to check if the provided plaintext password
     * matches the hashed password stored in the user object.
     * <p>
     * Password.check does not check whether the plaintext password is empty, hence
     * this method manually validates the password before checking.
     *
     * @param passwordPlaintext the plaintext password to verify
     * @return true if the password is valid and matches the stored hash, false otherwise
     */
    public boolean verifyPassword(String passwordPlaintext) {
        if (passwordPlaintext == null || passwordPlaintext.isEmpty()) {
            throw new IllegalArgumentException("Invalid Password: Cannot be empty");
        }
        return Password.check(passwordPlaintext, this.passwordHash).with(scrypt);
    }


    /**
     * Hashes a plaintext password using the Scrypt algorithm.
     * <p>
     * Passwords are hashed with a random salt of 16 bytes to enhance security.
     *
     * @param passwordPlaintext the plaintext password to hash
     * @return the hashed password as a String
     * @throws IllegalArgumentException if the password is null or empty
     */
    public static String hashPassword(String passwordPlaintext) throws IllegalArgumentException {
        if (passwordPlaintext == null || passwordPlaintext.isEmpty()) {
            throw new IllegalArgumentException("Cannot hash invalid password");
        }

        Hash passwordHash = Password.hash(passwordPlaintext).addRandomSalt(16).with(scrypt);
        return passwordHash.getResult();
    }
}
