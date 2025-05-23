package tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.controller.AIController.*;

import com.google.gson.Gson;

public class ChatControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatController chatController;
    private boolean isOllamaRunning = false;
    private boolean hasCorrectModel = false;

    private static final Gson gson = new Gson();

    private static final User CurrentUser = new User(
            "TestUser", User.hashPassword("password")
    );

    private static final Chat[] Chats = {
        new Chat(1, "Test Chat 1", "regular", "normal", 3, "University", "IT"),
        new Chat(1, "Test Chat 2", "regular", "normal", 3, "University", "IT"),
        new Chat(1, "Test Chat 3", "regular", "normal", 3, "University", "IT")
    };

    private static final Map<String, Message> Messages = new HashMap<>();
    static {
        Messages.put("messageUser", new Message(1, "Message from User", true, false));
        Messages.put("messageAI", new Message(1, "Message from AI", false, false));
        Messages.put("messageUserQuiz", new Message(1, "Quiz Message from User", true, true));
        Messages.put("messageAIQuiz", new Message(1, "Quiz Message from AI", false, true));
    }

    private static ModelResponseFormat loadModelResponseFromResource(String resourcePath) {
        try (InputStream is = ChatControllerTest.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(jsonContent, ModelResponseFormat.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load quiz content: " + e.getMessage(), e);
        }
    }

    private static final Map<String, ModelResponseFormat> QuizContent = new HashMap<>();
    static {
        QuizContent.put("valid", loadModelResponseFromResource("/ai/tutor/testdata/valid_quiz.json"));
        QuizContent.put("invalidQuiz", loadModelResponseFromResource("/ai/tutor/testdata/invalid_quiz.json"));
        QuizContent.put("invalidQuestion", loadModelResponseFromResource("/ai/tutor/testdata/invalid_question.json"));
        QuizContent.put("invalidAnswer", loadModelResponseFromResource("/ai/tutor/testdata/invalid_answer.json"));
    }

    private static final Map<String, Question[]> QuestionContent = new HashMap<>();
    static {
        QuestionContent.put("valid", getQuestionsFromResponse(QuizContent.get("valid")));
        QuestionContent.put("invalidQuestion", getQuestionsFromResponse(QuizContent.get("invalidQuestion")));
        QuestionContent.put("invalidAnswer", getQuestionsFromResponse(QuizContent.get("invalidAnswer")));
    }

    private static final Map<String, Option> AnswerContent = new HashMap<>();
    static {
        AnswerContent.put("valid", getFirstOptionFromQuiz("valid"));
        AnswerContent.put("invalid", getFirstOptionFromQuiz("invalidAnswer"));
    }

    /* ----- */

    private static Question[] getQuestionsFromResponse(ModelResponseFormat response) {
        if (response != null && response.quizzes != null && response.quizzes.length > 0) {
            return response.quizzes[0].getQuestions();
        } else {
            return new Question[0];
        }
    }

    private static Option getFirstOptionFromQuiz(String key) {
        ModelResponseFormat response = QuizContent.get(key);
        if (response != null && response.quizzes != null && response.quizzes.length > 0) {
            QuizFormat quiz = response.quizzes[0];
            return extractFirstOption(quiz);
        }
        return null;
    }

    private static Option extractFirstOption(QuizFormat quiz) {
        if (quiz != null) {
            Question[] questions = quiz.getQuestions();
            if (questions != null && questions.length > 0) {
                Option[] options = questions[0].getOptions();
                if (options != null && options.length > 0) {
                    return options[0];
                }
            }
        }
        return null;
    }

    /* ----- */

    @BeforeEach
    public void setUp(TestInfo testInfo) throws SQLException, IllegalStateException, IOException {
        System.out.println("Running test: " + testInfo.getDisplayName());
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        UserDAO userDAO = new UserDAO(db);
        userDAO.createUser(CurrentUser);

        ChatDAO chatDAO = new ChatDAO(db);
        for (Chat chat : Chats) {
            chatDAO.createChat(chat);
        }

        chatController = new ChatController(db, CurrentUser);
        isOllamaRunning = chatController.isOllamaRunning();
        hasCorrectModel = chatController.hasModel();
        chatController.setOllamaVerbose(true);

        if (!hasCorrectModel && isOllamaRunning) {
                fail(String.format(
                        "You need to download the correct model by running `ollama pull %s'", chatController.getModelName()
                ));
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    @Test
    public void testGetUserChats() throws IllegalArgumentException, SQLException {
        List<Chat> userChats = chatController.getUserChats();
        assertNotNull(userChats);
        assertEquals(Chats.length, userChats.size());
    }

    @Disabled
    // TODO: Need a User that has No Chats
    public void testGetNoneUserChats() throws IllegalArgumentException, SQLException {
        List<Chat> userChats = chatController.getUserChats();
        assertNotNull(userChats);
        assertEquals(0, userChats.size());
    }

    @Test
    public void testGetUserChat() throws IllegalArgumentException, NoSuchElementException, SQLException {
        int chatId = 1;
        Chat findChat = Chats[chatId - 1];
        Chat userChat = chatController.getChat(chatId);
        assertNotNull(userChat);
        assertEquals(findChat.getId(), userChat.getId());
    }

    @Test
    public void testGetUserChatInvalidId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChat(Chats.length + 1)
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
        int chatId = 1;
        String updatedName = "Updated Name";
        chatController.updateChatName(chatId, updatedName);

        Chat updatedChat = chatController.getChat(chatId);
        assertNotNull(updatedChat);
        assertEquals(updatedName, updatedChat.getName());
    }

    @Test
    public void testUpdateChatNameEmpty() throws IllegalArgumentException, NoSuchElementException, SQLException {
        int chatId = 1;
        Chat chat = Chats[chatId - 1];

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.updateChatName(chatId, "")
        );


        Chat originalChat = chatController.getChat(chatId);
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameNull() throws IllegalArgumentException, NoSuchElementException, SQLException {
        int chatId = 1;
        Chat chat = Chats[chatId - 1];

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.updateChatName(chatId, null)
        );

        Chat originalChat = chatController.getChat(chatId);
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameInvalidId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.updateChatName(Chats.length + 1, "New Chat Name")
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
                        Chats.length + 1, message.getContent(), message.getFromUser(), message.getIsQuiz()
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
        Message message = Messages.get("messageUser");
        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewChatMessage(
                        message.getChatId(), null, message.getFromUser(), message.getIsQuiz()
                )
        );
    }

    @Disabled
    public void testGenerateChatMessageResponse() throws IllegalArgumentException, NoSuchElementException, SQLException {
        assumeTrue(isOllamaRunning, "Ollama is not running");

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
    public void testMultiTurnChatMessageResponse() throws IllegalArgumentException, NoSuchElementException, SQLException {
        assumeTrue(isOllamaRunning, "Ollama is not running");

        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getQuizLength(), chat.getEducationLevel(), chat.getStudyArea()
        );
        int chatID = newChat.getId();

        /* First User Message */
        Message firstUserMessage = chatController.createNewChatMessage(chatID, "My favourite number is 5, remember that.", true, false);

        /* First AI Response */
        Message firstResponse = chatController.generateChatMessageResponse(firstUserMessage);

        /* Second User Message */
        Message secondUserMessage = chatController.createNewChatMessage(chatID, "So what is my favourite number?", true, false);

        /* Second AI Response */
        Message secondResponse = chatController.generateChatMessageResponse(secondUserMessage);

        assertEquals(firstUserMessage.getChatId(), secondResponse.getChatId());
        assertEquals(firstUserMessage.getId() + 1, firstResponse.getId());
        assertEquals(firstResponse.getId() + 1, secondUserMessage.getId());
        assertEquals(secondUserMessage.getId() + 1, secondResponse.getId());
    }

    @Test
    public void testGenerateChatMessageResponseInvalidFromAI() throws IllegalArgumentException, NoSuchElementException, SQLException {
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
        int chatId = 1;

        for (Message message : Messages.values()) {
            chatController.createNewChatMessage(
                    message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
            );
        }

        List<Message> messages = chatController.getChatMessages(chatId);
        assertNotNull(messages);
        assertEquals(Messages.size(), messages.size());
    }

    @Test
    public void testGetNoneChatMessages() throws IllegalArgumentException, NoSuchElementException, SQLException {
        int chatId = 1;

        List<Message> messages = chatController.getChatMessages(chatId);
        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    public void testGetChatMessagesInvalidChatId() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChatMessages(Chats.length + 1)
        );
    }

    @Test
    public void testGetChatMessagesInvalidChatIdRange() {
        assertThrows(
                NoSuchElementException.class,
                () -> chatController.getChatMessages(-1)
        );
    }

    @Disabled
    public void testGenerateQuiz() throws IllegalArgumentException, NoSuchElementException, SQLException {
        assumeTrue(isOllamaRunning, "Ollama is not running");
        assumeTrue(hasCorrectModel, "Required model is not available");

        int chatId = 1;

        // Send a regular message
        Message userMessage = chatController.createNewChatMessage(
                chatId, "Tell me about object-oriented programming principles", true, false
        );
        assertNotNull(userMessage);

        // Generate AI response
        Message aiResponse = chatController.generateChatMessageResponse(userMessage);
        assertNotNull(aiResponse);
        assertFalse(aiResponse.getIsQuiz());

        // Switch to quiz mode and request a quiz
        chatController.setQuizMode(true);

        // Create a message requesting a quiz
        Message quizRequest = chatController.createNewChatMessage(
                chatId, "Create a quiz about object-oriented programming", true, true
        );
        assertNotNull(quizRequest);
        assertTrue(quizRequest.getIsQuiz());

        // Generate AI response in quiz mode
        Message quizResponse = chatController.generateChatMessageResponse(quizRequest);
        assertNotNull(quizResponse);
        assertTrue(quizResponse.getIsQuiz());
        assertFalse(quizResponse.getFromUser());

        Quiz quiz = null;
        quiz = chatController.getQuizForMessage(quizResponse.getId());
        assertNotNull(quiz);
        assertEquals(quizResponse.getId(), quiz.getMessageId());
    }

    @Test
    public void testCreateNewQuiz() throws IllegalArgumentException, NoSuchElementException, SQLException {
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

    @Disabled
    public void testCreateNewQuizQuestion() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );
        assertNotNull(newQuiz);

        String questionStrContent = QuestionContent.get("valid")[0].getQuestionContent();
        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                questionStrContent, newQuiz
        );
        assertNotNull(newQuizQuestion);
        assertEquals(newQuiz.getMessageId(), newQuizQuestion.getMessageId());
    }

    @Disabled("Only implement if question number is extracted from quiz question content\nOtherwise remove test case")
    @Test
    public void testCreateNewQuizQuestionExistingNumber() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                QuestionContent.get("valid")[0].getQuestionContent(), newQuiz
        );
        assertNotNull(newQuizQuestion);

        String questionStrContent = QuestionContent.get("valid")[0].getQuestionContent();

        assertThrows(
                IllegalStateException.class,
                () -> chatController.createNewQuizQuestion(
                        questionStrContent, newQuiz
                )
        );
    }

    @Disabled
    public void testCreateNewQuizQuestionInvalidQuestionContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        String questionStrContent = QuestionContent.get("invalidQuestion")[0].getQuestionContent();

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuizQuestion(
                        questionStrContent, newQuiz
                )
        );
    }

    @Test
    public void testCreateNewQuizQuestionInvalidAnswerContent() throws IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        System.out.println(message.getIsQuiz());
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );


        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuizQuestion(
                        "", newQuiz
                )
        );

    }

    @Test
    public void testCreateNewQuizQuestionAnswer() throws IllegalStateException, IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        String questionStrContent = QuestionContent.get("valid")[0].getQuestionContent();
        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                questionStrContent, newQuiz
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
    public void testCreateNewQuizQuestionAnswerExistingAnswer() throws IllegalStateException, IllegalArgumentException, NoSuchElementException, SQLException {
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        String questionStrContent = QuestionContent.get("valid")[0].getQuestionContent();
        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                questionStrContent, newQuiz
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
        Message message = Messages.get("messageAIQuiz");
        Message aiMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        Quiz newQuiz = chatController.createNewQuiz(
                QuizContent.get("valid"), aiMessage
        );

        String questionStrContent = QuestionContent.get("valid")[0].getQuestionContent();
        QuizQuestion newQuizQuestion = chatController.createNewQuizQuestion(
                questionStrContent, newQuiz
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatController.createNewQuestionAnswerOption(
                        AnswerContent.get("invalid"), newQuizQuestion
                )
        );
    }
}
