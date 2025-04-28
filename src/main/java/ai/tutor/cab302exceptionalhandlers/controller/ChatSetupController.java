package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.ChatDAO;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    private final SQLiteConnection db;
    private final User currentUser;
    private final ChatController controller;
    // DAO fields...

    public ChatSetupController(SQLiteConnection db, User authenticatedUser) throws SQLException {
        this.db = db;
        this.currentUser = authenticatedUser;
        this.controller = new ChatController(db, currentUser);
    }

    @FXML
    public void initialize() {
        setupStartChatButton();
        setupExitButton();
        setupBackgroundExit();
    }

    // Move other methods related to chat-setup-view.fxml
    public void setupStartChatButton() {
        List<String> responseLengths = new ArrayList<String>(){{
            add("Concise");
            add("Moderate");
            add("Detailed");
        }};
        startChatButton.setOnAction(actionEvent -> {
            try {
                controller.createNewChat(chatNameInput.getText(), responseAttitude.getValue().toString(), responseLengths.get((int) responseLength.getValue()), educationLevel.getValue().toString(), chatTopic.getText());
                cancel();
            } catch (SQLException e ) {
                controller.showErrorAlert("Error creating chat" + e);
            }

        });
    }

    private void cancel() {
        try {
            // Load the previous FXML (chat-view.fxml)
            FXMLLoader fxmlLoader = new FXMLLoader(
                    QuizWhizApplication.class.getResource("chat-view.fxml")
            );
            ChatController controller = new ChatController(db, currentUser); // Or ChatViewController if split
            fxmlLoader.setController(controller);

            // Create the scene
            Scene previousScene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);

            // Get the current Stage
            Stage stage = (Stage) startChatButton.getScene().getWindow();
            stage.setScene(previousScene);
        } catch (IOException | SQLException e) {
            controller.showErrorAlert("Failed to return to chat view:" + e.getMessage());
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
