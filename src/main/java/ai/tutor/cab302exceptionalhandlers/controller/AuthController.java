package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.sql.SQLException;

public class AuthController {
    private SQLiteConnection db;
    private UserDAO userDAO;

    @FXML private Button signupButton;
    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;

    private boolean usernameEmpty = true;
    private boolean passwordEmpty = true;
    private boolean passwordCEmpty = true;

    public AuthController(SQLiteConnection db) {
        try {
            this.db = db;
            this.userDAO = new UserDAO(db);
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }



    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
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
        // Enable submit button conditionally
        boolean canSubmit;

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

        if (!password.equals(confirmPassword)) {
            // TODO: SignUp Unsuccessful
            System.err.println("User Sign Up Failed - Password do not match");
        } else {
            User newUser = signUp(username, password);

            if (newUser == null) {
                // TODO: SignUp Unsuccessful
                System.err.println("User Sign Up Failed");
            } else {
                // TODO: Implement switch to Chat Page
                System.err.println("User Sign Up Success");
            }
        }
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User existingUser = login(username, password);

        if (existingUser == null) {
            // TODO: Login Unsuccessful
            System.err.println("User Sign Up Failed");
        } else {
            // TODO: Implement switch to Chat Page
            System.err.println("User Login Success");
        }
    }

    /*
     * =========================================================================
     *                          CRUD Operations
     * =========================================================================
     */
    public User signUp(String username, String password) {
        // TODO: Display possible error messages to FXML
        if (!validUsername(username) || !validPassword(password)) {
            System.out.println("Invalid details were entered");
        } else {
            try {
                User existingUser = userDAO.getUser(username);
                if (existingUser != null) {
                    System.out.println("User already exists");
                } else {
                    // Get hash of user password
                    String hashedPassword = User.hashPassword(password);

                    User newUser = new User(username, hashedPassword);
                    userDAO.createUser(newUser);

                    return newUser;
                }
            } catch (SQLException e) {
                System.err.println("Failed to read users: " + e.getMessage());
            }
        }
        return null;
    }

    public User login(String username, String password) {
        // TODO: Display possible error messages to FXML
        if (!validUsername(username) || !validPassword(password)) {
            System.out.println("Invalid details were entered");
        } else {
            try {
                User existingUser = userDAO.getUser(username);
                if (existingUser == null) {
                    System.out.println("User does not exist");
                } else {
                    // Checks if input password will equal user's hashed password
                    if (!existingUser.verifyPassword(password)) {
                        System.out.println("Incorrect Password");
                    } else {
                        return existingUser;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Failed to read users: " + e.getMessage());
            }
        }
        return null;
    }

    private boolean validUsername(String username) {
        return username.matches("^[a-zA-Z0-9]+$");
    }

    private boolean validPassword(String password) {
        return password.matches("^[a-zA-Z0-9]+$");
    }
}
