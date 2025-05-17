package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javafx.stage.FileChooser;

import java.util.List;

public class ChatSetupController {
    @FXML private ComboBox<String> responseAttitude;
    @FXML private ComboBox<String> educationLevel;
    @FXML private ComboBox<String> quizDifficulty;
    @FXML private Slider responseLength;
    @FXML private TextField chatNameInput;
    @FXML private TextField chatTopic;
    @FXML private Button startChatButton;
    @FXML private Button exitButton;
    @FXML private Button downloadButton;
    @FXML private Pane backgroundOverlay;
    @FXML private Label settingsTitle;


    private final SQLiteConnection db;
    private final Chat currentChat;
    private final User currentUser;

    private final UserDAO userDAO;
    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;


    public ChatSetupController(SQLiteConnection db, User currentUser, Chat currentChat) throws RuntimeException, SQLException {
        this.db = db;
        this.currentUser = currentUser;
        this.currentChat = currentChat;

        this.userDAO = new UserDAO(db);
        this.chatDAO = new ChatDAO(db);
        this.messageDAO = new MessageDAO(db);
    }

    protected Stage getStage() {
        return (Stage) settingsTitle.getScene().getWindow();
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    @FXML
    public void initialize() {
        if (currentChat == null) {
            setupStartChatButton();
        }
        else {
            setupUpdateChatButton();
            configureChatSettings();
        }

        setupExitButton();
        setupBackgroundExit();
    }


    // Move other methods related to chat-setup-view.fxml
    private void setupStartChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                createNewChat(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), educationLevel.getValue(), chatTopic.getText());
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Creating Chat: " + e);
            }
        });
    }

    private void setupUpdateChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                updateChatDetails(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), educationLevel.getValue(), chatTopic.getText());
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Updating Chat: " + e);
            }
        });
    }

    private void configureChatSettings() {
        settingsTitle.setText("Chat Settings");
        chatNameInput.setText(currentChat.getName());
        responseAttitude.setValue(currentChat.getResponseAttitude());
        quizDifficulty.setValue(currentChat.getQuizDifficulty());
        educationLevel.setValue(currentChat.getEducationLevel());
        chatTopic.setText(currentChat.getStudyArea());
        startChatButton.setText("Update Chat");
    }

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


    // Saves a copy of the currently selected chat as a TXT file to directory of your choosing
    @FXML
    private void downloadChat() {
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


    private void chatReturn() throws IOException, RuntimeException, SQLException  {
        Utils.loadView("chat", new ChatController(db, currentUser), getStage());
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    // Create a new Chat record using UI user input
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
        if (Utils.validateNullOrEmpty(name)) {
            throw new IllegalArgumentException("Chat name cannot be empty");
        }
        if (Utils.validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (Utils.validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (Utils.validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (Utils.validateNullOrEmpty(studyArea)) { studyArea = null; }

        // Create and Add Chat to database
        Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, educationLevel, studyArea);
        chatDAO.createChat(newChat);

        return newChat;
    }


    public void updateChatDetails(String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, NoSuchElementException, SQLException {
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
        currentChat.setResponseAttitude(educationLevel);
        currentChat.setStudyArea(studyArea);
        chatDAO.updateChat(currentChat);
    }
}
