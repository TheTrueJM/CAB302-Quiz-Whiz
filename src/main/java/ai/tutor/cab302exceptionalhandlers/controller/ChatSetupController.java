package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

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


    public ChatSetupController(SQLiteConnection db, User currentUser) throws RuntimeException, SQLException {
        this.db = db;
        this.currentUser = currentUser;

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


    @FXML
    protected void downloadChat() {
        Utils.showErrorAlert("Cannot download messages for chat that doesn't exist");
    }


    protected void chatReturn() throws IOException, RuntimeException, SQLException  {
        Utils.loadView("chat", new ChatController(db, currentUser), getStage());
    }
}
