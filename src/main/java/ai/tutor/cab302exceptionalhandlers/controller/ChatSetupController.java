package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
     * Initiates the download of chat messages for the current chat as a text file.
     * <p>
     * Checks if a chat is selected using {@link #getCurrentChat()}, retrieves all messages for the
     * chat using {@link MessageDAO}, formats them into a text file, and prompts the user to save
     * it using a {@link FileChooser}. Displays alerts for no chat selected, no messages, or
     * successful/failed operations. Subclasses are responsible for providing the chat context.
     * </p>
     * @throws SQLException If database operations fail while retrieving messages
     * @throws IOException If file writing operations fail
     */

    @FXML
    protected void downloadChat() {
        try {
            // Check if a chat is available (subclasses should set this context)
            if (getCurrentChat() == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Chat Selected", "Cannot download messages for a chat that doesn't exist.");
                return;
            }

            // Get messages for the current chat
            List<Message> messages = messageDAO.getAllChatMessages(getCurrentChat().getId());

            if (messages.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Messages", "This chat has no messages to download.");
                return;
            }

            // Create file content
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("Chat: ").append(getCurrentChat().getName()).append("\n\n");
            for (Message message : messages) {
                String sender = message.getFromUser() ? "User" : "AI";
                fileContent.append(sender)
                        .append(": ")
                        .append(message.getContent())
                        .append(" [Quiz: ")
                        .append(message.getIsQuiz() ? "Yes" : "No")
                        .append("]\n");
            }

            // Open file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Chat Messages");
            fileChooser.setInitialFileName("chat_" + getCurrentChat().getName().replaceAll("[^a-zA-Z0-9]", "_") + "_messages.txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

            if (file != null) {
                // Write to file
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(fileContent.toString());
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Chat messages downloaded successfully!");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve messages: " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Failed to save file: " + e.getMessage());
        }
    }

    /**
     * Abstract method to get the current chat context.
     * <p>
     * Subclasses must implement this method to provide the chat being edited or viewed,
     * or return null if no chat is selected (e.g., in Create mode).
     * </p>
     * @return The current {@link Chat} object, or null if no chat is selected
     */
    protected abstract Chat getCurrentChat();

    /**
     * Navigates back to the chat screen for the current user.
     * <p>
     * Uses {@link SceneManager} to transition to the chat interface, passing the current user.
     * Any exceptions during navigation are propagated to the caller.
     * </p>
     * @throws Exception If navigation or scene management fails
     * @see SceneManager#navigateToChat(User)
     */

    protected void chatReturn() throws Exception {
        SceneManager.getInstance().navigateToChat(currentUser);
    }

    /**
     * Displays a dialog alert with the specified type, title, and message.
     * <p>
     * Creates and shows a JavaFX {@link Alert} dialog with the given parameters. The alert
     * is modal and waits for user interaction before proceeding.
     * </p>
     * @param type The type of alert (e.g., INFORMATION, ERROR)
     * @param title The title of the alert dialog
     * @param message The content message to display
     */

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
