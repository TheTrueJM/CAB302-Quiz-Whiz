package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.sql.SQLException;

public class AuthController {
    private final SQLiteConnection db;
    private final UserDAO userDAO;

    @FXML private Button signupButton;
    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;

    private boolean usernameEmpty = true;
    private boolean passwordEmpty = true;
    private boolean passwordCEmpty = true;


    public AuthController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    @FXML
    public void onFieldChanged(KeyEvent e) {
        TextField sender = (TextField)e.getSource();
        String senderID = sender.getId();
        String senderText = sender.getText();

        switch (senderID) {
            case "usernameField":
                usernameEmpty = senderText.isEmpty();
                break;
            case "passwordField":
                passwordEmpty = senderText.isEmpty();
                break;
            case "confirmPasswordField":
                passwordCEmpty = senderText.isEmpty();
                break;
        }

        submitButtonToggle();
    }

    private void submitButtonToggle(){
        boolean canSubmit = true;

        // Change condition if we are on sign up page
        if (confirmPasswordField == null) {
            canSubmit = !usernameEmpty && !passwordEmpty;
            if (canSubmit) {
                loginButton.setDisable(false);
            }
        }
        else {
            canSubmit = !usernameEmpty && !passwordEmpty && !passwordCEmpty;
            if (passwordField.getText().equals(confirmPasswordField.getText()) && canSubmit) {
                signupButton.setDisable(false);
            }
        }
    }

    @FXML
    public void onSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            User newUser = signUp(username, password);
            // TODO: Implement switch to Chat Page
            System.err.println("User Sign Up Success");
        } catch (Exception e) {
            // TODO: Display possible Sign Up error messages to FXML
            System.err.println("User Sign Up Failed: " + e.getMessage());
        }
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User existingUser = login(username, password);
            // TODO: Implement switch to Chat Page
            System.err.println("User Login Success");
        } catch (Exception e) {
            // TODO: Display possible Sign Up error messages to FXML
            System.err.println("User Login Failed: " + e.getMessage());
        }
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    public User signUp(String username, String password) throws IllegalStateException, IllegalArgumentException, SQLException {
        if (!validUsername(username)) {
            throw new IllegalArgumentException("Username is invalid");
        }
        if (!validPassword(password)) {
            throw new IllegalArgumentException("Password is invalid");
        }

        User existingUser = userDAO.getUser(username);
        if (existingUser != null) {
            throw new IllegalStateException("Username is already taken");
        }

        String hashedPassword = User.hashPassword(password);
        User newUser = new User(username, hashedPassword);
        userDAO.createUser(newUser);

        return newUser;
    }

    public User login(String username, String password) throws SecurityException, SQLException {
        User existingUser = userDAO.getUser(username == null ? "" : username);
        if (existingUser == null) {
            throw new SecurityException("User does not exist");
        }

        // Verify input password equals hashed user password
        if (!existingUser.verifyPassword(password == null ? "" : password)) {
            throw new SecurityException("Incorrect Password");
        }

        return existingUser;
    }

    private boolean validUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9]+$");
    }

    private boolean validPassword(String password) {
        return password != null && password.matches("^[a-zA-Z0-9]+$");
    }
}
