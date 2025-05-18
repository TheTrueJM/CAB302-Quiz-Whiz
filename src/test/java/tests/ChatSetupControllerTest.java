package tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import ai.tutor.cab302exceptionalhandlers.controller.ChatSetupController;
import ai.tutor.cab302exceptionalhandlers.controller.LoginController;
import ai.tutor.cab302exceptionalhandlers.controller.SignUpController;
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

public class ChatSetupControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatDAO chatDAO;
    private ChatSetupController createChatSetupController;
    private ChatSetupController updateChatSetupController;

    private static final User CurrentUser = new User(
            "TestUser", User.hashPassword("password")
    );

    private static final Chat ExistingChat = new Chat(
            1, "Existing Chat", "regular", "normal", "University", "IT"
    );

    private static final Chat NewChat = new Chat(
            1, "New Chat", "regular", "normal", "University", "IT"
    );

    private static final Chat UpdatedChat = new Chat(
            1, "Updated Chat", "casual", "hard", "High School", "Maths"
    );

    static {
        ExistingChat.setId(1);
        NewChat.setId(2);
        UpdatedChat.setId(ExistingChat.getId());
    }

    private static final Map<String, Message> Messages = new HashMap<>();
    static {
        Messages.put("messageUser", new Message(1, "Message from User", true, false));
        Messages.put("messageAI", new Message(1, "Message from AI", false, false));
        Messages.put("messageUserQuiz", new Message(1, "Quiz Message from User", true, true));
        Messages.put("messageAIQuiz", new Message(1, "Quiz Message from AI", false, true));
    }

    @BeforeEach
    public void setUp(TestInfo testInfo) throws SQLException, IllegalStateException, IOException {
        System.out.println("Running test: " + testInfo.getDisplayName());
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        UserDAO userDAO = new UserDAO(db);
        userDAO.createUser(CurrentUser);

        chatDAO = new ChatDAO(db);
        chatDAO.createChat(ExistingChat);

        MessageDAO messageDAO = new MessageDAO(db);
        for (Message message : Messages.values()) {
            messageDAO.createMessage(message);
        }

        createChatSetupController = new ChatSetupController(db, CurrentUser);
        updateChatSetupController = new ChatSetupController(db, CurrentUser, ExistingChat);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testCreateNewChat() throws IllegalArgumentException, SQLException {
        Chat newChat = createChatSetupController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertEquals(NewChat.getId(), newChat.getId());
        assertEquals(NewChat.getName(), newChat.getName());
        assertEquals(NewChat.getResponseAttitude(), newChat.getResponseAttitude());
        assertEquals(NewChat.getQuizDifficulty(), newChat.getQuizDifficulty());
        assertEquals(NewChat.getEducationLevel(), newChat.getEducationLevel());
        assertEquals(NewChat.getStudyArea(), newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        "", NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        null, NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyAttitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        NewChat.getName(), "", NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullAttitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        NewChat.getName(), null, NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyDifficulty() throws IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), "", NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullDifficulty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createChatSetupController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), null, NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyEducation() throws IllegalArgumentException, SQLException {
        Chat newChat = createChatSetupController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), "", NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatNullEducation() throws IllegalArgumentException, SQLException {
        Chat newChat = createChatSetupController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), null, NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatEmptyStudyArea() throws IllegalArgumentException, SQLException {
        Chat newChat = createChatSetupController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), ""
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatNullStudyArea() throws IllegalArgumentException, SQLException {
        Chat newChat = createChatSetupController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getEducationLevel(), null
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }


    @Test
    public void testUpdateChat() throws IllegalArgumentException, SQLException {
        updateChatSetupController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
        );

        Chat updatedchat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedchat);
        assertEquals(UpdatedChat.getId(), updatedchat.getId());
        assertEquals(UpdatedChat.getName(), updatedchat.getName());
        assertEquals(UpdatedChat.getResponseAttitude(), updatedchat.getResponseAttitude());
        assertEquals(UpdatedChat.getQuizDifficulty(), updatedchat.getQuizDifficulty());
        assertEquals(UpdatedChat.getEducationLevel(), updatedchat.getEducationLevel());
        assertEquals(UpdatedChat.getStudyArea(), updatedchat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyName() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        "", UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullName() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        null, UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyAttitude() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        UpdatedChat.getName(), "", UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullAttitude() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        UpdatedChat.getName(), null, UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyDifficulty() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), "", UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullDifficulty() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> updateChatSetupController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(),null, UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyEducation() throws SQLException {
        updateChatSetupController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), "", UpdatedChat.getStudyArea()
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertNull(updatedChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullEducation() throws SQLException {
        updateChatSetupController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), null, UpdatedChat.getStudyArea()
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertNull(updatedChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyStudyArea() throws SQLException {
        updateChatSetupController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), ""
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), updatedChat.getEducationLevel());
        assertNull(updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullStudyArea() throws SQLException {
        updateChatSetupController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getEducationLevel(), null
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getEducationLevel(), updatedChat.getEducationLevel());
        assertNull(updatedChat.getStudyArea());
    }
}
