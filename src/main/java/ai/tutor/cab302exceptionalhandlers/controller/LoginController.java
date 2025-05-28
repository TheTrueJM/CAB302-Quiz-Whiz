package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controls the login screen for creating new user accounts in the AI tutor application.
 * <p>
 * Extends {@link AuthController} to inherit common authentication logic and UI handling.
 * Manages the login form, validates user input (username, password),
 * and access existing users in the database via {@link ai.tutor.cab302exceptionalhandlers.model.UserDAO}.
 * </p>
 * @see AuthController
 * @see ai.tutor.cab302exceptionalhandlers.model.User
 */

public class LoginController extends AuthController {
    public LoginController(SQLiteConnection db) throws RuntimeException, SQLException {
        super(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    /**
     * Handles key events in the login form’s text fields to track input changes.
     * <p>
     * Updates boolean flags ({@code usernameEmpty} and {@code passwordEmpty}
     * based on whether the username or password fields are empty.
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
        }

        submitButtonToggle();
    }

    /**
     * Enables or disables the submit button based on input field states.
     * <p>
     * Disables the button if any of {@code usernameEmpty} or {@code passwordEmpty}
     * is true, ensuring all fields are filled before submission.
     * </p>
     */

    @Override
    protected void submitButtonToggle() {
        submitButton.setDisable(usernameEmpty || passwordEmpty);
    }


    /**
     * Processes the sign-up form submission to create a new user account.
     * <p>
     * Retrieves the username, password, and confirm password from the form fields.
     * Validates that the passwords match, authenticates the user via
     * {@link #authenticateUser(String, String)}, and navigates to the chat screen
     * if successful. Displays error feedback for invalid inputs or mismatches.
     * </p>
     */

    @Override
    @FXML
    protected void onSubmit() {
        resetErrorFeedback();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User existingUser = authenticateUser(username, password);

            // Open Chat Page
            loadChat(existingUser);

        } catch (IllegalArgumentException e) {
            errorFeedback(usernameFeedback, e.getMessage());
        } catch (SecurityException e) {
            errorFeedback(passwordFeedback, e.getMessage());
        } catch (Exception e) {
            Utils.showErrorAlert(e.getMessage());
        }
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    /**
     * Authenticates an existing user with the provided username and password.
     * <p>
     * Checks for an existing username, and verifies the password hash{@link User#verifyPassword(String)}
     * via {@link ai.tutor.cab302exceptionalhandlers.model.UserDAO#getUser(String)},
     * and creates the user in the database if valid.
     * </p>
     * @param username The username for the existing user
     * @param password The password to be verified against the hashed password
     * @return The existing {@link User} object
     * @throws IllegalArgumentException If the username does not exist
     * @throws SecurityException if the password is incorrect to the hash verification
     * @throws SQLException If database operations fail
     */

    @Override
    public User authenticateUser(String username, String password) throws IllegalArgumentException, SecurityException, SQLException {
        User existingUser = userDAO.getUser(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Verify input password equals hashed user password
        if (!existingUser.verifyPassword(password)) {
            throw new SecurityException("Incorrect Password");
        }

        return existingUser;
    }
}
