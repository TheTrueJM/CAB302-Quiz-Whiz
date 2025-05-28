package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.SceneManager;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Abstract base class for authentication controllers in the AI tutor application.
 * <p>
 * Provides common functionality for handling login and sign-up forms, including
 * input validation, UI feedback, and navigation. Subclasses ({@link LoginController},
 * {@link SignUpController}) implement specific authentication logic for logging in
 * or creating users. Interacts with the database via {@link UserDAO} and manages
 * scene transitions using {@link SceneManager}.
 * </p>
 * @see LoginController
 * @see SignUpController
 * @see UserDAO
 * @see SceneManager
 */

public abstract class AuthController {
    protected final SQLiteConnection db;
    protected final UserDAO userDAO;

    protected boolean usernameEmpty = true;
    protected boolean passwordEmpty = true;

    @FXML
    protected TextField usernameField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected PasswordField confirmPasswordField;

    @FXML
    protected Button submitButton;

    @FXML
    protected Label usernameFeedback;
    @FXML
    protected Label passwordFeedback;


    @FXML protected Button switchLayout;

    /**
     * Constructs an AuthController with a database connection.
     * <p>
     * Initializes the {@link #db} connection and creates a {@link UserDAO} instance
     * for user database operations.
     * </p>
     * @param db The SQLite database connection
     * @throws SQLException If database initialization fails
     * @throws RuntimeException If unexpected errors occur during setup
     */


    public AuthController(SQLiteConnection db) throws SQLException, RuntimeException {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */


    /**
     * Initializes the authentication form’s UI components and event handlers.
     * <p>
     * Sets up the switch layout button ({@link #setupSwitchLayoutButton()}),
     * submit button ({@link #setupSubmitButton()}), and input fields
     * ({@link #setupInputField()}) for user interaction. Called automatically
     * by JavaFX when the FXML is loaded.
     * </p>
     */

    @FXML
    public void initialize() {
        setupSwitchLayoutButton();
        setupSubmitButton();
        setupInputField();
    }


    /**
     * Handles key events in the authentication form’s input fields.
     * <p>
     * Abstract method to be implemented by subclasses to track changes in
     * {@link #usernameField}, {@link #passwordField}, or {@link #confirmPasswordField}
     * and update the submit button’s state.
     * </p>
     * @param e The key event triggered by typing in an input field
     */

    @FXML
    protected abstract void onFieldChanged(KeyEvent e);


    /**
     * Enables or disables the submit button based on input field states.
     * <p>
     * Abstract method to be implemented by subclasses to control the
     * {@link #submitButton} based on whether required fields are filled.
     * </p>
     */

    protected abstract void submitButtonToggle();

    /**
     * Displays error feedback in the specified label.
     * <p>
     * Sets the text of the given feedback label (e.g., {@link #usernameFeedback},
     * {@link #passwordFeedback}) to the provided error message.
     * </p>
     * @param feedbackLabel The label to display the error message
     * @param message The error message to show
     */

    protected void errorFeedback(Label feedbackLabel, String message) {
        feedbackLabel.setText(message);
    }

    /**
     * Clears error feedback from all feedback labels.
     * <p>
     * Resets the text of {@link #usernameFeedback} and {@link #passwordFeedback}
     * to empty strings, clearing any previous error messages.
     * </p>
     */

    protected void resetErrorFeedback() {
        usernameFeedback.setText("");
        passwordFeedback.setText("");
    }


    /**
     * Processes the authentication form submission.
     * <p>
     * Abstract method to be implemented by subclasses to handle form submission,
     * such as validating input and authenticating the user.
     * </p>
     */

    @FXML
    protected abstract void onSubmit();

    /**
     * Navigates to the chat screen for the authenticated user.
     * <p>
     * Uses {@link SceneManager} to load the chat interface for the specified user.
     * </p>
     * @param user The authenticated {@link User} to load the chat for
     * @throws IllegalStateException If the user is not specified
     * @throws RuntimeException If database connection fails
     * @throws SQLException If database operations fail
     * @throws IOException If loading the chat screen fails
     */

    public void loadChat(User user) throws IllegalStateException, RuntimeException, SQLException, IOException {
        SceneManager.getInstance().navigateToChat(user);
    }

    /**
     * Switches between login and sign-up screens.
     * <p>
     * Determines the target screen based on the current controller type
     * (e.g., from {@link LoginController} to {@link SignUpController} or vice versa)
     * and uses {@link SceneManager} to navigate to the appropriate authentication screen.
     * </p>
     * @throws IOException If loading the target screen fails
     * @throws SQLException If database operations fail
     * @throws RuntimeException If unexpected errors occur
     */

    @FXML
    protected void switchLayout() throws Exception {
        AuthType targetType = this instanceof LoginController ? AuthType.SIGNUP : AuthType.LOGIN;
        SceneManager.getInstance().navigateToAuth(targetType);
    }

    /**
     * Configures the switch layout button to handle navigation between login and sign-up screens.
     * <p>
     * Attaches an event handler to {@link #switchLayout} that calls {@link #switchLayout()}
     * when clicked, with error handling to display alerts for failures.
     * </p>
     */

    @FXML
    // Set up event handler for switch to login/signup button
    protected void setupSwitchLayoutButton() {
        if (switchLayout != null) {
            switchLayout.setOnAction(event -> {
                try {
                    switchLayout();
                } catch (Exception e) {
                    Utils.showErrorAlert("Failed to switch pages" + e.getMessage());
                }
            });
        }
    }

    /**
     * Configures the submit button to handle form submission.
     * <p>
     * Attaches an event handler to {@link #submitButton} that calls {@link #onSubmit()}
     * when clicked, with error handling to display alerts for failures.
     * </p>
     */

    @FXML
    // Set up event handler for submit button
    protected void setupSubmitButton() {
        if (submitButton != null) {
            submitButton.setOnAction(event -> {
                onSubmit();
            });
        }
    }

    /**
     * Configures input fields to respond to key events.
     * <p>
     * Attaches event handlers to {@link #usernameField} and {@link #passwordField}
     * that call {@link #onFieldChanged(KeyEvent)} when the user types, enabling
     * real-time input validation.
     * </p>
     */

    @FXML
    protected void setupInputField() {
        if (usernameField != null) {
            usernameField.setOnKeyReleased(this::onFieldChanged);
        }
        if (passwordField != null) {
            passwordField.setOnKeyReleased(this::onFieldChanged);
        }
    }

    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    /**
     * Authenticates a user with the provided username and password.
     * <p>
     * Abstract method to be implemented by subclasses to validate credentials
     * (for login) or create a new user (for sign-up) using {@link UserDAO}.
     * </p>
     * @param username The username to authenticate
     * @param password The password to validate or hash
     * @return The authenticated or newly created {@link User} object
     * @throws IllegalArgumentException If the username or password is invalid
     * @throws SecurityException If authentication fails (e.g., incorrect password)
     * @throws SQLException If database operations fail
     */

    public abstract User authenticateUser(String username, String password) throws IllegalArgumentException, SecurityException, SQLException;
}
