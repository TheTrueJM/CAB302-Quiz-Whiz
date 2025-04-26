package ai.tutor.cab302exceptionalhandlers.model;

import com.password4j.Hash;
import com.password4j.Password;

public class User {
    private int id;
    private String username;
    private String passwordHash;


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
        return Password.check(passwordPlaintext, this.passwordHash).withScrypt();
    }


    public static String hashPassword(String passwordPlaintext) {
        Hash passwordHash = Password.hash(passwordPlaintext).addRandomSalt(16).withScrypt();
        return passwordHash.getResult();
    }
}
