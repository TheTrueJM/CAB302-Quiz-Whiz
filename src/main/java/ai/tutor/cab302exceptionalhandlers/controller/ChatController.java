package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class ChatController {
    @FXML
    private ListView<Chat> chatsListView;
    @FXML
    private ListView<Message> messagesListView;
    @FXML
    private TextField chatNameField;
    @FXML
    private Button updateChatButton;
    @FXML
    private TextField messageInputField;

    private User currentUser;
    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private AnswerOptionDAO answerOptionDAO;

    public ChatController(SQLiteConnection db, User authenticatedUser) throws IllegalStateException {
        if (authenticatedUser == null) {
            throw new IllegalStateException("No user was authenticated");
        }

        try {
            this.currentUser = authenticatedUser;
            this.userDAO = new UserDAO(db);
            this.chatDAO = new ChatDAO(db);
            this.messageDAO = new MessageDAO(db);
            this.quizDAO = new QuizDAO(db);
            this.quizQuestionDAO = new QuizQuestionDAO(db);
            this.answerOptionDAO = new AnswerOptionDAO(db);
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        populateChatsListView();
        setupChatSelectionListener();
        setupMessagesListView();
        setupUpdateChatNameButton();
    }

    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */
    private void showErrorAlert (String message){
        // Create error alert object
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void populateChatsListView () {
        try {
            chatsListView.getItems().clear();
            chatsListView.getItems().addAll(chatDAO.getAllUserChats(currentUser.getId()));
        } catch (SQLException e) {
            showErrorAlert("Failed to load chats: " + e.getMessage());
        }
    }

    private void setupChatSelectionListener() {
        chatsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                try {
                    messagesListView.getItems().clear();
                    messagesListView.getItems().addAll(messageDAO.getAllChatMessages(newChat.getId()));
                    chatNameField.setText(newChat.getName());
                } catch (SQLException e) {
                    showErrorAlert("Failed to load messages: " + e.getMessage());
                }
            } else {
                chatNameField.setText("");
                messagesListView.getItems().clear();
            }
        });
    }

    private void setupMessagesListView() {
        messagesListView.setCellFactory(listView -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    setText(message.getContent());
                    //TODO: Remember to Style message cells based on sender in css
                    if (message.getFromUser()) {
                        getStyleClass().add("user-message");
                    } else {
                        getStyleClass().add("ai-message");
                    }
                    // Handle special AI Quiz messages
                    if (!message.getFromUser() && message.getIsQuiz()) {
                        Button takeQuizButton = new Button("Take Quiz");
                        takeQuizButton.setOnAction(event -> {
                            // TODO: Logic to handle quiz action
                        });
                        setGraphic(takeQuizButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    public void sendAndReceiveMessage() {
        Chat selectedChat = chatsListView.getSelectionModel().getSelectedItem();
        if (selectedChat == null) {
            showErrorAlert("No chat selected");
            return;
        }
        String content = messageInputField.getText();
        if (content == null || content.trim().isEmpty()) {
            showErrorAlert("Message cannot be empty");
            return;
        }
        try {
            // Create a new user message and save in database
            Message userMessage = createNewChatMessage(selectedChat.getId(), content, true, false);

            // Refresh the messages list
            messagesListView.getItems().clear();
            messagesListView.getItems().addAll(messageDAO.getAllChatMessages(selectedChat.getId()));

            // Clear the input field
            messageInputField.clear();

            //TODO: Pass message prompt to AI
            generateChatMessageResponse(userMessage);

            // Refresh again to show the AI response
            messagesListView.getItems().clear();
            messagesListView.getItems().addAll(messageDAO.getAllChatMessages(selectedChat.getId()));
        } catch (SQLException e) {
            showErrorAlert("Failed to send message: " + e.getMessage());
        }
    }

    private void setupUpdateChatNameButton() {
        updateChatButton.setOnAction(event -> {
            Chat selectedChat = chatsListView.getSelectionModel().getSelectedItem();
            String newName = chatNameField.getText();
            if (selectedChat == null) {
                showErrorAlert("No chat selected");
                return;
            }
            updateChatName(selectedChat.getId(), newName); // Let updateChatName handle validation
            chatNameField.setText(selectedChat.getName());
        });
    }

    public void handleCreateChatButton() {
        // TODO: Create chat based on parameters extracted from UI elements and refresh page
    }

    /*
     * =========================================================================
     *                          CRUD Operations
     * =========================================================================
     */
    // Create a Message object using UI user input
    public Message createNewChatMessage(int chatId, String content, boolean fromUser, boolean isQuiz) {
        try {
            Message userMessage = new Message(chatId, content, fromUser, isQuiz);
            messageDAO.createMessage(userMessage);
            return userMessage;
        } catch (SQLException e) {
            showErrorAlert("Failed to create message: " + e.getMessage());
            return null;
        }
    }

    // Create a Message object from the AI's response output using a user's Message object as input
    // If AI generation fails, create the Message object with default feedback content
    public Message generateChatMessageResponse(Message userMessage) {
        if (!userMessage.getFromUser()) {
            System.err.println("Chat message is from user");
            return null;
        }
        try {
            // TODO: Generate AI message
            String aiMessageContent = "I received your message";
            Message aiReponse = new Message(userMessage.getId(), aiMessageContent, false, userMessage.getIsQuiz());

            if (aiMessageContent == null) {
                aiMessageContent = "Default message";
            }
            //TODO: Operation to split the message for quiz if needed
            if (userMessage.getIsQuiz()) {
                createNewQuiz(aiMessageContent, aiReponse);
            }
            messageDAO.createMessage(aiReponse);
            return aiReponse;
        } catch (SQLException e) {
            return new Message(userMessage.getId(), "Failed to create message: " + e.getMessage(), false, false);
        }
    }

    // Create a new Chat record using UI user input
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return null;
        }
        if (responseAttitude == null || responseAttitude.trim().isEmpty()) {
            System.err.println("responseAttitude name cannot be empty");
            return null;
        }
        if (quizDifficulty == null || quizDifficulty.trim().isEmpty()) {
            System.err.println("Chat quizDifficulty cannot be empty");
            return null;
        }
        if (educationLevel == null || educationLevel.trim().isEmpty()) {
            educationLevel = null;
        }
        if (studyArea == null || studyArea.trim().isEmpty()) {
            studyArea = null;
        }

        try {
            // Create new chat object
            Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, educationLevel, studyArea);
            // Add new chat to database
            chatDAO.createChat(newChat);
            return newChat;
        } catch (SQLException e) {
            System.err.println("Failed to create chat: " + e.getMessage());
            return null;
        }
    }

    // Retrieve Chat records for a specific User
    public List<Chat> getUserChats() {
        try {
            return chatDAO.getAllUserChats(currentUser.getId());
        } catch (SQLException e) {
            System.err.println("Failed to read chats: " + e.getMessage());
            return null;
        }
    }

    // Retrieve a specific Chat record
    public Chat getChat(int chatId) {
        if (chatId < 0) {
            System.err.println("Chat id cannot be negative");
            return null;
        }
        try {
            Chat chat = chatDAO.getChat(chatId);
            return currentUser.getId() == chat.getUserId() ? chat : null;
        } catch (SQLException e) {
            System.err.println("Failed to read chat: " + e.getMessage());
            return null;
        }
    }

    // Update the details of a specific Chat record
    public boolean updateChatDetails(int chatId, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        try {
            Chat currentChat = chatDAO.getChat(chatId);
            if (currentChat == null) {
                System.err.println("Failed to access chat: " + chatId);
                return false;
            }

            currentChat.setResponseAttitude(responseAttitude);
            currentChat.setQuizDifficulty(quizDifficulty);
            currentChat.setResponseAttitude(educationLevel);
            currentChat.setStudyArea(studyArea);
            chatDAO.updateChat(currentChat);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update chat name: " + e.getMessage());
            return false;
        }
    }

    // Update the name of a specific Chat record
    public boolean updateChatName(int chatId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return false;
        }

        try {
            Chat currentChat = chatDAO.getChat(chatId);
            if (currentChat == null) {
                System.err.println("Failed to access chat: " + chatId);
                return false;
            }

            currentChat.setName(newName);
            chatDAO.updateChatName(currentChat);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update chat name: " + e.getMessage());
            return false;
        }
    }

    // Retrieve Message records for a specific Chat
    public List<Message> getChatMessages(int chatId) {
        try {
            return messageDAO.getAllChatMessages(chatId);
        } catch (SQLException e) {
            System.err.println("Failed to read messages: " + e.getMessage());
            return null;
        }
    }

    // Create a Quiz object from the AI's response message if it is a quiz message
    public Quiz createNewQuiz(String quizContent, Message responseMessage) {
        return null;
    }

    // Create a QuizQuestion object from the AI's response message if it is a quiz message
    public QuizQuestion createNewQuizQuestion(String questionContent, Quiz quiz) {
        return null;
    }

    // Create an AnswerOption object from the AI's response message if it is a quiz message
    public AnswerOption createNewQuestionAnswerOption(String answerOptionContent, QuizQuestion quizQuestion) {
        return null;
    }
}
