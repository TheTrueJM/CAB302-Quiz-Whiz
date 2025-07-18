package tests;

import static org.junit.jupiter.api.Assertions.*;

import ai.tutor.cab302exceptionalhandlers.controller.ChatCreateController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatSetupController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatUpdateController;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import ai.tutor.cab302exceptionalhandlers.model.*;

public class ChatSetupControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatDAO chatDAO;
    private ChatCreateController chatCreateController;
    private ChatUpdateController chatUpdateController;

    private static final User CurrentUser = new User(
            "TestUser", User.hashPassword("password")
    );

    private static final Chat ExistingChat = new Chat(
            1, "Existing Chat", "regular", "normal", 3, "University", "IT"
    );

    private static final Chat NewChat = new Chat(
            1, "New Chat", "regular", "normal", 3, "University", "IT"
    );

    private static final Chat UpdatedChat = new Chat(
            1, "Updated Chat", "casual", "hard", 3, "High School", "Maths"
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

        chatCreateController = new ChatCreateController(db, CurrentUser);
        chatUpdateController = new ChatUpdateController(db, CurrentUser, ExistingChat);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testCreateNewChat() throws IllegalArgumentException, SQLException {
        Chat newChat = chatCreateController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertEquals(NewChat.getId(), newChat.getId());
        assertEquals(NewChat.getName(), newChat.getName());
        assertEquals(NewChat.getResponseAttitude(), newChat.getResponseAttitude());
        assertEquals(NewChat.getQuizDifficulty(), newChat.getQuizDifficulty());
        assertEquals(NewChat.getQuizLength(), newChat.getQuizLength());
        assertEquals(NewChat.getEducationLevel(), newChat.getEducationLevel());
        assertEquals(NewChat.getStudyArea(), newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        "", NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        null, NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyAttitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), "", NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullAttitude() {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), null, NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyDifficulty() throws IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), "", NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatNullDifficulty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), null, NewChat.getQuizLength(), NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    @Disabled
    public void testCreateNewChatTooLowLength() throws IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), Chat.MIN_QUIZ_LENGTH -1, NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    @Disabled
    public void testCreateNewChatTooHighLength() throws IllegalArgumentException, SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatCreateController.createNewChat(
                        NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), Chat.MAX_QUIZ_LENGTH + 1, NewChat.getEducationLevel(), NewChat.getStudyArea()
                )
        );
    }

    @Test
    public void testCreateNewChatEmptyEducation() throws IllegalArgumentException, SQLException {
        Chat newChat = chatCreateController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), "", NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatNullEducation() throws IllegalArgumentException, SQLException {
        Chat newChat = chatCreateController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), null, NewChat.getStudyArea()
        );
        assertNotNull(newChat);
        assertNull(newChat.getEducationLevel());
    }

    @Test
    public void testCreateNewChatEmptyStudyArea() throws IllegalArgumentException, SQLException {
        Chat newChat = chatCreateController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), ""
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }

    @Test
    public void testCreateNewChatNullStudyArea() throws IllegalArgumentException, SQLException {
        Chat newChat = chatCreateController.createNewChat(
                NewChat.getName(), NewChat.getResponseAttitude(), NewChat.getQuizDifficulty(), NewChat.getQuizLength(), NewChat.getEducationLevel(), null
        );
        assertNotNull(newChat);
        assertNull(newChat.getStudyArea());
    }


    @Test
    public void testUpdateChat() throws IllegalArgumentException, SQLException {
        chatUpdateController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
        );

        Chat updatedchat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedchat);
        assertEquals(UpdatedChat.getId(), updatedchat.getId());
        assertEquals(UpdatedChat.getName(), updatedchat.getName());
        assertEquals(UpdatedChat.getResponseAttitude(), updatedchat.getResponseAttitude());
        assertEquals(UpdatedChat.getQuizDifficulty(), updatedchat.getQuizDifficulty());
        assertEquals(UpdatedChat.getQuizLength(), updatedchat.getQuizLength());
        assertEquals(UpdatedChat.getEducationLevel(), updatedchat.getEducationLevel());
        assertEquals(UpdatedChat.getStudyArea(), updatedchat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyName() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        "", UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullName() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        null, UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyAttitude() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), "", UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullAttitude() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), null, UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyDifficulty() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), "", UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullDifficulty() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(),null, UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    @Disabled
    public void testUpdateChatTooLowLength() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(),  UpdatedChat.getQuizDifficulty(), Chat.MIN_QUIZ_LENGTH - 1, UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }

    @Test
    @Disabled
    public void testUpdateChatTooHighLength() throws SQLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> chatUpdateController.updateChatDetails(
                        UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), Chat.MAX_QUIZ_LENGTH + 1, UpdatedChat.getEducationLevel(), UpdatedChat.getStudyArea()
                )
        );

        Chat originalChat = chatDAO.getChat(ExistingChat.getId());

        assertNotNull(originalChat);
        assertEquals(ExistingChat.getId(), originalChat.getId());
        assertEquals(ExistingChat.getName(), originalChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), originalChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), originalChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), originalChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), originalChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), originalChat.getStudyArea());
    }


    @Test
    public void testUpdateChatEmptyEducation() throws SQLException {
        chatUpdateController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), "", UpdatedChat.getStudyArea()
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), updatedChat.getQuizLength());
        assertNull(updatedChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullEducation() throws SQLException {
        chatUpdateController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), null, UpdatedChat.getStudyArea()
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), updatedChat.getQuizLength());
        assertNull(updatedChat.getEducationLevel());
        assertEquals(ExistingChat.getStudyArea(), updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatEmptyStudyArea() throws SQLException {
        chatUpdateController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), ""
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), updatedChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), updatedChat.getEducationLevel());
        assertNull(updatedChat.getStudyArea());
    }

    @Test
    public void testUpdateChatNullStudyArea() throws SQLException {
        chatUpdateController.updateChatDetails(
                UpdatedChat.getName(), UpdatedChat.getResponseAttitude(), UpdatedChat.getQuizDifficulty(), UpdatedChat.getQuizLength(), UpdatedChat.getEducationLevel(), null
        );

        Chat updatedChat = chatDAO.getChat(UpdatedChat.getId());

        assertNotNull(updatedChat);
        assertEquals(ExistingChat.getId(), updatedChat.getId());
        assertEquals(ExistingChat.getName(), updatedChat.getName());
        assertEquals(ExistingChat.getResponseAttitude(), updatedChat.getResponseAttitude());
        assertEquals(ExistingChat.getQuizDifficulty(), updatedChat.getQuizDifficulty());
        assertEquals(ExistingChat.getQuizLength(), updatedChat.getQuizLength());
        assertEquals(ExistingChat.getEducationLevel(), updatedChat.getEducationLevel());
        assertNull(updatedChat.getStudyArea());
    }
}
