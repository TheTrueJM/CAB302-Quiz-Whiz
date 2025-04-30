package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class ChatController {
    // Chat Window
    @FXML private ListView<Chat> chatsListView;
    @FXML private ListView<Message> messagesListView;
    @FXML private Button editChatName;
    @FXML private Button addNewChat;
    @FXML private Button confirmEditChatName;
    @FXML private TextField chatNameField;
    @FXML private TextField messageInputField;
    @FXML private TextField noChatsField;
    @FXML private Button configureChat;
    @FXML private TextField welcomeTitle;
    @FXML private Button logoutButton;
    @FXML private Button chatModeButton;
    @FXML private Button quizModeButton;
    @FXML private VBox greetingContainer;
    @FXML private Button chatSettingsButton;

    private final SQLiteConnection db;
    private final User currentUser;
    private final UserDAO userDAO;
    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final QuizDAO quizDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final AnswerOptionDAO answerOptionDAO;
    private boolean isQuiz;

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
        this.isQuiz = false;
    }


    @FXML
    public void initialize() {
        setupChatSelectionListener();
        setupChatListView();
        refreshChatListView();
        setupMessagesListView();
        setupEditChatNameButton();
        setupActivateEdit();
        setupSendAndReceiveMessage();
        setupCreateChatButton();
        setupChatSettingsButton();
        setupToggleChatMode();
        setupToggleQuizMode();
    }


    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    public void showErrorAlert (String message){
        // Create error alert object
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void setupChatListView(){
        chatsListView.setCellFactory(listView -> new ListCell<Chat>() {
            private final ImageView deleteIcon = new ImageView();
            private final Button deleteChatButton = new Button();
            private final Button selectChat = new Button();

            private final HBox container = new HBox(selectChat, deleteChatButton);
            {
                setChatListVisibility();
                selectChat.setOnAction(event -> {
                    Chat chat = getItem();
                    if (chat != null) {
                        chatsListView.getSelectionModel().select(chat);
                        toggleGreetingVisibility();
                        refreshMessageList(chat);
                    }
                });

                // Set up delete chat button
                Image image = new Image(getClass().getResourceAsStream("/ai/tutor/cab302exceptionalhandlers/images/delete.png"));
                deleteIcon.setImage(image);
                deleteIcon.setPreserveRatio(true);
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);

                // Set the icon in the delete button
                deleteChatButton.setGraphic(deleteIcon);
                deleteChatButton.setText("");
                deleteChatButton.setTooltip(new Tooltip("Delete Chat"));
                deleteChatButton.setAlignment(Pos.CENTER_RIGHT);
                deleteChatButton.getStyleClass().add("delete-button");

                // Handle delete action
                deleteChatButton.setOnAction(event -> {
                    Chat chat = getItem();
                    if (chat != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this chat?");
                        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK){
                            try {
                                chatDAO.deleteChat(chat);
                                refreshChatListView();
                            } catch (SQLException e) {
                                showErrorAlert("Failed to delete chat" + e.getMessage());
                            }
                        }
                    }
                });

            }
            @Override
            protected void updateItem(Chat chat, boolean empty) {
                super.updateItem(chat, empty);
                if (empty || chat == null) {
                    setGraphic(null);
                    container.setStyle("");
                    setStyle("-fx-background-color: #535353;");
                } else {
                    selectChat.getStyleClass().add("chat-selector");
                    container.getStyleClass().add("chat-selector-container");

                    selectChat.setText(chat.getName());
                    selectChat.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(container);
                }
            }
        });
    }

    private void setChatListVisibility (){
        if (getUserChats().isEmpty()){
            chatsListView.setVisible(false);
            noChatsField.setVisible(true);
            noChatsField.setAlignment(Pos.TOP_CENTER);
        }
        else {
            chatsListView.setVisible(true);
            noChatsField.setVisible(false);
        }
    }

    private void toggleGreetingVisibility() {
        if (getSelectedChat() == null) {
            editChatName.setVisible(false);
            chatSettingsButton.setVisible(false);
            configureChat.setVisible(true);
            welcomeTitle.setVisible(true);
            welcomeTitle.setText("Welcome, " + currentUser.getUsername());
            greetingContainer.setVisible(true);
            greetingContainer.setManaged(true);
            greetingContainer.setMouseTransparent(false);
        } else {
            editChatName.setVisible(true);
            chatSettingsButton.setVisible(true);
            configureChat.setVisible(false);
            welcomeTitle.setVisible(false);
            greetingContainer.setVisible(false);
            greetingContainer.setManaged(false);
            greetingContainer.setMouseTransparent(true);
        }
}


    public void refreshChatListView () {
        try {
            Chat selectedChat = getSelectedChat();
            Integer selectedChatId = (selectedChat != null) ? selectedChat.getId() : null;

            setChatListVisibility();
            chatsListView.getItems().clear();
            chatsListView.getItems().addAll(chatDAO.getAllUserChats(currentUser.getId()));
            List<Chat> updatedChats = chatsListView.getItems();

            // Reselect the current chat
            if (selectedChatId != null) {
                for (Chat chat : updatedChats) {
                    if (chat.getId() == selectedChatId) {
                        chatsListView.getSelectionModel().select(chat);
                        break;
                    }
                }
            } else {
                toggleGreetingVisibility();
            }
        } catch (SQLException e) {
            showErrorAlert("Failed to load chats: " + e.getMessage());
        }
    }

    private void refreshMessageList(Chat selectedChat) {
        try {
            messagesListView.getItems().clear();
            messagesListView.getItems().addAll(messageDAO.getAllChatMessages(selectedChat.getId()));
            // Scroll to the last message
            int lastIndex = messagesListView.getItems().size() - 1;
            if (lastIndex >= 0) {
                messagesListView.scrollTo(lastIndex);
            }
        } catch (SQLException e) {
            showErrorAlert("Failed to load messages: " + e.getMessage());
        }
    }

    private void setupChatSelectionListener() {
        // Ensure single selection mode
        chatsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chatsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                chatNameField.setText(newChat.getName());
                // No try-except here as refreshMessageList() will handle it
                refreshMessageList(newChat);
            } else {
                chatNameField.setText("");
                messagesListView.getItems().clear();
            }
        });
    }

    private void setupMessagesListView() {
        messagesListView.setCellFactory(listView -> new ListCell<Message>() {
            private final Label messageContent = new Label();
            private final VBox verticalContainer = new VBox(messageContent);
            private final HBox horizontalContainer = new HBox(verticalContainer);
            private final HBox wrapper = new HBox(horizontalContainer);

            {
                // Settings for message contents
                messageContent.setWrapText(true);

                // Set all cell background to white
                setStyle("-fx-background-color: white;");

                verticalContainer.setAlignment(Pos.CENTER);
                HBox.setMargin(horizontalContainer, new Insets(7, 0, 0, 7));

                wrapper.setPrefWidth(Region.USE_COMPUTED_SIZE);
                wrapper.setMaxWidth(450);
                wrapper.setFillHeight(false);
                HBox.setHgrow(wrapper, Priority.NEVER);
            }
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
                setGraphic(null);
                setAlignment(Pos.CENTER_LEFT);
            }

            private void configureCell(Message message) {
                horizontalContainer.getStyleClass().clear();
                verticalContainer.getStyleClass().clear();
                messageContent.setText(message.getContent());
                applyStyle(message);
                addQuizButton(message);
                setGraphic(wrapper);
            }

            private void applyStyle(Message message) {
                if (message.getFromUser()) {
                    horizontalContainer.getStyleClass().setAll("user-message");
                    wrapper.setAlignment(Pos.CENTER_RIGHT);
                    horizontalContainer.setAlignment(Pos.CENTER_RIGHT);
                    setAlignment(Pos.CENTER_RIGHT);
                } else {
                    horizontalContainer.getStyleClass().setAll("ai-message");
                    setAlignment(Pos.CENTER_LEFT);
                    wrapper.setAlignment(Pos.CENTER_LEFT);
                    horizontalContainer.setAlignment(Pos.CENTER_LEFT);
                }
            }

            private void addQuizButton(Message message) {
                verticalContainer.getChildren().setAll(messageContent);
                verticalContainer.setAlignment(Pos.CENTER);

                if (!message.getFromUser() && message.getIsQuiz() && verticalContainer.getChildren().size() == 1) {
                    messageContent.setText("Here is the quiz you asked for: ");
                    Button takeQuizButton = new Button("Take Quiz");
                    takeQuizButton.getStyleClass().add("takeQuizButton");
                    VBox.setMargin(takeQuizButton, new Insets(6, 0, 0,0));
                    takeQuizButton.setOnAction(event -> handleTakeQuiz(event, message));
                    verticalContainer.getChildren().add(takeQuizButton);
                }
            }
        });
    }

    private void handleTakeQuiz(ActionEvent actionEvent, Message message) {
        // TODO: Implement logic for quiz action
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    QuizWhizApplication.class.getResource("quiz-view.fxml")
            );

            QuizController controller = new QuizController(db, quizDAO.getQuiz(message.getId()));
            fxmlLoader.setController(controller);

            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            // Get the Stage from the event
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setupSendAndReceiveMessage() {
        messageInputField.setOnAction(event -> {
            Chat selectedChat = getSelectedChat();
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
                Message userMessage = createNewChatMessage(selectedChat.getId(), content, true, isQuiz);
                messageInputField.clear();

                //TODO: Pass message prompt to AI
                generateChatMessageResponse(userMessage);
                refreshMessageList(selectedChat);

            } catch (SQLException e) {
                showErrorAlert("Failed to send message: " + e.getMessage());
            }
        });
    }

    private void editChatNameAction() {
        try {
            Chat selectedChat = getSelectedChat();
            String newName = chatNameField.getText();
            if (selectedChat == null) {
                showErrorAlert("No chat selected");
                return;
            }
            updateChatName(selectedChat.getId(), newName); // Let updateChatName handle validation
            chatNameField.setText(selectedChat.getName());
            chatNameField.setOpacity(1);
            editChatName.setVisible(true);
            chatNameField.setEditable(false);
            confirmEditChatName.setVisible(false);
            refreshChatListView();
        } catch (Exception e) {
            showErrorAlert("Failed to update chat name " + e.getMessage());
        }
    }

    // Sets up button and text field to update the chat name on confirmation
    private void setupEditChatNameButton() {
       // Update chat name with button or text field confirm
       confirmEditChatName.setOnAction(event -> editChatNameAction());
       chatNameField.setOnAction(event -> editChatNameAction());

    }

    // Set up button that activates the ability to edit the chat name
    private void setupActivateEdit() {
        editChatName.setOnAction(actionEvent ->  {
            // TODO: Refactor these into class in styles.css and change the css class instead
            editChatName.setVisible(false);
            chatNameField.setOpacity(0.8);
            chatNameField.setEditable(true);
            confirmEditChatName.setVisible(true);
        });
    }

    // Loads chat setup window to take in inputs for new chat creation
    private void setupCreateChatButton() {
        // TODO: Create chat based on parameters extracted from UI elements and refresh page
        addNewChat.setOnAction(actionEvent -> {
            loadChatSettings(actionEvent, "Create");
        });
    }

    // Loads settings of specfic chat
    private void setupChatSettingsButton() {
        chatSettingsButton.setOnAction(event -> {
            loadChatSettings(event, "Update");
        });
    }

    public Chat getSelectedChat() {
        return chatsListView.getSelectionModel().getSelectedItem();
    }

    private void loadChatSettings(ActionEvent actionEvent, String operation) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    QuizWhizApplication.class.getResource("chat-setup-view.fxml")
            );

            ChatSetupController controller = new ChatSetupController(db, currentUser, this, operation, getSelectedChat());
            fxmlLoader.setController(controller);

            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            // Get the Stage from the event
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Logout functionality
    private void setupLogoutButton() {
        logoutButton.setOnAction(actionEvent -> {
        });
    }

    private void setupToggleChatMode() {
        chatModeButton.setOnAction(event -> {
            chatModeButton.getStyleClass().setAll("chat-mode-active");
            chatModeButton.setDisable(true);
            quizModeButton.getStyleClass().setAll("quiz-mode-disabled");
            quizModeButton.setDisable(false);
            quizModeButton.setOpacity(1);
            isQuiz = false;
        });
    }

    private void setupToggleQuizMode() {
        quizModeButton.setOnAction(event -> {
            quizModeButton.getStyleClass().setAll("quiz-mode-active");
            quizModeButton.setDisable(true);
            chatModeButton.getStyleClass().setAll("chat-mode-disabled");
            chatModeButton.setDisable(false);
            chatModeButton.setOpacity(1);
            isQuiz = true;
        });
    }

    /*
     * =========================================================================
     *                          CRUD Operations
     * =========================================================================
     */

    // Create a new Chat record using UI user input
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
        if (validateNullOrEmpty(name)) {
            showErrorAlert("Chat name cannot be empty");
            throw new IllegalArgumentException("Chat name difficulty cannot be empty");
        }
        if (validateNullOrEmpty(responseAttitude)) {
            showErrorAlert("Chat response attitude cannot be empty");
            throw new IllegalArgumentException("Chat name response cannot be empty");
        }
        if (validateNullOrEmpty(quizDifficulty)) {
            showErrorAlert("Chat quiz difficulty cannot be empty");
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
    public List<Chat> getUserChats(){
        try {
            return chatDAO.getAllUserChats(currentUser.getId());
        } catch (SQLException e){
            throw new IllegalArgumentException("Error fetching user chats");
        }
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
    public void updateChatDetails(int chatId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (validateNullOrEmpty(name)) {
            throw new IllegalArgumentException("Chat name attitude cannot be empty");
        }

        if (validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (validateNullOrEmpty(studyArea)) { studyArea = null; }


        Chat currentChat = getChat(chatId);
        currentChat.setName(name);
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

        boolean messageIsQuiz = fromUser ? isQuiz : false;
        Message newMessage = new Message(chatId, content, fromUser, messageIsQuiz);
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
//        if (!quizContent.equals("[Valid Quiz Content Format]")){
//            throw new IllegalArgumentException("Invalid quiz content format");
//        }

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
