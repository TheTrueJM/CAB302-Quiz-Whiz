package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import java.sql.SQLException;

public class ChatCreateController extends ChatSetupController {
    public ChatCreateController(SQLiteConnection db, User currentUser) throws RuntimeException, SQLException {
        super(db, currentUser);
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    @FXML
    public void initialize() {
        super.initialize();
    }


    // Move other methods related to chat-setup-view.fxml
    protected void setupConfirmChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                createNewChat(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), (int)quizLength.getValue(), educationLevel.getValue(), chatTopic.getText());
                chatReturn();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Creating Chat: " + e);
            }
        });
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    // Create a new Chat record using UI user input
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, int quizLength, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
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
        Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, quizLength, educationLevel, studyArea);
        chatDAO.createChat(newChat);

        return newChat;
    }
}
