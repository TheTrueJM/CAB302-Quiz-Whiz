package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.SceneManager;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Controls the user settings screen in the AI tutor application.
 * <p>
 * Allows users to update their username or password, view account statistics,
 * log out, or terminate their account. Interacts with the database via
 * {@link UserDAO} to update user details and uses {@link SceneManager} for
 * navigation to other screens (e.g., chat, login, sign-up).
 * </p>
 * @see UserDAO
 * @see SceneManager
 * @see User
 */

public class UserSettingsController {
    private SQLiteConnection db;
    private UserDAO userDAO;
    private User currentUser;

    private boolean usernameChanged = false;
    private boolean passwordChanged = false;

    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button saveButton;
    @FXML private Button terminateUserButton;

    @FXML private TextField usernameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label usernameFeedback;
    @FXML private Label currentPasswordFeedback;
    @FXML private Label newPasswordFeedback;

    // User stats widgets
    @FXML private Label quizzesTakenLabel;
    @FXML private Label averageScoreLabel;

    /**
     * Constructs a UserSettingsController with a database connection and user.
     * <p>
     * Initializes the {@link #db} connection, creates a {@link UserDAO} instance,
     * and sets the {@link #currentUser} whose settings will be managed. Throws
     * an exception if the provided user is null.
     * </p>
     * @param connection The SQLite database connection
     * @param authenticatedUser The currently authenticated user
     * @throws IllegalStateException If the user is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public UserSettingsController(SQLiteConnection connection, User authenticatedUser) throws IllegalStateException, RuntimeException, SQLException {
        if (authenticatedUser == null) {
            throw new IllegalStateException("No user was authenticated");
        }

        db = connection;
        userDAO = new UserDAO(db);
        currentUser = authenticatedUser;
    }

    /**
     * Initializes the user settings screen’s UI components and event handlers.
     * <p>
     * Sets up the username field ({@link #setupUsernameField()}), back button
     * ({@link #setupBackButton()}), logout button ({@link #setupLogoutButton()}),
     * save button ({@link #setupSaveButton()}), and terminate button
     * ({@link #setupTerminateButton()}). Called automatically by JavaFX when the
     * FXML is loaded.
     * </p>
     */

    @FXML
    public void initialize() {
        setupUsernameField();
        setupBackButton();
        setupLogoutButton();
        setupSaveButton();
        setupTerminateButton();
    }

    /**
     * Populates the username field with the current user’s username.
     * <p>
     * Sets the text of {@link #usernameField} to the username of {@link #currentUser}.
     * </p>
     */

    private void setupUsernameField() {
        usernameField.setText(currentUser.getUsername());
    }


    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    /**
     * Navigates back to the chat screen for the current user.
     * <p>
     * Uses {@link SceneManager} to load the chat interface for {@link #currentUser}.
     * </p>
     * @throws Exception If navigation or related operations fail
     */

    @FXML
    private void onBack() throws Exception {
       SceneManager.getInstance().navigateToChat(currentUser);
    }

    /**
     * Configures the back button to navigate to the chat screen.
     * <p>
     * Attaches an event handler to {@link #backButton} that calls {@link #onBack()}
     * when clicked, with error handling to display alerts for failures.
     * </p>
     */

    @FXML
    private void setupBackButton() {
            backButton.setOnAction(actionEvent -> {
                try {
                    onBack();
                } catch (Exception e) {
                    Utils.showErrorAlert("Failed to load chat page " + e.getMessage());
                }
            });

    }

    /**
     * Logs out the current user and navigates to the login screen.
     * <p>
     * Prompts the user with a confirmation dialog using {@link Utils#showConfirmAlert(String)}.
     * If confirmed, navigates to the login screen via {@link SceneManager}.
     * </p>
     * @throws Exception If navigation or related operations fail
     */

    @FXML
    private void onLogout() throws Exception {
        Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to logout?");
        if (result.isPresent()) {
            ButtonType buttonClicked = result.get();
            if (buttonClicked == ButtonType.OK) {
                SceneManager.getInstance().navigateToAuth(AuthType.LOGIN);
            }
        }
    }

    /**
     * Configures the logout button to handle user logout.
     * <p>
     * Attaches an event handler to {@link #logoutButton} that calls {@link #onLogout()}
     * when clicked, with error handling to display alerts for failures.
     * </p>
     */

    @FXML
    private void setupLogoutButton() {
        logoutButton.setOnAction(actionEvent -> {
            try {
                onLogout();
            } catch (Exception e) {
                Utils.showErrorAlert("Failed to logout " + e.getMessage());
            }
        });

    }

    /**
     * Saves changes to the user’s settings by updating their username and/or password.
     * <p>
     * Clears feedback labels, retrieves input from {@link #usernameField},
     * {@link #currentPasswordField}, {@link #newPasswordField}, and
     * {@link #confirmPasswordField}, and calls {@link #handleUsernameUpdate(String)}
     * and {@link #handlePasswordUpdate(String, String, String)} to process updates.
     * Displays a success message via {@link Utils#showInfoAlert(String)} indicating
     * which fields were updated, or if no changes were made.
     * </p>
     */

    @FXML
    private void onSave() {
        usernameFeedback.setText("");
        currentPasswordFeedback.setText("");
        newPasswordFeedback.setText("");

        String username = usernameField.getText();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        usernameChanged = passwordChanged = false;

        handleUsernameUpdate(username);
        handlePasswordUpdate(currentPassword, newPassword, confirmPassword);

        String feedbackMessage;
        if (usernameChanged && passwordChanged) {
            feedbackMessage = "Username & Password updated";
        } else if (usernameChanged) {
            feedbackMessage = "Username updated";
        } else if (passwordChanged) {
            feedbackMessage = "Password updated";
        } else {
            feedbackMessage = "No details updated";
        }

        Utils.showInfoAlert(feedbackMessage);
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    /**
     * Configures the save button to handle user settings updates.
     * <p>
     * Attaches an event handler to {@link #saveButton} that calls {@link #onSave()}
     * when clicked.
     * </p>
     */

    @FXML
    private void setupSaveButton() {
        saveButton.setOnAction(actionEvent -> {
            onSave();
        });

    }

    /**
     * Terminates the current user’s account and navigates to the sign-up screen.
     * <p>
     * Prompts the user with a confirmation dialog using {@link Utils#showConfirmAlert(String)}.
     * If confirmed, deletes the user from the database via {@link UserDAO#deleteUser(User)}
     * and navigates to the sign-up screen using {@link SceneManager}.
     * </p>
     * @throws Exception If navigation, database deletion, or related operations fail
     */

    @FXML
    private void onTerminate() throws Exception {
        Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to delete of your account?");
        if (result.isPresent()) {
            ButtonType buttonClicked = result.get();
            if (buttonClicked == ButtonType.OK) {;
                try {
                    userDAO.deleteUser(currentUser);
                    SceneManager.getInstance().navigateToAuth(AuthType.SIGNUP);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to delete account: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Configures the terminate button to handle account deletion.
     * <p>
     * Attaches an event handler to {@link #terminateUserButton} that calls
     * {@link #onTerminate()} when clicked, with error handling to display alerts
     * for failures.
     * </p>
     */

    @FXML
    private void setupTerminateButton() {
        terminateUserButton.setOnAction(actionEvent -> {
            try {
                onTerminate();
            } catch (Exception e) {
                Utils.showErrorAlert("Failed to terminate account  " + e.getMessage());
            }
        });

    }

    /**
     * Attempts to update the user’s username and provides feedback on failure.
     * <p>
     * Calls {@link #updateUsername(String)} to update the username in the database.
     * Sets {@link #usernameChanged} to true if successful, or displays an error message
     * in {@link #usernameFeedback} if an exception occurs.
     * </p>
     * @param username The new username to set
     */

    private void handleUsernameUpdate(String username) {
        try {
            updateUsername(username);
            usernameChanged = true;
        } catch (Exception e) {
            usernameFeedback.setText(e.getMessage());
        }
    }

    /**
     * Attempts to update the user’s password and provides feedback on failure.
     * <p>
     * Validates that the new password matches the confirmation and that the current
     * password is correct by calling {@link #updatePassword(String, String)}.
     * Sets {@link #passwordChanged} to true if successful, or displays error messages
     * in {@link #currentPasswordFeedback} or {@link #newPasswordFeedback} if an
     * exception occurs. Skips processing if all password fields are empty.
     * </p>
     * @param currentPassword The user’s current password for verification
     * @param newPassword The new password to set
     * @param confirmPassword The confirmation of the new password
     */

    private void handlePasswordUpdate(String currentPassword, String newPassword, String confirmPassword) {
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            try {
                if (!newPassword.equals(confirmPassword)) {
                    throw new IllegalArgumentException("Passwords do not match");
                }

                updatePassword(currentPassword, newPassword);
                passwordChanged = true;

            } catch (IllegalArgumentException e) {
                newPasswordFeedback.setText(e.getMessage());
            } catch (Exception e) {
                currentPasswordFeedback.setText(e.getMessage());
            }
        }
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    /**
     * Updates the user’s username if it has changed.
     * <p>
     * Checks if the new username differs from the current one, ensures it’s not already
     * taken using {@link UserDAO#getUser(String)}, and updates the user in the database
     * via {@link UserDAO#updateUser(User)}. Sets {@link #usernameChanged} to true if
     * the update occurs.
     * </p>
     * @param newUsername The new username to set
     * @throws IllegalArgumentException If the username is already taken
     * @throws SQLException If database operations fail
     */

    public void updateUsername(String newUsername) throws IllegalArgumentException, SQLException {
        if (!currentUser.getUsername().equals(newUsername)) {
            User existingUser = userDAO.getUser(newUsername);
            if (existingUser != null) {
                throw new IllegalArgumentException("Username is already taken");
            }

            currentUser.setUsername(newUsername);
            userDAO.updateUser(currentUser);
            usernameChanged = true;
        }
    }
    /**
     * Updates the user’s password after verifying the current password.
     * <p>
     * Validates the new password is not empty, verifies the current password using
     * {@link User#verifyPassword(String)}, hashes the new password with
     * {@link User#hashPassword(String)}, and updates the user in the database via
     * {@link UserDAO#updateUser(User)}. Sets {@link #passwordChanged} to true if
     * the update occurs.
     * </p>
     * @param currentPassword The user’s current password for verification
     * @param newPassword The new password to set
     * @throws IllegalArgumentException If the new password is empty
     * @throws SecurityException If the current password is incorrect
     * @throws SQLException If database operations fail
     */

    public void updatePassword(String currentPassword, String newPassword) throws IllegalArgumentException, SecurityException, SQLException {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        } else if (!currentUser.verifyPassword(currentPassword)) {
            throw new SecurityException("Incorrect Password");
        }

        String newPasswordHash = User.hashPassword(newPassword);
        currentUser.setPasswordHash(newPasswordHash);
        userDAO.updateUser(currentUser);
        passwordChanged = true;
    }
}
