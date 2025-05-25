package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.SceneManager;
import ai.tutor.cab302exceptionalhandlers.Utils.AIUtils;
import ai.tutor.cab302exceptionalhandlers.Utils.AIUtils.*;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import ai.tutor.cab302exceptionalhandlers.types.ChatSetupType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Arrays;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.concurrent.Task;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import one.jpro.platform.mdfx.MarkdownView;

/**
 * Class for running the main chat scene
 * <p>
 * This class runs the main chat screen. It controls the UI surrounding the key messaging function with the AI.
 */

public class ChatController {
    // Chat Window
    @FXML private ListView<Chat> chatsListView;
    @FXML private Button editChatName;
    @FXML private Button addNewChat;
    @FXML private Button confirmEditChatName;
    @FXML private TextField chatNameField;
    @FXML private TextArea messageInputField;
    @FXML private TextField noChatsField;
    @FXML private Button addNewChatMain;
    @FXML private TextField welcomeTitle;
    @FXML private Button logoutButton;
    @FXML private Button chatModeButton;
    @FXML private Button quizModeButton;
    @FXML private VBox greetingContainer;
    @FXML private Button chatSettingsButton;
    @FXML private Button userDetailsButton;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesVBox;
    @FXML private Button sendMessage;
    @FXML private HBox messageContainer;

    private final SQLiteConnection db;
    private final User currentUser;
    private final UserDAO userDAO;
    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final QuizDAO quizDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final AnswerOptionDAO answerOptionDAO;
    private boolean isQuiz;
    private boolean isThinking;
    private int thinkingChatId;
    private final AIUtils aiUtils;

    public ChatController(SQLiteConnection db, User authenticatedUser) throws IOException, RuntimeException, SQLException {
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
        this.isThinking = false;
        this.thinkingChatId = -1;
        this.aiUtils = AIUtils.getInstance();
    }

    /**
     * Initializes the fxml functions to setup the page
     */
    @FXML
    public void initialize() {
        setupChatSelectionListener();
        setupChatListView();
        refreshChatListView();
        setupEditChatNameButton();
        setupActivateEdit();
        setupMessageSendActions();
        setupExpandingMessageInput();
        setupCreateChatButton();
        setupChatSettingsButton();
        setupToggleChatMode();
        setupToggleQuizMode();
        setupLogoutButton();
        setupUserDetailsButton();
    }

    private Stage getStage() {
        return (Stage) chatsListView.getScene().getWindow();
    }

    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

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
                                Utils.showErrorAlert("Failed to delete chat" + e.getMessage());
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
            addNewChatMain.setVisible(true);
            welcomeTitle.setVisible(true);
            welcomeTitle.setText("Welcome, " + currentUser.getUsername());
            greetingContainer.setVisible(true);
            greetingContainer.setManaged(true);
            greetingContainer.setMouseTransparent(false);
            messageInputField.setDisable(true);
            sendMessage.setDisable(true);

        } else {
            editChatName.setVisible(true);
            chatSettingsButton.setVisible(true);
            addNewChatMain.setVisible(false);
            welcomeTitle.setVisible(false);
            greetingContainer.setVisible(false);
            greetingContainer.setManaged(false);
            greetingContainer.setMouseTransparent(true);
            messageInputField.setDisable(false);
            sendMessage.setDisable(false);
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
            Utils.showErrorAlert("Failed to load chats: " + e.getMessage());
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

            // Re-add the thinking node if a task is still running for this chat
            if (isThinking && thinkingChatId == selectedChat.getId()) {
                Node thinkingNode = createThinkingNode();
                chatMessagesVBox.getChildren().add(thinkingNode);
            }

            messageInputField.setDisable(isThinking);
            sendMessage.setDisable(isThinking);

            // Scroll to bottom after all messages are added
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to load messages: " + e.getMessage());
        }
    }

    private void setupChatSelectionListener() {
        chatsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        chatsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            if (newChat != null) {
                chatNameField.setText(newChat.getName());
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

        MarkdownView mdView = new MarkdownView();
        mdView.mdStringProperty().bind(messageLabel.textProperty());

        VBox verticalContainer = new VBox(mdView);
        verticalContainer.setAlignment(Pos.CENTER);

        HBox horizontalContainer = new HBox(verticalContainer);

        HBox wrapper = new HBox(horizontalContainer);
        wrapper.setFillHeight(false);

        if (message.getFromUser()) {
            addUserMessage(wrapper, horizontalContainer);
        } else if (!message.getFromUser() && message.getIsQuiz()) {
            addQuizMessage(wrapper, horizontalContainer, messageLabel, message, verticalContainer);
        } else {
            addAIMessage(wrapper, horizontalContainer);
        }

        HBox.setMargin(horizontalContainer, new Insets(7, 0, 0, 7));
        return wrapper;
    }


    private void addUserMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        horizontalContainer.getStyleClass().add("user-message");
        horizontalContainer.setAlignment(Pos.CENTER_RIGHT);
    }

    private void addAIMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("ai-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);
    }

    private void addQuizMessage(HBox wrapper, HBox horizontalContainer, Label messageLabel, Message message, VBox verticalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("ai-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);

        messageLabel.setText("Here is the quiz you asked for: ");
        Button takeQuizButton = new Button("Take Quiz");
        takeQuizButton.getStyleClass().add("takeQuizButton");
        VBox.setMargin(takeQuizButton, new Insets(6, 0, 0, 0));
        takeQuizButton.setOnAction(event -> {
            try {
                handleTakeQuiz(event, message);
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Loading Quiz: " + e);
            }
        });
        verticalContainer.getChildren().add(takeQuizButton);
    }

    private void addMessage(Message message) {
        if (chatMessagesVBox == null || chatScrollPane == null) {
            /* This gets covered during unit tests so just skip */
            return;
        }

        Node messageNode = createMessageNode(message);
        chatMessagesVBox.getChildren().add(messageNode);

        // listener so that scrollpae auto-scrolls
        ChangeListener<Number> heightListener = new ChangeListener<Number>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number oldHeight, Number newHeight) {
                if (newHeight.doubleValue() > oldHeight.doubleValue()) {
                    chatScrollPane.setVvalue(1.0);
                    chatMessagesVBox.heightProperty().removeListener(this);
                }
            }
        };

        chatMessagesVBox.heightProperty().addListener(heightListener);

        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
            chatMessagesVBox.heightProperty().removeListener(heightListener);
        });
    }

    private Node createThinkingNode() {
        Label thinkingLabel = new Label("Thinking...");
        thinkingLabel.setWrapText(true);
        thinkingLabel.setMaxWidth(450);
        thinkingLabel.setTextFill(Color.BLACK);

        Timeline thinkingAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> thinkingLabel.setText("Thinking")),
                new KeyFrame(Duration.seconds(0.5), e -> thinkingLabel.setText("Thinking.")),
                new KeyFrame(Duration.seconds(1.0), e -> thinkingLabel.setText("Thinking..")),
                new KeyFrame(Duration.seconds(1.5), e -> thinkingLabel.setText("Thinking...")),
                new KeyFrame(Duration.seconds(2), e -> thinkingLabel.setText("Thinking"))

        );
        thinkingAnimation.setCycleCount(Timeline.INDEFINITE);
        thinkingAnimation.play();

        VBox verticalContainer = new VBox(thinkingLabel);
        verticalContainer.setAlignment(Pos.CENTER);

        HBox horizontalContainer = new HBox(verticalContainer);
        horizontalContainer.getStyleClass().add("thinking-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);

        HBox wrapper = new HBox(horizontalContainer);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setFillHeight(false);

        HBox.setMargin(horizontalContainer, new Insets(7, 0, 0, 7));
        return wrapper;
    }

    private void handleTakeQuiz(ActionEvent actionEvent, Message message) throws IOException, RuntimeException, SQLException {
        Quiz currentQuiz = quizDAO.getQuiz(message.getId());
        SceneManager.getInstance().navigateToQuiz(currentQuiz, currentUser);
    }

    /**
     * The primary function to send and retrieve messages and the attached functionality.
     */
    public void SendAndReceiveMessage() {
        Chat selectedChat = getSelectedChat();

            if (!aiUtils.isOllamaRunning()) {
                Utils.showErrorAlert("Ollama is not running. Please install Ollama and pull the model: " + aiUtils.getModelName());
                return;
            } else if (!aiUtils.hasModel()) {
                Utils.showErrorAlert("Ollama model is not available. Please run: ollama pull " + aiUtils.getModelName());
                return;
            }

            if (selectedChat == null) {
                Utils.showErrorAlert("No chat selected");
                return;
            }
            String content = messageInputField.getText();
            if (content == null || content.trim().isEmpty()) {
                Utils.showErrorAlert("Message cannot be empty");
                return;
            }
            try {
                Message userMessage = createNewChatMessage(selectedChat.getId(), content, true, isQuiz);
                messageInputField.clear();
                addMessage(userMessage);

            messageInputField.setDisable(true);
            sendMessage.setDisable(true);

            isThinking = true;
            thinkingChatId = selectedChat.getId();
            Node thinkingNode = createThinkingNode();
            chatMessagesVBox.getChildren().add(thinkingNode);
            messageInputField.setDisable(true);

            Task<Message> aiResponseTask = new Task<Message>() {
                @Override
                protected Message call() throws Exception {
                    return generateAIResponse(userMessage);
                }
            };

            aiResponseTask.setOnSucceeded(e -> {
                Message aiResponse = aiResponseTask.getValue();
                chatMessagesVBox.getChildren().removeLast();
                addMessage(aiResponse);
                isThinking = false;
                thinkingChatId = -1;
                messageInputField.setDisable(false);
                sendMessage.setDisable(false);
            });

            aiResponseTask.setOnFailed(e -> {
                Utils.showErrorAlert("Failed to generate AI response: " + aiResponseTask.getException().getMessage());
                chatMessagesVBox.getChildren().removeLast();
                isThinking = false;
                thinkingChatId = -1;
                messageInputField.setDisable(false);
                sendMessage.setDisable(false);
            });

            new Thread(aiResponseTask).start();
        } catch (SQLException e) {
                Utils.showErrorAlert("Failed to send message: " + e.getMessage());
        }
    }

    private void setupMessageSendActions() {
        messageInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                SendAndReceiveMessage();
            }
        });
        sendMessage.setOnAction(event -> SendAndReceiveMessage());
    }

    private void setupExpandingMessageInput() {
        final double maxHeight = 200;

        // Adjust height dynamically
        messageInputField.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> adjustTextAreaHeight(maxHeight));
        });

        Platform.runLater(() -> adjustTextAreaHeight(maxHeight));
    }

    private void adjustTextAreaHeight(double maxHeight) {
        String text = messageInputField.getText();
        Font font = messageInputField.getFont();
        double lineHeight = font.getSize() * 1.4;
        double padding = 15;

        TextFlow localTextFlow = new TextFlow();
        Text textNode = new Text(text);
        textNode.setFont(font);

        double currentMessageInputFieldWidth = messageInputField.getWidth();
        double calculationMaxWidth = currentMessageInputFieldWidth - 10;

        localTextFlow.setMaxWidth(calculationMaxWidth);
        localTextFlow.getChildren().add(textNode);

        // Calculate lines
        String[] linesArray = text.isEmpty() ? new String[]{""} : text.split("\r\n|\n|\r");
        int explicitLines = (int) Arrays.stream(linesArray)
                .filter(line -> !line.trim().isEmpty())
                .count();
        explicitLines = Math.max(explicitLines, 1);

        double contentWidthForWrapping = localTextFlow.getMaxWidth();
        double actualTextWidthUnwrapped = textNode.getLayoutBounds().getWidth();

        int wrappedLines;
        if (text.isEmpty()) {
            wrappedLines = 1;
        } else if (contentWidthForWrapping > 0 && actualTextWidthUnwrapped > 0) {
            wrappedLines = (int) Math.ceil(actualTextWidthUnwrapped / contentWidthForWrapping);
        } else {
            wrappedLines = 1;
        }
        wrappedLines = Math.max(wrappedLines, 1);

        int lineCount = Math.max(explicitLines, wrappedLines);

        // Calculate and set height
        double newHeight = lineCount * lineHeight + padding;
        newHeight = Math.min(newHeight, maxHeight);

        // Update heights
        messageInputField.setPrefHeight(newHeight);
        messageContainer.setPrefHeight(newHeight);
    }


    private void editChatNameAction() {
        try {
            Chat selectedChat = getSelectedChat();
            String newName = chatNameField.getText();
            if (selectedChat == null) {
                Utils.showErrorAlert("No chat selected");
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
            Utils.showErrorAlert("Failed to update chat name " + e.getMessage());
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
            editChatName.setVisible(false);
            chatNameField.setOpacity(0.8);
            chatNameField.setEditable(true);
            confirmEditChatName.setVisible(true);
        });
    }

    // Loads chat setup window to take in inputs for new chat creation
    private void setupCreateChatButton() {
        addNewChat.setOnAction(actionEvent -> {
            try {
                loadChatSetup();
            } catch (IOException | SQLException e ) {
                Utils.showErrorAlert("Error Loading Chat Setup: " + e);
            }
        });

        addNewChatMain.setOnAction(actionEvent -> {
            try {
                loadChatSetup();
            } catch (IOException | SQLException e ) {
                Utils.showErrorAlert("Error Loading Chat Setup: " + e);
            }
        });
    }

    // Loads settings of specific chat
    private void setupChatSettingsButton() {
        chatSettingsButton.setOnAction(event -> {
            try {
                if (getSelectedChat() == null) { throw new IllegalStateException("No chat selected"); }
                loadChatSetup();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Loading Chat Setting: " + e);
            }
        });
    }

    /**
     * Retrieves the specific selected chat from the database
     */
    public Chat getSelectedChat() {
        return chatsListView.getSelectionModel().getSelectedItem();
    }

    private void loadChatSetup() throws IOException, RuntimeException, SQLException {
        ChatSetupType setupType = getSelectedChat() == null ? ChatSetupType.CREATE : ChatSetupType.UPDATE;
        SceneManager.getInstance().navigateToChatSetup(currentUser, setupType, getSelectedChat());
    }

    private void setupLogoutButton() {
        logoutButton.setOnAction(actionEvent -> {
            SceneManager.getInstance().navigateToAuth(AuthType.LOGIN);
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
            try {
                SceneManager.getInstance().navigateToUserSettings(currentUser);
            } catch (Exception e) {
                Utils.showErrorAlert("Error Loading User Settings: " + e);
            }
        });
    }

    /*
     * =========================================================================
     *                          CRUD Operations
     * =========================================================================
     */

    // Create a new Chat record using UI user input
    // TODO: Remove as it's in ChatSetup
    /**
     * Creates the chat, being removed as its in chatsetup
     */
    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, int quizLength, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
        if (Utils.validateNullOrEmpty(name)) {
            Utils.showErrorAlert("Chat name cannot be empty");
            throw new IllegalArgumentException("Chat name difficulty cannot be empty");
        }
        if (Utils.validateNullOrEmpty(responseAttitude)) {
            Utils.showErrorAlert("Chat response attitude cannot be empty");
            throw new IllegalArgumentException("Chat name response cannot be empty");
        }
        if (Utils.validateNullOrEmpty(quizDifficulty)) {
            Utils.showErrorAlert("Chat quiz difficulty cannot be empty");
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");

        }

        if (Utils.validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (Utils.validateNullOrEmpty(studyArea)) { studyArea = null; }

        // Create and Add Chat to database
        Chat newChat = new Chat(currentUser.getId(), name, responseAttitude, quizDifficulty, quizLength, educationLevel, studyArea);
        chatDAO.createChat(newChat);

        return newChat;
    }

    // Retrieve Chat records for a specific User
    /**
     * Retrieves all user chats from database
     */
    public List<Chat> getUserChats(){
        try {
            return chatDAO.getAllUserChats(currentUser.getId());
        } catch (SQLException e){
            throw new IllegalArgumentException("Error fetching user chats");
        }
    }

    // Retrieve a specific Chat record
    /**
     * Retrieves a specific chat from database
     */
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
    /**
     * Alternative name for get chat in order to provide clarity and consistency
     */
    public Chat validateChatExistsForCurrentUser(int chatId) throws NoSuchElementException, SQLException {
        return getChat(chatId);
    }

    // Update the details of a specific Chat record
    // TODO: Remove as it's in ChatSetup
    /**
     * Redundant as in the chatsetup now
     */
    public void updateChatDetails(int chatId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (Utils.validateNullOrEmpty(name)) {
            throw new IllegalArgumentException("Chat name attitude cannot be empty");
        }

        if (Utils.validateNullOrEmpty(responseAttitude)) {
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
        }
        if (Utils.validateNullOrEmpty(quizDifficulty)) {
            throw new IllegalArgumentException("Chat quiz difficulty cannot be empty");
        }

        if (Utils.validateNullOrEmpty(educationLevel)) { educationLevel = null; }
        if (Utils.validateNullOrEmpty(studyArea)) { studyArea = null; }


        Chat currentChat = getChat(chatId);
        currentChat.setName(name);
        currentChat.setResponseAttitude(responseAttitude);
        currentChat.setQuizDifficulty(quizDifficulty);
        currentChat.setResponseAttitude(educationLevel);
        currentChat.setStudyArea(studyArea);
        chatDAO.updateChat(currentChat);
    }

    // Update the name of a specific Chat record
    /**
     * Updates the chatName in the database
     */
    public void updateChatName(int chatId, String newName) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (Utils.validateNullOrEmpty(newName)) {
            throw new IllegalArgumentException("Chat name cannot be empty");
        }

        Chat currentChat = getChat(chatId);
        currentChat.setName(newName);
        chatDAO.updateChatName(currentChat);
    }


    // Create a Message object using UI user input
    /**
     * Creates a message object based on the user's input
     */
    public Message createNewChatMessage(int chatId, String content, boolean fromUser, boolean isQuiz) throws NoSuchElementException, SQLException {
        if (Utils.validateNullOrEmpty(content)) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        validateChatExistsForCurrentUser(chatId);

        boolean messageIsQuiz = isQuiz;
        Message newMessage = new Message(chatId, content, fromUser, messageIsQuiz);
        messageDAO.createMessage(newMessage);
        return newMessage;
    }

    // Create a Message object from the AI's response output using a user's Message object as input
    // If AI generation fails, create the Message object with default feedback content
    /**
     * Using the ai it generates a response to the user inputted message
     */
    public Message generateChatMessageResponse(Message userMessage) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (!userMessage.getFromUser()) {
            throw new IllegalArgumentException("Message must be from user");
        }

        validateChatExistsForCurrentUser(userMessage.getChatId());

        Message aiResponse = generateAIResponse(userMessage);
        return aiResponse;
    }

    private Message generateAIResponse(Message userMessage) throws NoSuchElementException, SQLException {
        /* Preprocess Chat */
        boolean isQuiz = userMessage.getIsQuiz();
        int chatID = userMessage.getChatId();
        Chat chatConfig = getChat(userMessage.getChatId());
        List<Message> chatHistory = getChatMessages(userMessage.getChatId());

        /* Generation */
        ModelResponseFormat aiMessageContent = aiUtils.generateResponse(chatHistory, chatConfig, isQuiz);
        Message aiResponse = new Message(chatID, aiMessageContent.response, false, isQuiz);

        /* Automatically add message to database */
        messageDAO.createMessage(aiResponse);

        if (aiResponse.getIsQuiz()) {
            createNewQuiz(aiMessageContent, aiResponse);
        }

        return aiResponse;
    }


    // Retrieve Message records for a specific Chat
    /**
     * Retrieves the message records for a specific chat
     */
    public List<Message> getChatMessages(int chatId) throws NoSuchElementException, SQLException {
        validateChatExistsForCurrentUser(chatId);
        return messageDAO.getAllChatMessages(chatId);
    }


    // Create a Quiz object from the AI's response message if it is a quiz message
    /**
     * Creates a quiz using the AI through {@code createNewQuizQuestion} and {@code createNewQuestionAnswerOption}
     */
    public Quiz createNewQuiz(ModelResponseFormat response, Message responseMessage) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (responseMessage == null) {
            throw new IllegalArgumentException("Quiz must be for a message");
        }
        if (!responseMessage.getIsQuiz()){
            throw new IllegalArgumentException("Quiz must be for a quiz message");
        }
        if (responseMessage.getFromUser()){
            throw new IllegalArgumentException("Quiz cannot be for a user message");
        }
        if (!AIUtils.validateQuizResponse(response) || response.getQuizTitle() == null) {
            throw new IllegalArgumentException("Invalid quiz content");
        }

        String quizName = response.getQuizTitle();
        Chat currentChat = getChat(responseMessage.getChatId());
        Quiz newQuiz = new Quiz(responseMessage.getId(), quizName, currentChat.getQuizDifficulty());
        quizDAO.createQuiz(newQuiz);

        for (Question questionFormat : response.getQuizQuestions()) {
            QuizQuestion question = createNewQuizQuestion(questionFormat.getQuestionContent(), newQuiz);
            for (Option answerOptionFormat : questionFormat.getOptions()) {
                createNewQuestionAnswerOption(answerOptionFormat, question);
            }
        }
        return newQuiz;
    }

    // Create a QuizQuestion object from the AI's response message if it is a quiz message
    /**
     * Creates 1 question for a newly generated quiz
     */
    public QuizQuestion createNewQuizQuestion(String questionContent, Quiz quiz) throws IllegalArgumentException, SQLException {
        if (quiz == null) {
            throw new IllegalArgumentException("Question must be for a quiz");
        }

        if (Utils.validateNullOrEmpty(questionContent)) {
            throw new IllegalArgumentException("Question content cannot be empty");
        }

        // TODO: Depending on AI response quizContent extract number from questionContent or assign dynamically
        int questionsCreated = quizQuestionDAO.getAllQuizQuestions(quiz.getMessageId()).size();
        int questionNumber = questionsCreated + 1;

        QuizQuestion question = new QuizQuestion(quiz.getMessageId(), questionNumber, questionContent);
        quizQuestionDAO.createQuizQuestion(question);

        return question;
    }

    // Create an AnswerOption object from the AI's response message if it is a quiz message
    /**
     * Uses the AI response to generate an answer option for the question
     */
    public AnswerOption createNewQuestionAnswerOption(Option option, QuizQuestion quizQuestion) throws IllegalStateException, IllegalStateException, IllegalArgumentException, SQLException{
        if (quizQuestion == null) {
            throw new IllegalArgumentException("Answer option must be for a quiz question");
        }

        if (Utils.validateNullOrEmpty(option.getOptionLetter())) {
            throw new IllegalArgumentException("Answer option letter cannot be empty");
        }

        if (Utils.validateNullOrEmpty(option.getOptionText())) {
            throw new IllegalArgumentException("Answer option text cannot be empty");
        }

        String optionLetter = option.getOptionLetter();
        String optionValue = option.getOptionText();
        boolean isAnswer = option.isAnswer();

        if (answerOptionDAO.getQuestionAnswerOption(quizQuestion.getMessageId(), quizQuestion.getNumber(), optionLetter) != null) {
            throw new IllegalStateException("Answer option already exists");
        }

        AnswerOption answerOption = new AnswerOption(quizQuestion.getMessageId(), quizQuestion.getNumber(), optionLetter, optionValue, isAnswer);
        answerOptionDAO.createAnswerOption(answerOption);

        return answerOption;
    }

    //Sets the chat to quiz mode, which allows quizzes to be generated
    /**
     * Sets the chat to quiz mode, which allows quizzes to be generated
     */
    public void setQuizMode(boolean quizMode) {
        this.isQuiz = quizMode;
    }


    /**
     * Retrieves the message ID the quiz requires
     */
    public Quiz getQuizForMessage(int messageId) throws SQLException, NoSuchElementException {
        Quiz quiz = quizDAO.getQuiz(messageId);
        if (quiz == null) {
            return null;
        }
        return quiz;
    }
}
