package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class ChatController {
    @FXML private ListView<Chat> chatsListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private TextField chatNameField;
    @FXML private Button updateChatNameButton;
    @FXML private TextField messageInputField;

    private final SQLiteConnection db;
    private final User currentUser;
    private final UserDAO userDAO;
    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final QuizDAO quizDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final AnswerOptionDAO answerOptionDAO;


    public ChatController(SQLiteConnection db, User authenticatedUser) throws RuntimeException, SQLException {
        if (authenticatedUser == null) {
            throw new IllegalStateException("No user was authenticated");
        }

        this.db = db;
        this.currentUser = authenticatedUser;
        this.userDAO = new UserDAO(db);
        this.chatDAO = new ChatDAO(db);
        this.messageDAO = new MessageDAO(db);
        this.quizDAO = new QuizDAO(db);
        this.quizQuestionDAO = new QuizQuestionDAO(db);
        this.answerOptionDAO = new AnswerOptionDAO(db);
    }


    @FXML
    public void initialize() {
        refreshChatListView();
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

    private void refreshChatListView () {
        try {
            chatsListView.getItems().clear();
            chatsListView.getItems().addAll(chatDAO.getAllUserChats(currentUser.getId()));
        } catch (SQLException e) {
            showErrorAlert("Failed to load chats: " + e.getMessage());
        }
    }

    private void refreshMessageList(Chat selectedChat) {
        try {
            messagesListView.getItems().clear();
            messagesListView.getItems().addAll(messageDAO.getAllChatMessages(selectedChat.getId()));
        } catch (SQLException e) {
            showErrorAlert("Failed to load messages: " + e.getMessage());
        }
    }

    private void setupChatSelectionListener() {
        chatsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                // No try-except here as refreshMessageList() will handle it
                refreshMessageList(newChat);
                chatNameField.setText(newChat.getName());
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
                    clearCell();
                } else {
                    configureCell(message);
                }
            }

            private void clearCell() {
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll("user-message", "ai-message");
            }

            private void configureCell(Message message) {
                setText(message.getContent());
                applyStyle(message);
                addQuizButton(message);
            }

            private void applyStyle(Message message) {
                getStyleClass().removeAll("user-message", "ai-message");
                if (message.getFromUser()) {
                    getStyleClass().add("user-message");
                } else {
                    getStyleClass().add("ai-message");
                }
            }

            private void addQuizButton(Message message) {
                if (!message.getFromUser() && message.getIsQuiz()) {
                    Button takeQuizButton = new Button("Take Quiz");
                    takeQuizButton.setOnAction(event -> handleTakeQuiz(message));
                    setGraphic(takeQuizButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void handleTakeQuiz(Message message) {
        // TODO: Implement logic for quiz action
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
            Message userMessage = createNewChatMessage(selectedChat.getId(), content, true, false);

            refreshMessageList(selectedChat);
            messageInputField.clear();

            //TODO: Pass message prompt to AI
            generateChatMessageResponse(userMessage);
            refreshMessageList(selectedChat);
        } catch (SQLException e) {
            showErrorAlert("Failed to send message: " + e.getMessage());
        }
    }

    private void setupUpdateChatNameButton() {
        updateChatNameButton.setOnAction(event -> {
            try {
                Chat selectedChat = chatsListView.getSelectionModel().getSelectedItem();
                String newName = chatNameField.getText();
                if (selectedChat == null) {
                    showErrorAlert("No chat selected");
                    return;
                }
                updateChatName(selectedChat.getId(), newName); // Let updateChatName handle validation
                chatNameField.setText(selectedChat.getName());
            } catch (Exception e) {
                showErrorAlert("Failed to send message: " + e.getMessage());
            }
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

    // Create a new Chat record using UI user input
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
        if (validateNullOrEmpty(name)) {
            throw new IllegalArgumentException("Chat name cannot be empty");
        }
        if (validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (validateNullOrEmpty(studyArea)) { studyArea = null; }

        // Create and Add Chat to database
        Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, educationLevel, studyArea);
        chatDAO.createChat(newChat);

        return newChat;
    }

    // Retrieve Chat records for a specific User
    public List<Chat> getUserChats() throws SQLException {
        return chatDAO.getAllUserChats(currentUser.getId());
    }

    // Retrieve a specific Chat record
    public Chat getChat(int chatId) throws NoSuchElementException, SQLException {
        Chat chat = chatDAO.getChat(chatId);

        if (chat == null || currentUser.getId() != chat.getUserId()) {
            throw new NoSuchElementException("Chat does not exist");
        }

        return chat;
    }

    // Alternative name for getChat() to check if a chat exists for the current user
    // This method is not necessary as getChat() is the exact same function
    // but is included for clarity + naming consistency
    public Chat validateChatExistsForCurrentUser(int chatId) throws NoSuchElementException, SQLException {
        return getChat(chatId);
    }

    // Update the details of a specific Chat record
    public void updateChatDetails(int chatId, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (validateNullOrEmpty(studyArea)) { studyArea = null; }

        Chat currentChat = getChat(chatId);
        currentChat.setResponseAttitude(responseAttitude);
        currentChat.setQuizDifficulty(quizDifficulty);
        currentChat.setResponseAttitude(educationLevel);
        currentChat.setStudyArea(studyArea);
        chatDAO.updateChat(currentChat);
    }

    // Update the name of a specific Chat record
    public void updateChatName(int chatId, String newName) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (validateNullOrEmpty(newName)) {
            throw new IllegalArgumentException("Chat name cannot be empty");
        }

        Chat currentChat = getChat(chatId);
        currentChat.setName(newName);
        chatDAO.updateChatName(currentChat);
    }


    // Create a Message object using UI user input
    public Message createNewChatMessage(int chatId, String content, boolean fromUser, boolean isQuiz) throws NoSuchElementException, SQLException {
        if (validateNullOrEmpty(content)) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        validateChatExistsForCurrentUser(chatId);

        Message newMessage = new Message(chatId, content, fromUser, isQuiz);
        messageDAO.createMessage(newMessage);
        return newMessage;
    }

    // Create a Message object from the AI's response output using a user's Message object as input
    // If AI generation fails, create the Message object with default feedback content
    public Message generateChatMessageResponse(Message userMessage) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (!userMessage.getFromUser()) {
            throw new IllegalArgumentException("Message must be from user");
        }

        validateChatExistsForCurrentUser(userMessage.getChatId());

        // TODO: Generate AI message
        String aiMessageContent = "I received your message";
        aiMessageContent = validateNullOrEmpty(aiMessageContent) ? "Default message" : aiMessageContent;

        Message aiResponse = new Message(userMessage.getChatId(), aiMessageContent, false, userMessage.getIsQuiz());
        messageDAO.createMessage(aiResponse);

        //TODO: Operation to split the message for quiz if needed
        if (aiResponse.getIsQuiz()) {
            createNewQuiz(aiMessageContent, aiResponse);
        }

        return aiResponse;
    }

    // Retrieve Message records for a specific Chat
    public List<Message> getChatMessages(int chatId) throws NoSuchElementException, SQLException {
        validateChatExistsForCurrentUser(chatId);
        return messageDAO.getAllChatMessages(chatId);
    }


    // Create a Quiz object from the AI's response message if it is a quiz message
    public Quiz createNewQuiz(String quizContent, Message responseMessage) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (responseMessage == null) {
            throw new IllegalArgumentException("Quiz must be for a message");
        }
        if (!responseMessage.getIsQuiz()){
            throw new IllegalArgumentException("Quiz must be for a quiz message");
        }
        if (responseMessage.getFromUser()){
            throw new IllegalArgumentException("Quiz cannot be for a user message");
        }

        // TODO: Implement proper invalid quiz content format checking
        if (!quizContent.equals("[Valid Quiz Content Format]")){
            throw new IllegalArgumentException("Invalid quiz content format");
        }

        // TODO: Depending on AI response quizContent extract name
        String quizName = "Computer Science Quiz";
        Chat currentChat = getChat(responseMessage.getChatId());
        Quiz newQuiz = new Quiz(responseMessage.getId(), quizName, currentChat.getQuizDifficulty());
        quizDAO.createQuiz(newQuiz);

        return newQuiz;
    }

    // Create a QuizQuestion object from the AI's response message if it is a quiz message
    public QuizQuestion createNewQuizQuestion(String questionContent, Quiz quiz) throws IllegalArgumentException, SQLException {
        if (quiz == null) {
            throw new IllegalArgumentException("Question must be for a quiz");
        }

        // TODO: Implement proper invalid quiz question content format checking
        if (!questionContent.equals("[Valid Quiz Question Content Format]")){
            throw new IllegalArgumentException("Invalid quiz question content format");
        }

        // TODO: Depending on AI response quizContent extract number from questionContent or assign dynamically
        int questionsCreated = quizQuestionDAO.getAllQuizQuestions(quiz.getMessageId()).size();
        int questionNumber = questionsCreated + 1;

        QuizQuestion question = new QuizQuestion(quiz.getMessageId(), questionNumber, questionContent);
        quizQuestionDAO.createQuizQuestion(question);

        return question;
    }

    // Create an AnswerOption object from the AI's response message if it is a quiz message
    public AnswerOption createNewQuestionAnswerOption(String answerOptionContent, QuizQuestion quizQuestion) throws IllegalStateException, IllegalStateException, IllegalArgumentException, SQLException{
        if (quizQuestion == null) {
            throw new IllegalArgumentException("Answer option must be for a quiz question");
        }

        // TODO: Implement proper invalid question answer option content format checking
        if (!answerOptionContent.equals("[Valid Quiz Question Answer Option Content Format]")){
            throw new IllegalArgumentException("Invalid question answer option content format");
        }

        // TODO: Depending on AI response quizContent extract option, value and correctness
        String option = "Option";
        String value = "Answer option statement";
        boolean isAnswer = true;

        if (answerOptionDAO.getQuestionAnswerOption(quizQuestion.getMessageId(), quizQuestion.getNumber(), option) != null) {
            throw new IllegalStateException("Answer option already exists");
        }

        AnswerOption answerOption = new AnswerOption(quizQuestion.getMessageId(), quizQuestion.getNumber(), option, value, isAnswer);
        answerOptionDAO.createAnswerOption(answerOption);

        return answerOption;
    }


    private boolean validateNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
