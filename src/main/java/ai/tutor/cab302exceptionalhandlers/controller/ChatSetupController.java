package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatSetupController {
    @FXML private ComboBox responseAttitude;
    @FXML private Slider responseLength;
    @FXML private TextField chatNameInput;
    @FXML private TextField chatTopic;
    @FXML private ComboBox educationLevel;
    @FXML private Button startChatButton;
    @FXML private ComboBox quizDifficulty;
    @FXML private Button exitButton;
    @FXML private Pane backgroundOverlay;
    @FXML private Label settingsTitle;

    private final SQLiteConnection db;
    private final User currentUser;
    private final ChatController mainController;
    private final String operation;
    private Chat selectedChat;
    // DAO fields...

    public ChatSetupController(SQLiteConnection db, User authenticatedUser, ChatController mainController, String operation, Chat selectedChat) throws SQLException {
        this.operation = operation;
        this.db = db;
        this.currentUser = authenticatedUser;
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
                mainController.createNewChat(chatNameInput.getText(), responseAttitude.getValue().toString(), quizDifficulty.getValue().toString(), educationLevel.getValue().toString(), chatTopic.getText());
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
                mainController.updateChatDetails(mainController.getSelectedChat().getId(),chatNameInput.getText(), responseAttitude.getValue().toString(), quizDifficulty.getValue().toString(), educationLevel.getValue().toString(), chatTopic.getText());
                cancel();
            } catch (SQLException e ) {
                mainController.showErrorAlert("Error creating chat" + e);
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

    private void setupBackgroundExit() {
        backgroundOverlay.setOnMouseClicked(actionEvent -> {
            cancel();
        });
    }
}
