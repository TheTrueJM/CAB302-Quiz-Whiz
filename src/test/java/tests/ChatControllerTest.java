package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import ai.tutor.cab302exceptionalhandlers.model.*;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;


@Disabled("ChatController not implemented yet")
public class ChatControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatController chatController;

    private static final User CurrentUser = new User(
            "TestUser", User.hashPassword("password")
    );

    private static final Map<String, Chat> Chats = new HashMap<>();
    static {
        Chats.put("chat1", new Chat(1, "Test Chat 1", "regular", "normal", "University", "IT"));
        Chats.put("chat2", new Chat(1, "Test Chat 2", "regular", "normal", "University", "IT"));
        Chats.put("chat3", new Chat(1, "Test Chat 3", "regular", "normal", "University", "IT"));
    }

    private static final Map<String, Message> Messages = new HashMap<>();
    static {
        Messages.put("messageUser", new Message(1, "Message from User", true, false));
        Messages.put("messageAI", new Message(1, "Message from AI", false, false));
        Messages.put("messageUserQuiz", new Message(1, "Quiz Message from User", true, true));
        Messages.put("messageAIQuiz", new Message(1, "Quiz Message from AI", false, true));
    }

    // TODO: Figure out format for AI quiz response
    private static final Map<String, String> QuizContent = new HashMap<>();
    static {
        QuizContent.put("valid", "[Valid Quiz Content Format]");
        QuizContent.put("invalidQuiz", "[Invalid Quiz Content Format]");
        QuizContent.put("invalidQuestion", "[Invalid Quiz Question Content Format]");
        QuizContent.put("invalidAnswer", "[Invalid Quiz Question Answer Option Content Format]");
    }

    private static final Map<String, String> QuestionContent = new HashMap<>();
    static {
        QuestionContent.put("valid", "[Valid Quiz Question Content Format]");
        QuestionContent.put("invalidQuestion", "[Invalid Quiz Question Content Format]");
        QuestionContent.put("invalidAnswer", "[Invalid Quiz Question Answer Option Content Format]");
    }

    private static final Map<String, String> AnswerContent = new HashMap<>();
    static {
        AnswerContent.put("valid", "[Valid Quiz Question Answer Option Content Format]");
        AnswerContent.put("invalid", "[Invalid Quiz Question Answer Option Content Format]");
    }


    @BeforeEach
    public void setUp() throws SQLException, IllegalStateException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        UserDAO userDAO = new UserDAO(db);
        userDAO.createUser(CurrentUser);

        chatController = new ChatController(db, CurrentUser);
    }


    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testCreateNewChat() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertEquals(1, newChat.getId());
        assertEquals(chat.getName(), newChat.getName());
        assertEquals(chat.getResponseAttitude(), newChat.getResponseAttitude());
        assertEquals(chat.getQuizDifficulty(), newChat.getQuizDifficulty());
        assertEquals(chat.getEducationLevel(), newChat.getEducationLevel());
        assertEquals(chat.getStudyArea(), newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatEmptyName() {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        "", chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullName() {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        null, chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyAttitude() {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        chat.getName(), "", chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullAttitude() {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        chat.getName(), null, chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyDifficulty() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        chat.getName(), chat.getResponseAttitude(), "", chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullDifficulty() {
        Chat chat = Chats.get("chat1");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChat(
                        chat.getName(), chat.getResponseAttitude(), null, chat.getEducationLevel(), chat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyEducation() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), "", chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatNullEducation() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), null, chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatEmptyStudyArea() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), ""
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatNullStudyArea() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), null
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testGetUserChats() throws IllegalArgumentException, SQLException {
        for (Chat chat : Chats.values()) {
            chatController.createNewChat(
                    chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }
        List<Chat> userChats = chatController.getUserChats();
        assertNotNull(userChats);
        assertEquals(Chats.size(), userChats.size());
    }

    @Test
    public void testGetNoneUserChats() throws IllegalArgumentException, SQLException {
        List<Chat> userChats = chatController.getUserChats();
        assertNotNull(userChats);
        assertEquals(0, userChats.size());
    }

    @Test
    public void testGetUserChat() throws IllegalArgumentException, NoSuchElementException, SQLException {
        for (Chat chat : Chats.values()) {
            chatController.createNewChat(
                    chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }
        Chat findChat = Chats.get("chat2");
        findChat.setId(2);
        Chat userChat = chatController.getChat(findChat.getId());
        assertNotNull(userChat);
        assertEquals(findChat.getId(), userChat.getId());
    }

    @Test
    public void testGetUserChatInvalidId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChat(1)
        );
    }

    @Test
    public void testGetUserChatInvalidIdRange() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChat(-1)
        );
    }

    @Test
    public void testUpdateChatName() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        String updatedName = "Updated Name";
        chatController.updateChatName(newChat.getId(), updatedName);

        Chat updatedChat = chatController.getChat(newChat.getId());
        assertNotNull(updatedChat);
        assertEquals(updatedName, updatedChat.getName());
    }

    @Test
    public void testUpdateChatNameEmpty() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.updateChatName(newChat.getId(), "")
        );


        Chat originalChat = chatController.getChat(newChat.getId());
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameNull() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.updateChatName(newChat.getId(), null)
        );

        Chat originalChat = chatController.getChat(newChat.getId());
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameInvalidId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.updateChatName(1, "New Chat Name")
        );
    }

    @Test
    public void testUpdateChatNameInvalidIdRange() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.updateChatName(-1, "New Chat Name")
        );
    }

    @Test
    public void testCreateNewChatMessage() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageUser");
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(newMessage);
        assertEquals(1, newMessage.getId());
        assertEquals(message.getChatId(), newMessage.getChatId());
        assertEquals(message.getContent(), newMessage.getContent());
        assertEquals(message.getFromUser(), newMessage.getFromUser());
        assertEquals(message.getIsQuiz(), newMessage.getIsQuiz());
    }

    @Test
    public void testCreateNewChatMessageInvalidChatId() {
        Message message = Messages.get("messageUser");
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.createNewChatMessage(
                        1, message.getContent(), message.getFromUser(), message.getIsQuiz()
                )
        );
    }

    @Test
    public void testCreateNewChatMessageInvalidChatIdRange() {
        Message message = Messages.get("messageUser");
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.createNewChatMessage(
                        -1, message.getContent(), message.getFromUser(), message.getIsQuiz()
                )
        );
    }

    @Test
    public void testCreateNewChatMessageEmptyContent() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageUser");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChatMessage(
                        message.getChatId(), "", message.getFromUser(), message.getIsQuiz()
                )
        );
    }

    @Test
    public void testCreateNewChatMessageNullContent() throws IllegalArgumentException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageUser");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChatMessage(
                        message.getChatId(), null, message.getFromUser(), message.getIsQuiz()
                )
        );
    }

    @Test
    public void testGenerateChatMessageResponse() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageUser");
        Message userMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(userMessage);

        Message responseMessage = chatController.generateChatMessageResponse(userMessage);
        assertNotNull(responseMessage);
        assertEquals(userMessage.getId() + 1, responseMessage.getId());
        assertEquals(userMessage.getChatId(), responseMessage.getChatId());
        assertNotNull(responseMessage.getContent());
        assertFalse(responseMessage.getFromUser());
        assertEquals(userMessage.getIsQuiz(), responseMessage.getIsQuiz());
    }

    @Test
    public void testGenerateChatMessageResponseInvalidFromAI() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAI");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(aiMessage);

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.generateChatMessageResponse(aiMessage)
        );
    }


    @Test
    public void testGetChatMessages() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);


        for (Message message : Messages.values()) {
            chatController.createNewChatMessage(
                    message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
            );
        }

        List<Message> messages = chatController.getChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(Messages.size(), messages.size());
    }

    @Test
    public void testGetNoneChatMessages() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        List<Message> messages = chatController.getChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    public void testGetChatMessagesInvalidChatId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChatMessages(1)
        );
    }

    @Test
    public void testGetChatMessagesInvalidChatIdRange() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChatMessages(-1)
        );
    }

    @Test
    public void testCreateNewQuiz() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(aiMessage);

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );
        assertNotNull(newQuiz);
        assertEquals(aiMessage.getId(), newQuiz.getMessageId());
    }

    @Test
    public void testCreateNewQuizInvalidNotQuiz() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAI");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuiz(
                        QuizContent.get("valid"), aiMessage
                )
        );
    }

    @Test
    public void testCreateNewQuizInvalidFromUser() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageUserQuiz");
        Message userMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuiz(
                        QuizContent.get("valid"), userMessage
                )
        );
    }

    @Test
    public void testCreateNewQuizInvalidQuizContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuiz(
                        QuizContent.get("invalidQuiz"), aiMessage
                )
        );
    }

    @Test
    public void testCreateNewQuizInvalidQuestionContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuiz(
                        QuizContent.get("invalidQuestion"), aiMessage
                )
        );
    }

    @Test
    public void testCreateNewQuizInvalidAnswerContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuiz(
                        QuizContent.get("invalidAnswer"), aiMessage
                )
        );
    }

    @Test
    public void testCreateNewQuizQuestion() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );
        assertNotNull(newQuiz);

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid"), newQuiz
        );
        assertNotNull(newQuizQuestion);
        assertEquals(newQuiz.getMessageId(), newQuizQuestion.getMessageId());
    }

    @Test
    public void testCreateNewQuizQuestionExistingNumber() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid"), newQuiz
        );
        assertNotNull(newQuizQuestion);

        assertThrows(
                IllegalStateException.class,
                () -> chatController.createNewQuizQuestion(
                        QuestionContent.get("valid"), newQuiz
                )
        );
    }

    @Test
    public void testCreateNewQuizQuestionInvalidQuestionContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuizQuestion(
                        QuestionContent.get("invalidQuestion"), newQuiz
                )
        );
    }

    @Test
    public void testCreateNewQuizQuestionInvalidAnswerContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuizQuestion(
                        QuestionContent.get("invalidAnswer"), newQuiz
                )
        );

    }

    @Test
    public void testCreateNewQuizQuestionAnswer() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid"), newQuiz
        );
        assertNotNull(newQuizQuestion);

        AnswerOption newAnswerOption = chatController.createNewQuestionAnswerOption(
                AnswerContent.get("valid"), newQuizQuestion
        );
        assertNotNull(newAnswerOption);
        assertEquals(newQuizQuestion.getMessageId(), newAnswerOption.getMessageId());
        assertEquals(newQuizQuestion.getNumber(), newAnswerOption.getQuestionNumber());
    }

    @Test
    public void testCreateNewQuizQuestionAnswerExistingAnswer() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid"), newQuiz
        );

        chatController.createNewQuestionAnswerOption(
                AnswerContent.get("valid"), newQuizQuestion
        );

        assertThrows(
                IllegalStateException.class,
                () -> chatController.createNewQuestionAnswerOption(
                        AnswerContent.get("valid"), newQuizQuestion
                )
        );
    }


    @Test
    public void testCreateNewQuizQuestionAnswerInvalidContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Chat chat = Chats.get("chat1");
        chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid"), newQuiz
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuestionAnswerOption(
                        QuestionContent.get("invalid"), newQuizQuestion
                )
        );
    }
}
