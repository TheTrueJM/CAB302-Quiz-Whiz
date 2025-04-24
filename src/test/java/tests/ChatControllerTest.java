package tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.tutor.cab302exceptionalhandlers.model.*;
import org.junit.jupiter.api.*;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("ChatController not implemented yet")
public class ChatControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatController chatController;

    private static final User User = new User("TestUser", "password");

    private static final Map<String, Chat> Chats = new HashMap<>();
    static {
        Chats.put("Chat1", new Chat(1, "Test Chat 1", "regular", "normal", "University", "IT"));
        Chats.put("Chat2", new Chat(1, "Test Chat 2", "regular", "normal", "University", "IT"));
        Chats.put("Chat3", new Chat(1, "Test Chat 3", "regular", "normal", "University", "IT"));
    }

    private static final Map<String, Message> Messages = new HashMap<>();
    static {
        Messages.put("MessageUser", new Message(1, "Message from User", true, false));
        Messages.put("MessageAI", new Message(1, "Message from AI", false, false));
        Messages.put("MessageUserQuiz", new Message(1, "Quiz Message from User", true, true));
        Messages.put("MessageAIQuiz", new Message(1, "Quiz Message from AI", false, true));
    }


    @BeforeEach
    public void setUp() throws SQLException, IllegalStateException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        UserDAO userDAO = new UserDAO(db);
        userDAO.createUser(User);

        chatController = new ChatController(db, User);
    }


    @AfterEach
    public void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateNewChat() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertEquals(1, newChat.getId());
    }

    @Test
    public void testCreateNewChatEmptyName() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                "", chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatNullName() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                null, chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatEmptyAttitude() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), "", chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatNullAttitude() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), null, chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatEmptyDifficulty() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), "", chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatNullDifficulty() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), null, chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatEmptyEducation() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), "", chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatNullEducation() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), null, chat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatEmptyStudyArea() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), ""
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatNullStudyArea() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), null
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testGetUserChats() {
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
    public void testGetNoneUserChats() {
        List<Chat> userChats = chatController.getUserChats();
        assertNotNull(userChats);
        assertEquals(0, userChats.size());
    }

    @Test
    public void testGetUserChat() {
        for (Chat chat : Chats.values()) {
            chatController.createNewChat(
                    chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }
        Chat findChat = Chats.get("Chat2");
        Chat userChat = chatController.getChat(findChat.getId());
        assertNotNull(userChat);
        assertEquals(findChat.getId(), userChat.getId());
    }

    @Test
    public void testGetUserChatInvalidId() {
        Chat userChat = chatController.getChat(-1);
        assertNull(userChat);
    }

    @Test
    public void testUpdateChatName() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        String updatedName = "Updated Name";
        boolean updated = chatController.updateChatName(newChat.getId(), updatedName);
        assertTrue(updated);

        Chat updatedChat = chatController.getChat(newChat.getId());
        assertNotNull(updatedChat);
        assertEquals(updatedName, updatedChat.getName());
    }

    @Test
    public void testUpdateChatNameEmpty() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), "");
        assertFalse(updated);

        Chat originalChat = chatController.getChat(newChat.getId());
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameNull() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), null);
        assertFalse(updated);

        Chat originalChat = chatController.getChat(newChat.getId());
        assertNotNull(originalChat);
        assertEquals(chat.getName(), originalChat.getName());
    }

    @Test
    public void testUpdateChatNameInvalidId() {
        boolean updated = chatController.updateChatName(-1, "New Chat Name");
        assertFalse(updated);
    }

    @Test
    public void testCreateNewChatMessage() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageUser");
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(newMessage);
        assertEquals(1, newMessage.getId());
    }

    @Test
    public void testCreateNewChatMessageInvalidChatId() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageUser");
        Message newMessage = chatController.createNewChatMessage(
                -1, message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNull(newMessage);
    }


    @Test
    public void testCreateNewChatMessageEmptyContent() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageUser");
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), "", message.getFromUser(), message.getIsQuiz()
        );
        assertNull(newMessage);
    }

    @Test
    public void testCreateNewChatMessageNullContent() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageUser");
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), null, message.getFromUser(), message.getIsQuiz()
        );
        assertNull(newMessage);
    }

    @Test
    public void testGenerateChatMessageResponse() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageUser");
        Message userMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(userMessage);

        Message responseMessage = chatController.generateChatMessageResponse(userMessage);
        assertNotNull(responseMessage);
        assertEquals(2, responseMessage.getId());
        assertFalse(responseMessage.getFromUser());
    }

    @Test
    public void testGenerateChatMessageResponseInvalidFromAI() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages.get("MessageAI");
        Message AIMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );
        assertNotNull(AIMessage);

        Message responseMessage = chatController.generateChatMessageResponse(AIMessage);
        assertNull(responseMessage);
    }


    @Test
    public void testGetChatMessages() {
        Chat chat = Chats.get("Chat1");
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
    public void testGetNoneChatMessages() {
        Chat chat = Chats.get("Chat1");
        Chat newChat = chatController.createNewChat(
                chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        List<Message> messages = chatController.getChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(0, messages.size());
    }
}
