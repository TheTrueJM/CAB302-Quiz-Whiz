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
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;

    private User currentUser;

    public ChatController() {
        SQLiteConnection connection = new SQLiteConnection();
        this.chatDAO = new ChatDAO(connection);
        this.messageDAO = new MessageDAO(connection);

        // TODO: User will be extracted from AuthService when implemented
        this.currentUser = new User("Temp", "1234");
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in");
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

    private void sendAndReceiveMessage() {
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

    // Creates a new chat and adds the first messageDAO
    public Chat createNewChat (String name, String responseAttitude, String quizDifficulty, String
    educationLevel, String studyArea){

        if (name == null || name.trim().isEmpty()) {
            showErrorAlert("Chat name cannot be empty");
            return null;
        }

        // Create new chat object
        Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, educationLevel, studyArea);

        try {
            // Add new chat to database
            chatDAO.createChat(newChat);
            populateChatsListView();
            return newChat;

        } catch (SQLException e) {
            showErrorAlert("Failed to create chat:" + e.getMessage());
            return null;
        }
    }

    // Get all chats for a user
    public List<Chat> getUserChats ( int userId) {
        try{
            return chatDAO.getAllUserChats(userId);
        }catch (SQLException e){
            showErrorAlert("Failed to load chats");
            return null;
        }
    }

    // Load messages for a specific chat
    public List<Message> loadChatMessages ( int chatId){
        try{
            return messageDAO.getAllChatMessages(chatId);
        }catch (SQLException e){
            showErrorAlert("Failed to load messages");
            return null;
        }
    }


    // Update a chat's name
    public boolean updateChatName (Chat chat, String newName){
        if (chat == null) {
            showErrorAlert("Chat cannot be null");
            return false;
        }
        if (newName == null || newName.trim().isEmpty()) {
            showErrorAlert("Chat name cannot be empty");
            return false;
        }
        if (currentUser == null || chat.getUserId() != currentUser.getId()) {
            showErrorAlert("Chat does not belong to the current user");
            return false;
        }

        try {
            // Create a new Chat object with the updated name
            Chat updatedChat = new Chat(chat.getUserId(), newName, chat.getResponseAttitude(),
                    chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea());

            // Ensure the ID is the same
            updatedChat.setId(chat.getId());

            chatDAO.updateChat(chat);

            // Update the original chat object after successful database update
            chat.setName(newName);

            // Refresh chat list to display new name
            populateChatsListView();
            return true;
        } catch (SQLException e) {
            showErrorAlert("Failed to update chat name: " + e.getMessage());
            return false;
        }
    }
}

