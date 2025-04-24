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

    // TODO: Insert Auth object when implemented
    private SQLiteConnection db;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;

    private User currentUser;

    public ChatController() {
        try {
            db = new SQLiteConnection();
            this.chatDAO = new ChatDAO(db);
            this.messageDAO = new MessageDAO(db);

            // TODO: User will be extracted from AuthService when implemented
            this.currentUser = new User("Temp", "1234");
            currentUser.setId(1);
            if (currentUser == null) {
                throw new IllegalStateException("No user is logged in");
            }
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
                    if (!message.getFromUser() && message.getContent().contains("Here is the quiz you asked for:")) {
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
            // Create a new user message
            Message userMessage = new Message(selectedChat.getId(), content, true, false);
            messageDAO.createMessage(userMessage);

            // Refresh the messages list
            messagesListView.getItems().clear();
            messagesListView.getItems().addAll(messageDAO.getAllChatMessages(selectedChat.getId()));

            // Clear the input field
            messageInputField.clear();

            //TODO: Pass message prompt to AI
            // Simulate an AI response for placeholder
            Message aiResponse = new Message(selectedChat.getId(), "I received your message: " + content, false, false);
            messageDAO.createMessage(aiResponse);

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
            updateChatName(selectedChat, newName); // Let updateChatName handle validation
            chatNameField.setText(selectedChat.getName());
        });
    }

    public void handleCreateChatButton() {
        // TODO: Create chat based on parameters extracted from UI elements and refresh page
    }

    // Creates a new chat
    public Chat createNewChat(int userId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return null;
        }

        try {
            // Create new chat object
            Chat newChat = new Chat(userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea);
            // Add new chat to database
            chatDAO.createChat(newChat);
            return newChat;
        } catch (SQLException e) {
            System.err.println("Failed to create chat: " + e.getMessage());
            return null;
        }
    }

    // Get all chats for a user
    public List<Chat> getUserChats(int userId) {
        try {
            return chatDAO.getAllUserChats(userId);
        } catch (SQLException e) {
            System.err.println("Failed to read chats: " + e.getMessage());
            return null;
        }
    }

    // Get a specific chat
    public Chat getChat(int chatId) {
        try {
            return chatDAO.getChat(chatId);
        } catch (SQLException e) {
            System.err.println("Failed to read chat: " + e.getMessage());
            return null;
        }
    }

    // Update the name of a specific chat
    public boolean updateChatName(int chatId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return false;
        }

        try {
            Chat currentChat = chatDAO.getChat(chatId);
            if (currentChat == null) {
                System.err.println("Chat not found with ID: " + chatId);
                return false;
            }
            if (currentUser == null || currentChat.getUserId() != currentUser.getId()) {
                System.err.println("Chat does not belong to the current user");
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

    // Get all messages for a specific chat
    public List<Message> loadChatMessages(int chatId) {
        try {
            return messageDAO.getAllChatMessages(chatId);
        } catch (SQLException e) {
            System.err.println("Failed to read messages: " + e.getMessage());
            return null;
        }
    }
}
