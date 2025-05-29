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

    @FXML
    public void initialize() {
        setupConfirmChatButton();
        setupExitButton();
        setupBackgroundExit();
    }


    protected abstract void setupConfirmChatButton();

    private void setupExitButton() {
        exitButton.setOnAction(actionEvent -> {
            try {
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Exiting Chat Setup: " + e);
            }
        });
    }

    private void setupBackgroundExit() {
        backgroundOverlay.setOnMouseClicked(actionEvent -> {
            try {
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Exiting Chat Setup: " + e);
            }
        });
    }

    // TODO: JAVADOCS HERE
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

    protected void chatReturn() throws Exception {
        SceneManager.getInstance().navigateToChat(currentUser);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
