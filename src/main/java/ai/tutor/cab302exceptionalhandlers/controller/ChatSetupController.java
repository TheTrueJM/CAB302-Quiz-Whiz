package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.control.*;
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

    private final ChatController mainController;
    private final String operation;
    private final SQLiteConnection db;
    private Chat selectedChat;

    public ChatSetupController(SQLiteConnection db, ChatController mainController, String operation, Chat selectedChat) throws SQLException {
        this.db = db;
        this.operation = operation;
        this.mainController = mainController;
        this.selectedChat = selectedChat;
    }

    @FXML
    public void initialize() {
        if (Objects.equals(operation, "Create")) {
            setupStartChatButton();
        }
        else if ((Objects.equals(operation, "Update"))) {
            configureChatSettings();
            setupUpdateChatButton();
        }
        setupExitButton();
        setupBackgroundExit();
    }

    // Move other methods related to chat-setup-view.fxml
    private void setupStartChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                mainController.createNewChat(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), educationLevel.getValue(), chatTopic.getText());
                cancel();
            } catch (SQLException e ) {
                mainController.showErrorAlert("Error creating chat" + e);
            }
        });
    }

    private void configureChatSettings(){
        settingsTitle.setText("Chat Settings");
        chatNameInput.setText(selectedChat.getName());
        responseAttitude.setValue(selectedChat.getResponseAttitude());
        quizDifficulty.setValue(selectedChat.getQuizDifficulty());
        educationLevel.setValue(selectedChat.getEducationLevel());
        chatTopic.setText(selectedChat.getStudyArea());
        startChatButton.setText("Update Chat");

    }

    private void setupUpdateChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                mainController.updateChatDetails(mainController.getSelectedChat().getId(),chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), educationLevel.getValue(), chatTopic.getText());
                cancel();
            } catch (SQLException e ) {
                mainController.showErrorAlert("Error updating chat" + e);
            }
        });
    }

    private void cancel() {
        try {
            // Load the previous FXML (chat-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(
                    QuizWhizApplication.class.getResource("chat-view.fxml")
            );
            fxmlLoader.setController(mainController);

            // Create the scene
            Scene previousScene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);

            // Get the current Stage
            Stage stage = (Stage) startChatButton.getScene().getWindow();
            stage.setScene(previousScene);

        } catch (IOException e) {
            mainController.showErrorAlert("Failed to return to chat view:" + e.getMessage());
        }
    }

    private void setupExitButton() {
        exitButton.setOnAction(actionEvent -> {
            cancel();
        });
    }

    // Saves a copy of the currently selected chat as a TXT file to directory of your choosing
    @FXML
    private void downloadChat() {

        try {
            // Initialize MessageDAO
            MessageDAO messageDAO = new MessageDAO(db);

            // Get messages for the current chat
            List<Message> messages = messageDAO.getAllChatMessages(selectedChat.getId());

            if (messages.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Messages", "This chat has no messages to download.");
                return;
            }

            // Create file content
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("Chat: ").append(selectedChat.getName()).append("\n\n");
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
            fileChooser.setInitialFileName("chat_" + selectedChat.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_messages.txt");
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


    private void setupBackgroundExit() {
        backgroundOverlay.setOnMouseClicked(actionEvent -> {
            cancel();
        });
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
}



}
