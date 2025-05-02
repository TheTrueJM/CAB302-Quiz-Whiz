package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ChatSetupController {
    @FXML private ComboBox<String> responseAttitude;
    @FXML private ComboBox<String> educationLevel;
    @FXML private ComboBox<String> quizDifficulty;

    @FXML private Slider responseLength;
    @FXML private TextField chatNameInput;
    @FXML private TextField chatTopic;
    @FXML private Button startChatButton;
    @FXML private Button exitButton;
    @FXML private Pane backgroundOverlay;
    @FXML private Label settingsTitle;

    private final ChatController mainController;
    private final String operation;
    private final Chat selectedChat;
    private final SQLiteConnection db;

    public ChatSetupController(SQLiteConnection db, ChatController mainController, String operation, Chat selectedChat) throws SQLException {
        this.operation = operation;
        this.mainController = mainController;
        this.selectedChat = selectedChat;
        this.db = db;
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
        else {
            mainController.showErrorAlert("No chat selected for update.");
            cancel();
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
                mainController.updateChatDetails(selectedChat.getId(),chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), educationLevel.getValue(), chatTopic.getText());
                cancel();
            } catch (SQLException e ) {
                mainController.showErrorAlert("Error updating chat" + e);
            }
        });
    }

    private void cancel() {
        Object[] params = {db, mainController.getCurrentUser()};

        Stage stage = (Stage) startChatButton.getScene().getWindow();
        Utils.loadPage("chat-view.fxml", ChatController.class, stage, params);
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
