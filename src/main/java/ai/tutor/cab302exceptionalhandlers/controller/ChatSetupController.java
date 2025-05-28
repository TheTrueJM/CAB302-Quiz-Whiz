package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Abstract base class for managing the chat setup screen in the AI tutor application.
 * <p>
 * Provides shared functionality for creating or updating chats, including UI components
 * for configuring chat settings (e.g., response attitude, quiz difficulty) and navigation
 * back to the chat screen. Subclasses must implement {@link #setupConfirmChatButton()}
 * to handle chat creation or updates. Interacts with the database via {@link UserDAO},
 * {@link ChatDAO}, and {@link MessageDAO}, and uses {@link SceneManager} for navigation.
 * </p>
 * @see UserDAO
 * @see ChatDAO
 * @see MessageDAO
 * @see SceneManager
 * @see User
 */

public abstract class ChatSetupController {
    @FXML protected ComboBox<String> responseAttitude;
    @FXML protected ComboBox<String> educationLevel;
    @FXML protected ComboBox<String> quizDifficulty;
    @FXML protected Slider quizLength;
    @FXML protected Button downloadButton;
    @FXML protected TextField chatNameInput;
    @FXML protected TextField chatTopic;
    @FXML protected Button startChatButton;
    @FXML protected Button exitButton;
    @FXML protected Pane backgroundOverlay;
    @FXML protected Label settingsTitle;


    protected final SQLiteConnection db;
    protected final User currentUser;

    protected final UserDAO userDAO;
    protected final ChatDAO chatDAO;
    protected final MessageDAO messageDAO;

    /**
     * Constructs a ChatSetupController with a database connection and authenticated user.
     * <p>
     * Initializes the {@link #db} connection, sets the {@link #currentUser}, and creates
     * instances of {@link UserDAO}, {@link ChatDAO}, and {@link MessageDAO}. Throws an
     * exception if the user is null.
     * </p>
     * @param db The SQLite database connection
     * @param authenticatedUser The currently authenticated user
     * @throws IllegalStateException If the user is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public ChatSetupController(SQLiteConnection db, User authenticatedUser) throws IllegalStateException, RuntimeException, SQLException {
        if (authenticatedUser == null) {
            throw new IllegalStateException("No user was authenticated");
        }

        this.db = db;
        this.currentUser = authenticatedUser;

        this.userDAO = new UserDAO(db);
        this.chatDAO = new ChatDAO(db);
        this.messageDAO = new MessageDAO(db);
    }

    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    /**
     * Initializes the chat setup screen’s UI components and event handlers.
     * <p>
     * Sets up the confirm chat button ({@link #setupConfirmChatButton()}), exit button
     * ({@link #setupExitButton()}), and background exit functionality
     * ({@link #setupBackgroundExit()}). Called automatically by JavaFX when the FXML
     * is loaded.
     * </p>
     */

    @FXML
    public void initialize() {
        setupConfirmChatButton();
        setupExitButton();
        setupBackgroundExit();
    }

    /**
     * Abstract method to set up the confirm chat button.
     * <p>
     * Subclasses must implement this method to define the behavior of
     * {@link #startChatButton}, typically handling chat creation or updates.
     * </p>
     */

    protected abstract void setupConfirmChatButton();

    /**
     * Configures the exit button to return to the chat screen.
     * <p>
     * Sets up {@link #exitButton} to trigger {@link #chatReturn()} on click, with error
     * handling to display alerts for failures.
     * </p>
     */

    private void setupExitButton() {
        exitButton.setOnAction(actionEvent -> {
            try {
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Exiting Chat Setup: " + e);
            }
        });
    }

    /**
     * Configures the background overlay to return to the chat screen on click.
     * <p>
     * Sets up {@link #backgroundOverlay} to trigger {@link #chatReturn()} when clicked,
     * with error handling to display alerts for failures.
     * </p>
     */

    private void setupBackgroundExit() {
        backgroundOverlay.setOnMouseClicked(actionEvent -> {
            try {
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Exiting Chat Setup: " + e);
            }
        });
    }

    /**
     * Handles the download chat action (not functional in setup).
     * <p>
     * Displays an error alert indicating that downloading messages is not possible
     * for a chat that doesn’t yet exist.
     * </p>
     */

    @FXML
    protected void downloadChat() {
        Utils.showErrorAlert("Cannot download messages for chat that doesn't exist");
    }

    /**
     * Handles the download chat action (not functional in setup).
     * <p>
     * Displays an error alert indicating that downloading messages is not possible
     * for a chat that doesn’t yet exist.
     * </p>
     */

    protected void chatReturn() throws Exception {
        SceneManager.getInstance().navigateToChat(currentUser);
    }
}
