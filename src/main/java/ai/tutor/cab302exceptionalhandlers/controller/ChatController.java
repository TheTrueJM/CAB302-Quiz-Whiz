package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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
    @FXML private Button userDetailsButton;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesVBox;
    private Node tempThinkingMessageNode;
    private Timeline thinkingAnimation;

    private final SQLiteConnection db;
    private final User currentUser;
    private final UserDAO userDAO;
    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final QuizDAO quizDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final AnswerOptionDAO answerOptionDAO;
    private boolean isQuiz;
    private final AIController aiController;
    private boolean isGenerating;

    public ChatController(SQLiteConnection db, User authenticatedUser) throws RuntimeException, SQLException, IOException {
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
        this.isGenerating = false;
        this.aiController = new AIController();
        this.tempThinkingMessageNode = null;
        this.thinkingAnimation = null;
    }

    @FXML
    public void initialize() {
        setupChatSelectionListener();
        setupChatListView();
        refreshChatListView();
        setupEditChatNameButton();
        setupActivateEdit();
        setupSendAndReceiveMessage();
        setupCreateChatButton();
        setupChatSettingsButton();
        setupToggleChatMode();
        setupToggleQuizMode();
        setupLogoutButton();
        setupUserDetailsButton();
    }

    // Problem: ChatController is very dependant on AIController, should that be the case?
    public boolean isOllamaRunning() {
        return aiController.isOllamaRunning();
    }

    public boolean hasModel() {
        return aiController.hasModel();
    }

    public String getModelName() {
        return aiController.getModelName();
    }

    public void setOllamaVerbose(boolean verbose) {
        aiController.setVerbose(verbose);
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
        System.err.println("Error: " + message);
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
            List<Message> messages = messageDAO.getAllChatMessages(selectedChat.getId());
            chatMessagesVBox.getChildren().clear();

            for (Message message : messages) {
                Node messageNode = createMessageNode(message);
                chatMessagesVBox.getChildren().add(messageNode);
            }

            // Scroll to bottom after all messages are added
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            showErrorAlert("Failed to load messages: " + e.getMessage());
        }
    }

    private void setupChatSelectionListener() {
        chatsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chatsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                chatNameField.setText(newChat.getName());
                // No try-except here as refreshMessageList() will handle it
                refreshMessageList(newChat);
            } else {
                chatNameField.setText("");
                chatMessagesVBox.getChildren().clear();
            }
        });
    }

    private Node createMessageNode(Message message) {
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(450);
        messageLabel.setTextFill(Color.BLACK);

        VBox verticalContainer = new VBox(messageLabel);
        verticalContainer.setAlignment(Pos.CENTER);

        HBox horizontalContainer = new HBox(verticalContainer);

        HBox wrapper = new HBox(horizontalContainer);
        wrapper.setFillHeight(false);
        if (message.getContent().equals("Thinking...")) {
            styleThinkingMessage(wrapper, horizontalContainer);
        }
        else if (message.getFromUser()) {
            styleUserMessage(wrapper, horizontalContainer);
        } else if (!message.getFromUser() && message.getIsQuiz()) {
            addQuizMessage(wrapper, horizontalContainer, messageLabel, message, verticalContainer);
        } else {
            styleAIMessage(wrapper, horizontalContainer);
        }

        HBox.setMargin(horizontalContainer, new Insets(7, 0, 0, 7));
        return wrapper;
    }

    private void styleUserMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        horizontalContainer.getStyleClass().add("user-message");
        horizontalContainer.setAlignment(Pos.CENTER_RIGHT);
    }

    private void styleAIMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("ai-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);
    }

    private void styleThinkingMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("thinking-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);
        scrollToBottom();

        Label messageLabel = (Label) ((VBox) horizontalContainer.getChildren().getFirst()).getChildren().getFirst();

        thinkingAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> messageLabel.setText("Thinking")),
                new KeyFrame(Duration.seconds(0.5), e -> messageLabel.setText("Thinking.")),
                new KeyFrame(Duration.seconds(1.0), e -> messageLabel.setText("Thinking..")),
                new KeyFrame(Duration.seconds(1.5), e -> messageLabel.setText("Thinking..."))

        );
        thinkingAnimation.setCycleCount(Timeline.INDEFINITE);
        thinkingAnimation.play();
    }

    private void addQuizMessage(HBox wrapper, HBox horizontalContainer, Label messageLabel, Message message, VBox verticalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("ai-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);

        messageLabel.setText("Here is the quiz you asked for: ");
        Button takeQuizButton = new Button("Take Quiz");
        takeQuizButton.getStyleClass().add("takeQuizButton");
        VBox.setMargin(takeQuizButton, new Insets(6, 0, 0, 0));
        takeQuizButton.setOnAction(event -> handleTakeQuiz(event, message));
        verticalContainer.getChildren().add(takeQuizButton);;
    }

    private void addMessage(Message message) {
        if (chatMessagesVBox == null || chatScrollPane == null) {
            /* This gets covered during unit tests so just skip */
            return;
        }

        Node messageNode = createMessageNode(message);
        chatMessagesVBox.getChildren().add(messageNode);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            // Force layout to update VBox height
            chatMessagesVBox.layout();
            chatScrollPane.setVvalue(1.0);
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

        if (!isOllamaRunning()) {
                showErrorAlert("Ollama is not running. Please install Ollama and pull the model: " + getModelName());
                return;
            } else if (!hasModel()) {
                showErrorAlert("Ollama model is not available. Please run: ollama pull " + getModelName());
                return;
            }

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
                messageInputField.setDisable(true);
                Message userMessage = createNewChatMessage(selectedChat.getId(), content, true, isQuiz);
                messageInputField.clear();
                addMessage(userMessage);
                generateChatMessageResponse(userMessage);
            } catch (SQLException | NoSuchElementException | IllegalArgumentException e) {
                Platform.runLater(() -> {
                    showErrorAlert("Failed to send message: " + e.getMessage());
                    removeTempThinkingMessage();
                    messageInputField.setDisable(false);
                });
            }
        });
    }

    private void addTempThinkingMessage() {
        // Create a temporary message (not saved to database)
        Message thinkingMessage = new Message(getSelectedChat().getId(), "Thinking...", false, false);
        tempThinkingMessageNode = createMessageNode(thinkingMessage);
        chatMessagesVBox.getChildren().add(tempThinkingMessageNode);
        chatScrollPane.setVvalue(1.0);
    }


    private void removeTempThinkingMessage() {
        if (tempThinkingMessageNode != null) {
            chatMessagesVBox.getChildren().remove(tempThinkingMessageNode);
            tempThinkingMessageNode = null;
        }
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
        Object[] params = {db, this, operation, getSelectedChat()};

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Utils.loadPage("chat-setup-view.fxml", ChatSetupController.class, stage, params);
    }

    private void setupLogoutButton() {
        logoutButton.setOnAction(actionEvent -> {
            Object[] params = {db};

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Utils.loadPage("login-view.fxml", LoginController.class, stage, params);
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

    private void setupUserDetailsButton() {
        userDetailsButton.setOnAction(actionEvent -> {
            System.out.println("=================User Details================");

            Object[] params = {db, currentUser};

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Utils.loadPage("user-settings-view.fxml", UserSettingsController.class, stage, params);

        });
    }

    /*
     * =========================================================================
     *                          Chat CRUD Operations
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


    /*
     * =========================================================================
     *                          Message CRUD Operations
     * =========================================================================
     */

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
    public void generateChatMessageResponse(Message userMessage) throws SQLException, IllegalArgumentException, NoSuchElementException {
        if (!userMessage.getFromUser()) {
            throw new IllegalArgumentException("Message must be from user");
        }

        isGenerating = true;
        Platform.runLater(this::addTempThinkingMessage);

        try {
            validateChatExistsForCurrentUser(userMessage.getChatId());
            handleAIResponseGeneration(userMessage, getSelectedChat());
        } catch (NoSuchElementException | IllegalArgumentException e) {
            handleGenerationError(e.getMessage());
            throw e;
        }
    }

    /* ===================== AI Generation Process  ===================== */

    private void handleAIResponseGeneration(Message userMessage, Chat chat){
        generateAIResponse(userMessage, chat)
                .thenAcceptAsync(this::processAIResponse, Platform::runLater)
                .exceptionally(throwable -> {
                    handleGenerationError(throwable.getMessage());
                    return null;
                });
    }

    private CompletableFuture<Message> generateAIResponse(Message userMessage, Chat chat) {
        return aiController.generateResponse(getChatMessages(userMessage.getChatId()), chat, userMessage.getIsQuiz())
                .thenApply(aiMessageContent -> new Message(userMessage.getChatId(), aiMessageContent, false, userMessage.getIsQuiz()));
    }

    private void processAIResponse(Message aiResponse) {
        try {
            messageDAO.createMessage(aiResponse);
            addMessage(aiResponse);
            if (aiResponse.getIsQuiz()) {
                createNewQuiz(aiResponse.getContent(), aiResponse);
            }
            isGenerating = false;
            removeTempThinkingMessage();
            messageInputField.setDisable(false);
        } catch (SQLException e) {
            showErrorAlert("Failed to save AI response: " + e.getMessage());
            isGenerating = false;
            removeTempThinkingMessage();
            messageInputField.setDisable(false);
        }
    }

    private void handleGenerationError(String errorMessage) {
        Platform.runLater(() -> {
            showErrorAlert("Failed to generate AI response: " + errorMessage);
            isGenerating = false;
            removeTempThinkingMessage();
            messageInputField.setDisable(false);
        });
    }

    // Retrieve Message records for a specific Chat
    public List<Message> getChatMessages(int chatId) {
        try {
            validateChatExistsForCurrentUser(chatId);
            return messageDAO.getAllChatMessages(chatId);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Chat doesn't exist");
        } catch (SQLException e) {
            throw new IllegalArgumentException("Failed to get chat messages");
        }
    }

    /*
     * =========================================================================
     *                          Quiz CRUD Operations
     * =========================================================================
     */

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


    /*
     * =========================================================================
     *                          Utility Methods
     * =========================================================================
     */

    private boolean validateNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public User getCurrentUser() {
        return currentUser;
    }

}
