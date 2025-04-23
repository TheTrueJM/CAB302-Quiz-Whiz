package ai.tutor.cab302exceptionalhandlers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ai.tutor.cab302exceptionalhandlers.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;

import static org.junit.jupiter.api.Assertions.*;

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
        chatController = new ChatController(db);
    }

    @AfterEach
    public void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            Files.deleteIfExists(Paths.get("testing.db"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNewChat() throws SQLException {
        Chat newChat = chatController.createNewChat( "Test Chat", "helpful", "Easy", "High School", "Math");

        assertNotNull(newChat);
        assertEquals("Test Chat", newChat.getName());
    }

    @Test
    public void testGetUserChats() throws SQLException{
        chatController.createNewChat( "Test Chat 1", "helpful", "Easy", "High School", "Math");
        chatController.createNewChat( "Test Chat 2", "helpful", "Medium", "College", "Science");

        List<Chat> userChats = chatController.getUserChats(1);

        assertNotNull(userChats);
        assertEquals(2, userChats.size());
    }

    @Test
    public void testLoadChatMessages() throws SQLException{
        Chat newChat = chatController.createNewChat( "Test Chat", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        List<Message> messages = chatController.loadChatMessages(newChat.getId());
        assertNotNull(messages);
        assertEquals(0, messages.size());
        // TODO: assertEquals("your mom", messages.get(0).getContent());
    }

    @Test
    public void testUpdateChatName() throws SQLException{
        Chat newChat = chatController.createNewChat ( "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat, "Updated Name");
        assertTrue(updated);

        Chat updatedChat = chatDAO.getChat(newChat.getId());
        assertNotNull(updatedChat);
        assertEquals("Updated Name", updatedChat.getName());
    }

    @Test
    public void testUpdateChatNameEmpty() throws SQLException{
        Chat newChat = chatController.createNewChat( "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat, "");
        assertFalse(updated);
    }

    @Test
    public void testUpdateChatNameNull() throws SQLException{
        Chat newChat = chatController.createNewChat( "Initial Name", "helpful", "Easy", "High School", "Math");
        assertNotNull(newChat);

        boolean updated = chatController.updateChatName(newChat, null);
        assertFalse(updated);
    }

    @Test
    public void testCreateNewChatEmptyName() throws SQLException{
        Chat newChat = chatController.createNewChat( "", "helpful", "Easy", "High School", "Math");
        assertNull(newChat);
    }

    @Test
    public void testCreateNewChatNullName() throws SQLException{
        Chat newChat = chatController.createNewChat( null, "helpful", "Easy", "High School", "Math");
        assertNull(newChat);
    }
}
