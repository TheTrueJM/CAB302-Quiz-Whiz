package ai.tutor.cab302exceptionalhandlers.model;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.ScryptFunction;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private static final ScryptFunction scrypt = ScryptFunction.getInstance(65536, 8, 1, 64);


    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }


    public int getId() {return id; }

    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }


    public boolean verifyPassword(String passwordPlaintext) {
        return Password.check(passwordPlaintext, this.passwordHash).with(scrypt);
    }


    public static boolean validUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean validPassword(String password) {
        return password != null && password.matches("^[a-zA-Z0-9]+$");
    }

    public static String hashPassword(String passwordPlaintext) {
        Hash passwordHash = Password.hash(passwordPlaintext).addRandomSalt(16).with(scrypt);
        return passwordHash.getResult();
    }
}
