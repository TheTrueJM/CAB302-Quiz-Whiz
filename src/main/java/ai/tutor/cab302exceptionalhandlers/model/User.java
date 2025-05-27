package ai.tutor.cab302exceptionalhandlers.model;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.ScryptFunction;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private static final ScryptFunction scrypt = ScryptFunction.getInstance(65536, 8, 1, 64);


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


    public boolean verifyPassword(String passwordPlaintext) {
        if (passwordPlaintext == null || passwordPlaintext.isEmpty()) {
            throw new IllegalArgumentException("Invalid Password: Cannot be empty");
        }
        return Password.check(passwordPlaintext, this.passwordHash).with(scrypt);
    }


    public static String hashPassword(String passwordPlaintext) throws IllegalArgumentException {
        if (passwordPlaintext == null || passwordPlaintext.isEmpty()) {
            throw new IllegalArgumentException("Cannot hash invalid password");
        }

        Hash passwordHash = Password.hash(passwordPlaintext).addRandomSalt(16).with(scrypt);
        return passwordHash.getResult();
    }
}
