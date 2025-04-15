package ai.tutor.cab302exceptionalhandlers.model;

public class User {
    private int id;
    private String username;
    private String password;


    public User(String username, String password) {
        this.username = username;
        // Encrypt Password Here or Prior
        this.password = password;
    }


    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String Password) {
        // Encrypt Password here or Prior
        this.password = password;
    }
}
