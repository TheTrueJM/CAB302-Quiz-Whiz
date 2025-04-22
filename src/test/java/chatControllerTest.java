package ai.tutor.cab302exceptionalhandlers.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.sql.Connection;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;

import ai.tutor.cab302exceptionalhandlers.model.ChatDAO;
import ai.tutor.cab302exceptionalhandlers.model.MessageDAO;
import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;

public class chatControllerTest {
    private Connection connection;
    private SQLiteConnection db;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private ChatController chatController;

    @BeforeEach
    public void setUp() {
        db = new SQLiteConnection("testing");
        connection = db.getInstance();
        chatDAO = new ChatDAO(db);
        messageDAO = new MessageDAO(db);
        chatController = new ChatController(chatDAO, messageDAO);
    }

    @AfterEach
    public void tearDown() {
        try {
            // Remote database file
            String dbFilePath = "testing.db";
            connection.close();
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(dbFilePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNewChat() {
        Chat newChat = chatController.createNewChat(1, "Test Chat", "helpful", "Easy", "High School", "Math");

        assertNotNull(newChat);
        assertEquals("Test Chat", newChat.getName());
    }

    @Test
    public void testGetUserChats() {
        chatController.createNewChat(1, "Test Chat 1", "helpful", "Easy", "High School", "Math");
        chatController.createNewChat(1, "Test Chat 2", "helpful", "Medium", "College", "Science");

        List<Chat> userChats = chatController.getUserChats(1);

        assertNotNull(userChats);
        assertEquals(2, userChats.size());
    }

    @Test
    public void testLoadChatMessages() {
        Chat newChat = chatController.createNewChat(1, "Test Chat", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        List<Message> messages = chatController.loadChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(0, messages.size());
        // TODO: assertEquals("your mom", messages.get(0).getContent());
    }

    @Test
    public void testUpdateChatName() {
        Chat newChat = chatController.createNewChat(1, "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), "Updated Name");
        assertEquals(true, updated);

        Chat updatedChat = chatDAO.getChat(newChat.getId());
        assertNotNull(updatedChat);
        assertEquals("Updated Name", updatedChat.getName());
    }

    @Test
    public void testUpdateChatNameEmpty() {
        Chat newChat = chatController.createNewChat(1, "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), "");
        assertEquals(false, updated);
    }

    @Test
    public void testUpdateChatNameNull() {
        Chat newChat = chatController.createNewChat(1, "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat.getId(), null);
        assertEquals(false, updated);
    }

    @Test
    public void testUpdateChatNameInvalidId() {
        boolean updated = chatController.updateChatName(-1, "New Name");
        assertEquals(false, updated);
    }

    @Test
    public void testCreateNewChatEmptyName() {
        Chat newChat = chatController.createNewChat(1, "", "helpful", "Easy", "High School", "Math");
        assertEquals(null, newChat);
    }

    @Test
    public void testCreateNewChatNullName() {
        Chat newChat = chatController.createNewChat(1, null, "helpful", "Easy", "High School", "Math");
        assertEquals(null, newChat);
    }
}
