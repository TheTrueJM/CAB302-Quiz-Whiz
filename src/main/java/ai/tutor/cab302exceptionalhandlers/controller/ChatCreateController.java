package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import java.sql.SQLException;

/**
 * Controller for creating new chats in the AI tutor application.
 * <p>
 * Extends {@link ChatSetupController} to provide functionality for creating a new
 * chat based on user inputs from the chat setup screen. Implements
 * {@link #setupConfirmChatButton()} to handle chat creation and uses
 * {@link #createNewChat(String, String, String, int, String, String)} to save the chat
 * to the database via {@link #chatDAO}. After creation, navigates back to the chat screen.
 * </p>
 * @see ChatSetupController
 * @see ChatDAO
 * @see Chat
 * @see User
 */

public class ChatCreateController extends ChatSetupController {

    /**
     * Constructs a ChatCreateController with a database connection and authenticated user.
     * <p>
     * Calls the superclass constructor to initialize the database connection
     * ({@link #db}), current user ({@link #currentUser}), and DAO objects
     * ({@link #userDAO}, {@link #chatDAO}, {@link #messageDAO}).
     * </p>
     * @param db The SQLite database connection
     * @param currentUser The currently authenticated user
     * @throws IllegalStateException If the user is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public ChatCreateController(SQLiteConnection db, User currentUser) throws IllegalStateException, RuntimeException, SQLException {
        super(db, currentUser);
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    /**
     * Initializes the chat creation screen’s UI components and event handlers.
     * <p>
     * Calls the superclass’s {@link ChatSetupController#initialize()} method to set up
     * shared UI components and event handlers (e.g., exit button, background overlay).
     * </p>
     */

    @FXML
    public void initialize() {
        super.initialize();
    }

    @Override
    protected Chat getCurrentChat() {
        return null; // No chat exists in Create mode
    }



    /**
     * Configures the confirm chat button to create a new chat.
     * <p>
     * Sets up {@link #startChatButton} to create a new chat using user inputs from
     * {@link #chatNameInput}, {@link #responseAttitude}, {@link #quizDifficulty},
     * {@link #quizLength}, {@link #educationLevel}, and {@link #chatTopic}. Calls
     * {@link #createNewChat(String, String, String, int, String, String)} to save the
     * chat to the database and then navigates back to the chat screen using
     * {@link #chatReturn()}. Displays an error alert if creation fails.
     * </p>
     */
    @Override
    protected void setupConfirmChatButton() {
        startChatButton.setOnAction(actionEvent -> {
            try {
                createNewChat(chatNameInput.getText(), responseAttitude.getValue(), quizDifficulty.getValue(), (int)quizLength.getValue(), educationLevel.getValue(), chatTopic.getText());
                chatReturn();
            } catch (Exception e) {
                Utils.showErrorAlert("Error Creating Chat: " + e);
            }
        });
        downloadButton.setDisable(true); // Disable download in Create mode
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    /**
     * Creates a new chat record using user inputs from the UI.
     * <p>
     * Validates the chat name, response attitude, and quiz difficulty, then creates a
     * new {@link Chat} object and saves it to the database via {@link #chatDAO}.
     * Allows optional fields (education level and study area) to be null.
     * </p>
     * @param name The inputted chat name
     * @param responseAttitude The selected response attitude
     * @param quizDifficulty The selected quiz difficulty
     * @param quizLength The chosen quiz length
     * @param educationLevel The selected education level (optional)
     * @param studyArea The inputted study area (optional)
     * @return The newly created {@link Chat}
     * @throws IllegalArgumentException If the chat name, response attitude, or quiz difficulty is empty
     * @throws SQLException If database operations fail
     */

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
