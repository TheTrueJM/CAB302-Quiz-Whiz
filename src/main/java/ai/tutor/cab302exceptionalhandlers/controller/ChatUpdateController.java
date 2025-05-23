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

public class ChatUpdateController extends ChatSetupController {
    private final Chat currentChat;


    public ChatUpdateController(SQLiteConnection db, User currentUser, Chat currentChat) throws RuntimeException, SQLException {
        super(db, currentUser);
        this.currentChat = currentChat;
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    @FXML
    public void initialize() {
        super.initialize();
        configureChatSettings();
    }


    // Move other methods related to chat-setup-view.fxml
    protected void setupConfirmChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                updateChatDetails(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), (int)quizLength.getValue(), educationLevel.getValue(), chatTopic.getText());
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
        quizLength.setValue(currentChat.getQuizLength());
        educationLevel.setValue(currentChat.getEducationLevel());
        chatTopic.setText(currentChat.getStudyArea());
        startChatButton.setText("Update Chat");
    }


    // Saves a copy of the currently selected chat as a TXT file to directory of your choosing
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
