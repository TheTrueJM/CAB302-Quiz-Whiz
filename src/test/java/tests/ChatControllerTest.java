package tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("ChatController not implemented yet")
public class ChatControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatController chatController;

    private static final User[] Users = {
            new User("TestUser1", "password"),
            new User("TestUser2", "password"),
    };

    private static final Chat[] Chats = {
            new Chat(1, "Test Chat 1", "regular", "normal", "University", "IT"),
            new Chat(1, "Test Chat 2", "regular", "normal", "University", "IT"),
            new Chat(2, "Test Chat 3", "regular", "normal", "University", "IT")
    };


    @BeforeEach
    public void setUp() throws SQLException {
        db = new SQLiteConnection(true);
        connection = db.getInstance();
        chatController = new ChatController(db);
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
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        assertNotNull(newChat);
        assertEquals(1, newChat.getId()); // 0 or 1 for first autoIncrement ID?
    }

    @Test
    public void testCreateNewChatEmptyName() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), "", chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatNullName() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), null, chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        assertNull(newChat);
    }

    @Test
    public void testGetUserChats() {
        for (Chat chat : Chats) {
            chatController.createNewChat(
                    chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }

        int userId = 1;
        long userChatsCount = Arrays.stream(Chats).filter(chat -> chat.getUserId() == userId).count();
        List<Chat> userChats = chatController.getUserChats(userId);

        assertNotNull(userChats);
        assertEquals(userChatsCount, userChats.size());
    }

    @Test
    public void testGetUserChat() {
        Chat newChat = null;
        for (Chat chat : Chats) {
            newChat = chatController.createNewChat(
                    chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }

        if (newChat != null) {
            Chat userChat = chatController.getChat(newChat.getId());

            assertNotNull(userChat);
            assertEquals(newChat.getId(), userChat.getId());
        }
    }

    @Test
    public void testGetUserChatInvalidId() {
        Chat userChat = chatController.getChat(-1);
        assertNull(userChat);
    }

    @Test
    public void testLoadNoneChatMessages() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        List<Message> messages = chatController.loadChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(0, messages.size());
        // TODO: assertEquals("your mom", messages.get(0).getContent());
    }

    @Test
    public void testUpdateChatName() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
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
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), "");
        assertFalse(updated);

        Chat unupdatedChat = chatController.getChat(newChat.getId());
        assertNotNull(unupdatedChat);
        assertEquals(chat.getName(), unupdatedChat.getName());
    }

    @Test
    public void testUpdateChatNameNull() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), null);
        assertFalse(updated);

        Chat unupdatedChat = chatController.getChat(newChat.getId());
        assertNotNull(unupdatedChat);
        assertEquals(chat.getName(), unupdatedChat.getName());
    }

    @Test
    public void testUpdateChatNameInvalidId() {
        boolean updated = chatController.updateChatName(-1, "New Chat Name");
        assertFalse(updated);
    }
}
