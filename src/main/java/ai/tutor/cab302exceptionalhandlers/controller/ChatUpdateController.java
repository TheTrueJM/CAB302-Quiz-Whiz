package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
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
import java.util.NoSuchElementException;

/**
 * Controller for updating existing chats in the AI tutor application.
 * <p>
 * Extends {@link ChatSetupController} to provide functionality for updating an existing
 * chat’s details (e.g., chat name, response attitude, quiz difficulty) and downloading
 * chat messages as a text file. Implements the abstract method
 * {@link #setupConfirmChatButton()} to handle chat updates and navigation back to the
 * chat screen. Interacts with the database via {@link ChatDAO} and {@link MessageDAO}
 * for updating chats and retrieving messages, respectively.
 * </p>
 * @see ChatSetupController
 * @see ChatDAO
 * @see MessageDAO
 * @see Chat
 * @see User
 */

public class ChatUpdateController extends ChatSetupController {


    private final Chat currentChat;


    /**
     * Constructs a ChatUpdateController with a database connection, authenticated user, and the chat to update.
     * <p>
     * Calls the superclass constructor to initialize the database connection, current user,
     * and DAO instances ({@link UserDAO}, {@link ChatDAO}, {@link MessageDAO}). Sets the
     * {@link #currentChat} to be updated. Throws an exception if the user is null.
     * </p>
     * @param db The SQLite database connection
     * @param currentUser The currently authenticated user
     * @param currentChat The chat to be updated
     * @throws IllegalStateException If the user is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public ChatUpdateController(SQLiteConnection db, User currentUser, Chat currentChat) throws IllegalStateException, RuntimeException, SQLException {
        super(db, currentUser);
        this.currentChat = currentChat;
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    /**
     * Initializes the chat update screen’s UI components and event handlers.
     * <p>
     * Calls the superclass’s {@link ChatSetupController#initialize()} to set up shared
     * functionality (e.g., exit button, background exit), then configures the UI with
     * existing chat details using {@link #configureChatSettings()}.
     * </p>
     */

    @FXML
    public void initialize() {
        super.initialize();
        configureChatSettings();
    }

    /**
     * Retrieves the current chat being updated.
     * <p>
     * Overrides the abstract {@link ChatSetupController#getCurrentChat()} method to provide
     * the chat context for the update mode. Returns the {@code currentChat} field, which is
     * set by the subclass when initializing the update interface.
     * </p>
     * @return The current {@link Chat} object being updated, or null if not set
     * @see ChatSetupController#getCurrentChat()
     */

    @Override
    protected Chat getCurrentChat() {
        return currentChat; // Return the current chat in Update mode
    }


    /**
     * Sets up the confirm chat button to update the current chat.
     * <p>
     * Configures {@link ChatSetupController#startChatButton} to update the chat using
     * {@link #updateChatDetails(String, String, String, int, String, String)} with user
     * inputs from the UI, then navigates back to the chat screen via
     * {@link ChatSetupController#chatReturn()}. Displays an error alert if the update fails.
     * </p>
     */

    @Override
    protected void setupConfirmChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                updateChatDetails(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), (int)quizLength.getValue(), educationLevel.getValue(), chatTopic.getText());
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Updating Chat: " + e);
            }
        });
        downloadButton.setDisable(false); // Enable download in Update mode
    }

    /**
     * Configures the UI with the current chat’s settings.
     * <p>
     * Populates UI components (e.g., {@link ChatSetupController#chatNameInput},
     * {@link ChatSetupController#responseAttitude}) with the details of {@link #currentChat}.
     * Updates the {@link ChatSetupController#settingsTitle} and
     * {@link ChatSetupController#startChatButton} text to reflect the update context.
     * </p>
     */

    private void configureChatSettings() {
        settingsTitle.setText("Chat Settings");
        chatNameInput.setText(currentChat.getName());
        responseAttitude.setValue(currentChat.getResponseAttitude());
        quizDifficulty.setValue(currentChat.getQuizDifficulty());
        quizLength.setValue(currentChat.getQuizLength());
        educationLevel.setValue(currentChat.getEducationLevel());
        chatTopic.setText(currentChat.getStudyArea());
        startChatButton.setText("Update Chat");
        downloadButton.setDisable(false);
    }


    /**
     * Initiates the download of chat messages for the current chat as a text file.
     * <p>
     * Checks if a chat is selected using {@link #getCurrentChat()}, retrieves all messages for the
     * chat using {@link MessageDAO}, formats them into a text file, and prompts the user to save
     * it using a {@link FileChooser}. Displays alerts for no chat selected, no messages, or
     * successful/failed operations. Subclasses are responsible for providing the chat context.
     * </p>
     */

    @Override
    @FXML
    protected void downloadChat() {
        try {
            // Get messages for the current chat
            List<Message> messages = messageDAO.getAllChatMessages(currentChat.getId());

            if (messages.isEmpty()) {
                Utils.showInfoAlert("This chat has no messages to download."); // Header: No Messages
                return;
            }

            // Create file content
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("Chat: ").append(currentChat.getName()).append("\n\n");
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
            fileChooser.setInitialFileName("chat_" + currentChat.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_messages.txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

            if (file != null) {
                // Write to file
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(fileContent.toString());
                }
                Utils.showInfoAlert("Chat messages downloaded successfully!"); // Header: Success
            }

        } catch (SQLException e) {
            Utils.showErrorAlert( "Failed to retrieve messages: " + e.getMessage()); // Header: Database Error
        } catch (IOException e) {
            Utils.showErrorAlert("Failed to save file: " + e.getMessage()); // Header: File Error
        }
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    /**
     * Updates the details of the current chat.
     * <p>
     * Validates the required inputs (chat name, response attitude, quiz difficulty), updates
     * the {@link #currentChat} object with new values, and saves the changes to the database
     * using {@link ChatSetupController#chatDAO}. Optional fields (education level, study area)
     * are set to null if empty.
     * </p>
     * @param name The updated name of the chat
     * @param responseAttitude The updated AI response attitude (e.g., formal, casual)
     * @param quizDifficulty The updated quiz difficulty level (e.g., easy, medium, hard)
     * @param quizLength The updated number of questions in a quiz
     * @param educationLevel The updated user education level (optional, can be null)
     * @param studyArea The updated user study area or topic (optional, can be null)
     * @throws IllegalArgumentException If the chat name, response attitude, or quiz difficulty is empty
     * @throws NoSuchElementException If the chat does not exist in the database
     * @throws SQLException If database operations fail
     */

    public void updateChatDetails(String name, String responseAttitude, String quizDifficulty, int quizLength, String educationLevel, String studyArea) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (Utils.validateNullOrEmpty(name)) {
            throw new IllegalArgumentException("Chat name attitude cannot be empty");
        }
        if (Utils.validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (Utils.validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (Utils.validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (Utils.validateNullOrEmpty(studyArea)) { studyArea = null; }


        currentChat.setName(name);
        currentChat.setResponseAttitude(responseAttitude);
        currentChat.setQuizDifficulty(quizDifficulty);
        currentChat.setQuizLength(quizLength);
        currentChat.setEducationLevel(educationLevel);
        currentChat.setStudyArea(studyArea);
        chatDAO.updateChat(currentChat);
    }
}
