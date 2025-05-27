package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controls the sign-up screen for creating new user accounts in the AI tutor application.
 * <p>
 * Extends {@link AuthController} to inherit common authentication logic and UI handling.
 * Manages the sign-up form, validates user input (username, password, confirmed password),
 * and creates new users in the database via {@link ai.tutor.cab302exceptionalhandlers.model.UserDAO}.
 * </p>
 * @see AuthController
 * @see ai.tutor.cab302exceptionalhandlers.model.User
 */

public class SignUpController extends AuthController {
    private boolean passwordCEmpty = true;

    @FXML
    private TextField confirmPasswordField;


    public SignUpController(SQLiteConnection db) throws RuntimeException, SQLException {
        super(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    /**
     * Initializes the sign-up screen by setting up UI components and event handlers.
     * <p>
     * Calls the parent class's {@link AuthController#initialize()} method to configure
     * common authentication UI elements, then invokes {@link #setupConfirmPasswordField()}
     * to handle the confirmed password field specific to sign-up.
     * </p>
     * @see AuthController#initialize()
     */

    @Override
    public void initialize() {
        super.initialize();
        setupConfirmPasswordField();
    }

    /**
     * Handles key events in the sign-up form’s text fields to track input changes.
     * <p>
     * Updates boolean flags ({@code usernameEmpty}, {@code passwordEmpty}, {@code passwordCEmpty})
     * based on whether the username, password, or confirm password fields are empty.
     * Calls {@link #submitButtonToggle()} to enable or disable the submit button accordingly.
     * </p>
     * @param e The key event triggered by typing in a text field
     */

    @Override
    @FXML
    protected void onFieldChanged(KeyEvent e) {
        TextField sender = (TextField) e.getSource();
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

    /**
     * Configures the confirmed password field to respond to key events.
     * <p>
     * Attaches an event handler to {@link #confirmPasswordField} that calls
     * {@link #onFieldChanged(KeyEvent)} when the user types, enabling validation
     * of the confirmed password input.
     * </p>
     */

    @FXML
    protected  void setupConfirmPasswordField() {
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyReleased(this::onFieldChanged);
        }
    }

    /**
     * Enables or disables the submit button based on input field states.
     * <p>
     * Disables the button if any of {@code usernameEmpty}, {@code passwordEmpty}, or
     * {@code passwordCEmpty} is true, ensuring all fields are filled before submission.
     * </p>
     */

    @Override
    protected void submitButtonToggle() {
        submitButton.setDisable(usernameEmpty || passwordEmpty || passwordCEmpty);
    }


    /**
     * Processes the sign-up form submission to create a new user account.
     * <p>
     * Retrieves the username, password, and confirm password from the form fields.
     * Validates that the passwords match, authenticates the user via
     * {@link #authenticateUser(String, String)}, and navigates to the chat screen
     * if successful. Displays error feedback for invalid inputs or mismatches.
     * </p>
     * @throws IOException If loading the chat screen fails
     * @throws SQLException If database operations fail
     * @throws IllegalArgumentException If the username is taken or invalid
     * @throws SecurityException If the passwords do not match
     */
    @Override
    @FXML
    protected void onSubmit() throws IOException, RuntimeException, SQLException {
        resetErrorFeedback();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            if (!password.equals(confirmPassword)) {
                throw new SecurityException("Passwords do not match");
            }

            User newUser = authenticateUser(username, password);

            // Open Chat Page
            loadChat(newUser);

        } catch (IllegalArgumentException e) {
            errorFeedback(usernameFeedback, e.getMessage());
        } catch (SecurityException e) {
            errorFeedback(passwordFeedback, e.getMessage());
        }
    }

    /*
     * =====================
     *    CRUD Operations
     * =====================
     */


    /**
     * Authenticates and creates a new user with the provided username and password.
     * <p>
     * Hashes the password using {@link User#hashPassword(String)}, checks for duplicate
     * usernames via {@link ai.tutor.cab302exceptionalhandlers.model.UserDAO#getUser(String)},
     * and creates the user in the database if valid.
     * </p>
     * @param username The username for the new user
     * @param password The password to be hashed and stored
     * @return The newly created {@link User} object
     * @throws IllegalArgumentException If the username is already taken
     * @throws SQLException If database operations fail
     */

    @Override
    public User authenticateUser(String username, String password) throws IllegalArgumentException, SQLException {
        String hashedPassword = User.hashPassword(password);
        User newUser = new User(username, hashedPassword);

        User existingUser = userDAO.getUser(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username is already taken");
        }

        userDAO.createUser(newUser);
        return newUser;
    }
}
