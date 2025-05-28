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
 * Controls the main chat screen in the AI tutor application.
 * <p>
 * Manages the chat interface, allowing users to select chats, send/receive messages,
 * generate quizzes, and navigate to other screens (e.g., chat setup, user settings,
 * login). Integrates with the database via {@link ChatDAO}, {@link MessageDAO},
 * {@link QuizDAO}, {@link QuizQuestionDAO}, and {@link AnswerOptionDAO}, and uses
 * {@link AIUtils} for AI-generated responses.
 * </p>
 * @see ChatDAO
 * @see MessageDAO
 * @see QuizDAO
 * @see QuizQuestionDAO
 * @see AnswerOptionDAO
 * @see AIUtils
 * @see SceneManager
 * @see User
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

    /**
     * Constructs a ChatController with a database connection and authenticated user.
     * <p>
     * Initializes the {@link #db} connection, sets the {@link #currentUser}, and creates
     * instances of {@link UserDAO}, {@link ChatDAO}, {@link MessageDAO}, {@link QuizDAO},
     * {@link QuizQuestionDAO}, and {@link AnswerOptionDAO}. Throws an exception if the
     * user is null.
     * </p>
     * @param db The SQLite database connection
     * @param authenticatedUser The currently authenticated user
     * @throws IllegalStateException If the user is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public ChatController(SQLiteConnection db, User authenticatedUser) throws IllegalStateException, RuntimeException, SQLException {
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
     * Initializes the chat screen’s UI components and event handlers.
     * <p>
     * Sets up chat selection listener ({@link #setupChatSelectionListener()}), chat
     * list view ({@link #setupChatListView()}), refreshes the chat list
     * ({@link #refreshChatListView()}), and configures buttons for editing chat names
     * ({@link #setupEditChatNameButton()}), activating edits ({@link #setupActivateEdit()}),
     * sending messages ({@link #setupMessageSendActions()}), expanding the message input
     * ({@link #setupExpandingMessageInput()}), creating chats ({@link #setupCreateChatButton()}),
     * chat settings ({@link #setupChatSettingsButton()}), toggling chat mode
     * ({@link #setupToggleChatMode()}), toggling quiz mode ({@link #setupToggleQuizMode()}),
     * logout ({@link #setupLogoutButton()}), and user details ({@link #setupUserDetailsButton()}).
     * Called automatically by JavaFX when the FXML is loaded.
     * </p>
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


    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    /**
     * Configures the chat list view with custom cells for chat selection and deletion.
     * <p>
     * Sets up a custom {@link ListCell} for {@link #chatsListView} with a select button
     * and a delete button featuring a delete icon. Handles chat selection and deletion
     * with confirmation, updating the view via {@link #refreshChatListView()}.
     * </p>
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

    /**
     * Toggles the visibility of the chat list and no-chats field based on available chats.
     * <p>
     * Shows {@link #chatsListView} if chats exist, otherwise shows {@link #noChatsField}.
     * </p>
     */

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

    /**
     * Toggles the visibility of the greeting container based on chat selection.
     * <p>
     * Displays the {@link #greetingContainer} with a welcome message if no chat is selected,
     * otherwise hides it and enables message input.
     * </p>
     */

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

    /**
     * Refreshes the chat list view to display updated chats.
     * <p>
     * Clears and repopulates {@link #chatsListView} with chats from {@link #chatDAO},
     * preserving the selected chat if it exists.
     * </p>
     */

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

    /**
     * Refreshes the message list for the selected chat.
     * <p>
     * Retrieves messages from {@link #messageDAO}, clears {@link #chatMessagesVBox},
     * and adds message nodes. Adds a thinking node if the AI is processing for this chat.
     * </p>
     * @param selectedChat The currently selected chat
     */

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

    /**
     * Sets up a listener for chat selection changes.
     * <p>
     * Configures {@link #chatsListView} to handle single selection, updating the
     * {@link #chatNameField} and refreshing messages when a new chat is selected.
     * </p>
     */

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

    /**
     * Creates a UI node for a given message.
     * <p>
     * Constructs a {@link Label} or {@link MarkdownView} for the message content,
     * wrapped in {@link HBox} and {@link VBox}, with styling based on whether the
     * message is from the user, AI, or a quiz.
     * </p>
     * @param message The message to display
     * @return The UI node representing the message
     */

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

    /**
     * Styles a message node as a user message.
     * <p>
     * Aligns the {@link HBox} to the right and adds the "user-message" CSS class.
     * </p>
     * @param wrapper The outer {@link HBox} wrapper
     * @param horizontalContainer The inner {@link HBox} containing the message
     */

    private void addUserMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        horizontalContainer.getStyleClass().add("user-message");
        horizontalContainer.setAlignment(Pos.CENTER_RIGHT);
    }

    /**
     * Styles a message node as an AI message.
     * <p>
     * Aligns the {@link HBox} to the left and adds the "ai-message" CSS class.
     * </p>
     * @param wrapper The outer {@link HBox} wrapper
     * @param horizontalContainer The inner {@link HBox} containing the message
     */

    private void addAIMessage(HBox wrapper, HBox horizontalContainer) {
        wrapper.setAlignment(Pos.CENTER_LEFT);
        horizontalContainer.getStyleClass().add("ai-message");
        horizontalContainer.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Styles a message node as a quiz message with a "Take Quiz" button.
     * <p>
     * Aligns the {@link HBox} to the left, adds the "ai-message" CSS class, and includes
     * a button to navigate to the quiz screen.
     * </p>
     * @param wrapper The outer {@link HBox} wrapper
     * @param horizontalContainer The inner {@link HBox} containing the message
     * @param messageLabel The label for the message content
     * @param message The message object
     * @param verticalContainer The vertical container for additional elements
     */

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

    /**
     * Adds a message node to the chat messages container.
     * <p>
     * Creates and adds a message node to {@link #chatMessagesVBox}, with auto-scrolling
     * functionality when the height changes.
     * </p>
     * @param message The message to add
     */

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

    /**
     * Creates a UI node indicating the AI is thinking.
     * <p>
     * Returns a {@link HBox} with a label and an animated "Thinking..." text.
     * </p>
     * @return The UI node representing the thinking state
     */

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

    /**
     * Handles the action to take a quiz from a message.
     * <p>
     * Retrieves the quiz from {@link #quizDAO} and navigates to the quiz screen
     * using {@link SceneManager}.
     * </p>
     * @param actionEvent The action event triggering the quiz
     * @param message The message containing the quiz
     * @throws Exception If navigation or quiz retrieval fails
     */

    private void handleTakeQuiz(ActionEvent actionEvent, Message message) throws Exception {
        Quiz currentQuiz = quizDAO.getQuiz(message.getId());
        SceneManager.getInstance().navigateToQuiz(currentQuiz, currentUser);
    }

    /**
     * Sends a message and retrieves an AI response.
     * <p>
     * Validates AI availability, chat selection, and message content, creates a user
     * message, and triggers an asynchronous task to generate an AI response.
     * </p>
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

    /**
     * Sets up actions for sending messages.
     * <p>
     * Configures {@link #messageInputField} to send messages on Enter key press and
     * {@link #sendMessage} to trigger {@link #SendAndReceiveMessage()} on click.
     * </p>
     */

    private void setupMessageSendActions() {
        messageInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                SendAndReceiveMessage();
            }
        });
        sendMessage.setOnAction(event -> SendAndReceiveMessage());
    }

    /**
     * Sets up the message input field to expand dynamically.
     * <p>
     * Adjusts the height of {@link #messageInputField} based on content, up to a
     * maximum height of 200 pixels, using {@link #adjustTextAreaHeight(double)}.
     * </p>
     */

    private void setupExpandingMessageInput() {
        final double maxHeight = 200;

        // Adjust height dynamically
        messageInputField.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> adjustTextAreaHeight(maxHeight));
        });

        Platform.runLater(() -> adjustTextAreaHeight(maxHeight));
    }

    /**
     * Adjusts the height of the message input field based on content.
     * <p>
     * Calculates the required height based on text lines and wraps, setting the
     * {@link #messageInputField} and {@link #messageContainer} heights accordingly.
     * </p>
     * @param maxHeight The maximum allowable height in pixels
     */

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

    /**
     * Handles the action to edit a chat name.
     * <p>
     * Updates the chat name using {@link #updateChatName(int, String)} and refreshes
     * the chat list view.
     * </p>
     */

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

    /**
     * Configures the edit chat name button and text field actions.
     * <p>
     * Sets up {@link #confirmEditChatName} and {@link #chatNameField} to trigger
     * {@link #editChatNameAction()} on action.
     * </p>
     */

    private void setupEditChatNameButton() {
       // Update chat name with button or text field confirm
       confirmEditChatName.setOnAction(event -> editChatNameAction());
       chatNameField.setOnAction(event -> editChatNameAction());
    }

    /**
     * Configures the button to activate chat name editing.
     * <p>
     * Sets up {@link #editChatName} to enable editing of {@link #chatNameField}
     * and show {@link #confirmEditChatName}.
     * </p>
     */

    private void setupActivateEdit() {
        editChatName.setOnAction(actionEvent ->  {
            editChatName.setVisible(false);
            chatNameField.setOpacity(0.8);
            chatNameField.setEditable(true);
            confirmEditChatName.setVisible(true);
        });
    }

    /**
     * Configures the buttons to create a new chat.
     * <p>
     * Sets up {@link #addNewChat} and {@link #addNewChatMain} to trigger
     * {@link #loadChatSetup()} for chat creation.
     * </p>
     */

    private void setupCreateChatButton() {
        addNewChat.setOnAction(actionEvent -> {
            try {
                loadChatSetup();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Loading Chat Setup: " + e);
            }
        });

        addNewChatMain.setOnAction(actionEvent -> {
            try {
                loadChatSetup();
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Loading Chat Setup: " + e);
            }
        });
    }

    /**
     * Configures the chat settings button to load chat setup.
     * <p>
     * Sets up {@link #chatSettingsButton} to trigger {@link #loadChatSetup()} for
     * the selected chat, with validation for chat selection.
     * </p>
     */

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
     * Retrieves the currently selected chat from the chat list view.
     * <p>
     * Returns the selected item from {@link #chatsListView}.
     * </p>
     * @return The selected {@link Chat} or null if none is selected
     */

    public Chat getSelectedChat() {
        return chatsListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Loads the chat setup screen based on the selected chat.
     * <p>
     * Navigates to {@link ChatSetupType#CREATE} if no chat is selected, or
     * {@link ChatSetupType#UPDATE} if a chat is selected, using {@link SceneManager}.
     * </p>
     * @throws Exception If navigation fails
     */

    private void loadChatSetup() throws Exception {
        ChatSetupType setupType = getSelectedChat() == null ? ChatSetupType.CREATE : ChatSetupType.UPDATE;
        SceneManager.getInstance().navigateToChatSetup(currentUser, setupType, getSelectedChat());
    }

    /**
     * Configures the logout button to navigate to the login screen.
     * <p>
     * Sets up {@link #logoutButton} to trigger navigation using {@link SceneManager}.
     * </p>
     */

    private void setupLogoutButton() {
        logoutButton.setOnAction(actionEvent -> {
            try {
                SceneManager.getInstance().navigateToAuth(AuthType.LOGIN);
            } catch (Exception e) {
                Utils.showErrorAlert("Error Loading Authentication: " + e);
            }
        });
    }

    /**
     * Configures the chat mode button to toggle chat mode.
     * <p>
     * Sets up {@link #chatModeButton} to enable chat mode and disable quiz mode,
     * updating {@link #isQuiz} to false.
     * </p>
     */

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

    /**
     * Configures the quiz mode button to toggle quiz mode.
     * <p>
     * Sets up {@link #quizModeButton} to enable quiz mode and disable chat mode,
     * updating {@link #isQuiz} to true.
     * </p>
     */

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

    /**
     * Configures the user details button to navigate to user settings.
     * <p>
     * Sets up {@link #userDetailsButton} to trigger navigation using {@link SceneManager}.
     * </p>
     */

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

    /**
     * Creates a new chat with the specified parameters.
     * <p>
     * Validates inputs and creates a {@link Chat} object, saving it to the database
     * via {@link #chatDAO}. Note: This method is marked TODO for removal as it’s
     * handled in {@link ChatSetupController} ??.
     * </p>
     * @param name The inputted chat name
     * @param responseAttitude The selected response attitude
     * @param quizDifficulty The selected quiz difficulty
     * @param quizLength The chosen quiz length
     * @param educationLevel The selected education level
     * @param studyArea The inputted study area
     * @return The newly created {@link Chat}
     * @throws IllegalArgumentException If the chat name, response attitude, or quiz difficulty is empty
     * @throws SQLException If database operations fail
     */

    public Chat createNewChat(String name, String responseAttitude, String quizDifficulty, int quizLength, String educationLevel, String studyArea) throws IllegalArgumentException, SQLException {
        if (Utils.validateNullOrEmpty(name)) {
            Utils.showErrorAlert("Chat name cannot be empty");
            throw new IllegalArgumentException("Chat name cannot be empty");
        }
        if (Utils.validateNullOrEmpty(responseAttitude)) {
            Utils.showErrorAlert("Chat response attitude cannot be empty");
            throw new IllegalArgumentException("Chat response attitude cannot be empty");
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


    /**
     * Retrieves all chats for the current user from the database.
     * <p>
     * Fetches chats using {@link #chatDAO} based on {@link #currentUser}’s ID.
     * </p>
     * @return A list of all chats for the current user
     * @throws IllegalArgumentException If fetching chats fails
     */

    public List<Chat> getUserChats(){
        try {
            return chatDAO.getAllUserChats(currentUser.getId());
        } catch (SQLException e){
            throw new IllegalArgumentException("Error fetching user chats");
        }
    }

    /**
     * Retrieves a specific chat from the database.
     * <p>
     * Fetches the chat using {@link #chatDAO} and validates it belongs to
     * {@link #currentUser}.
     * </p>
     * @param chatId The ID of the chat to retrieve
     * @return The requested {@link Chat}
     * @throws NoSuchElementException If the chat does not exist or doesn’t belong to the user
     * @throws SQLException If database operations fail
     */

    public Chat getChat(int chatId) throws NoSuchElementException, SQLException {
        Chat chat = chatDAO.getChat(chatId);

        if (chat == null || currentUser.getId() != chat.getUserId()) {
            throw new NoSuchElementException("Chat does not exist");
        }

        return chat;
    }

    /**
     * Validates a chat exists for the current user (alternative to {@link #getChat(int)}).
     * <p>
     * Provides clarity and consistency by reusing {@link #getChat(int)}.
     * </p>
     * @param chatId The ID of the chat to validate
     * @return The requested {@link Chat}
     * @throws NoSuchElementException If the chat does not exist or doesn’t belong to the user
     * @throws SQLException If database operations fail
     */

    public Chat validateChatExistsForCurrentUser(int chatId) throws NoSuchElementException, SQLException {
        return getChat(chatId);
    }

    /**
     * Updates the details of a specific chat (marked as redundant, handled in {@link ChatSetupController}).
     * <p>
     * Note: This method is marked TODO for removal as it’s handled in {@link ChatSetupController}.
     * </p>
     * @param chatId The ID of the chat to update
     * @param name The new chat name
     * @param responseAttitude The new response attitude
     * @param quizDifficulty The new quiz difficulty
     * @param educationLevel The new education level
     * @param studyArea The new study area
     * @throws IllegalArgumentException If the chat name, response attitude, or quiz difficulty is empty
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
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

    /**
     * Updates the name of a specific chat in the database.
     * <p>
     * Validates the new name and updates the chat using {@link #chatDAO}.
     * </p>
     * @param chatId The ID of the chat to update
     * @param newName The new name for the chat
     * @throws IllegalArgumentException If the chat name is empty
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
     */

    public void updateChatName(int chatId, String newName) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (Utils.validateNullOrEmpty(newName)) {
            throw new IllegalArgumentException("Chat name cannot be empty");
        }

        Chat currentChat = getChat(chatId);
        currentChat.setName(newName);
        chatDAO.updateChatName(currentChat);
    }


    /**
     * Creates a new message based on user input.
     * <p>
     * Validates the content and creates a {@link Message} object, saving it to the
     * database via {@link #messageDAO}.
     * </p>
     * @param chatId The ID of the chat
     * @param content The message content
     * @param fromUser Indicates if the message is from the user
     * @param isQuiz Indicates if the message requests a quiz
     * @return The newly created {@link Message}
     * @throws IllegalArgumentException If the message content is empty
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
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

    /**
     * Generates an AI response to a user message.
     * <p>
     * Validates the message is from a user, generates a response using {@link #generateAIResponse(Message)},
     * and returns the AI’s {@link Message}.
     * </p>
     * @param userMessage The user’s message
     * @return The AI’s response {@link Message}
     * @throws IllegalArgumentException If the message is not from a user
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
     */

    public Message generateChatMessageResponse(Message userMessage) throws IllegalArgumentException, NoSuchElementException, SQLException {
        if (!userMessage.getFromUser()) {
            throw new IllegalArgumentException("Message must be from user");
        }

        validateChatExistsForCurrentUser(userMessage.getChatId());

        Message aiResponse = generateAIResponse(userMessage);
        return aiResponse;
    }

    /**
     * Generates an AI response based on chat history and configuration.
     * <p>
     * Preprocesses the chat, generates a response using {@link AIUtils#generateResponse(List, Chat, boolean)},
     * saves the response to the database, and creates a quiz if applicable.
     * </p>
     * @param userMessage The user’s message
     * @return The AI’s response {@link Message}
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
     */

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


    /**
     * Retrieves all messages for a specific chat.
     * <p>
     * Validates the chat exists and fetches messages using {@link #messageDAO}.
     * </p>
     * @param chatId The ID of the chat
     * @return A list of all messages for the chat
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
     */

    public List<Message> getChatMessages(int chatId) throws NoSuchElementException, SQLException {
        validateChatExistsForCurrentUser(chatId);
        return messageDAO.getAllChatMessages(chatId);
    }


    /**
     * Creates a new quiz from an AI response message.
     * <p>
     * Validates the response and creates a {@link Quiz} with associated
     * {@link QuizQuestion} and {@link AnswerOption} objects.
     * </p>
     * @param response The AI response format
     * @param responseMessage The response message
     * @return The newly created {@link Quiz}
     * @throws IllegalArgumentException If the message is null, not a quiz message, from a user, or invalid
     * @throws NoSuchElementException If the chat does not exist
     * @throws SQLException If database operations fail
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

    /**
     * Creates a new quiz question for a given quiz.
     * <p>
     * Validates the quiz and question content, creating a {@link QuizQuestion} object.
     * </p>
     * @param questionContent The content of the question
     * @param quiz The quiz to associate with the question
     * @return The newly created {@link QuizQuestion}
     * @throws IllegalArgumentException If the quiz is null or question content is empty
     * @throws SQLException If database operations fail
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

    /**
     * Creates a new answer option for a quiz question.
     * <p>
     * Validates the option and creates an {@link AnswerOption} object.
     * </p>
     * @param option The option details
     * @param quizQuestion The question to associate with the option
     * @return The newly created {@link AnswerOption}
     * @throws IllegalArgumentException If the question is null, option letter or text is empty
     * @throws IllegalStateException If the answer option already exists
     * @throws SQLException If database operations fail
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

    /**
     * Sets the chat to quiz mode.
     * <p>
     * Updates {@link #isQuiz} to enable or disable quiz generation.
     * </p>
     * @param quizMode Whether to enable quiz mode
     */

    public void setQuizMode(boolean quizMode) {
        this.isQuiz = quizMode;
    }


    /**
     * Retrieves the quiz associated with a message.
     * <p>
     * Fetches the quiz from {@link #quizDAO} based on the message ID.
     * </p>
     * @param messageId The ID of the message
     * @return The associated {@link Quiz} or null if none exists
     * @throws SQLException If database operations fail
     * @throws NoSuchElementException If the quiz does not exist
     */

    public Quiz getQuizForMessage(int messageId) throws SQLException, NoSuchElementException {
        Quiz quiz = quizDAO.getQuiz(messageId);
        if (quiz == null) {
            return null;
        }
        return quiz;
    }
}
