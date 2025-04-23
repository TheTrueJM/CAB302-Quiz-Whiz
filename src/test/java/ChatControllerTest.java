import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import ai.tutor.cab302exceptionalhandlers.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;

import static org.junit.jupiter.api.Assertions.*;

public class ChatControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private ChatController chatController;

    private static final User[] Users = {
            new User("TestUser1", "password"),
            new User("TestUser2", "password")
    };

    private static final Chat[] Chats = {
            new Chat(1, "Test Chat 1", "regular", "normal", "University", "IT"),
            new Chat(1, "Test Chat 2", "regular", "normal", "University", "IT"),
            new Chat(2, "Test Chat 3", "regular", "normal", "University", "IT")
    };

    private static final Message[] Messages = {
            new Message(1, "Chat Message 1", true, false),
            new Message(1, "Chat Message 2", false, true),
            new Message(1, "Chat Message 3", true, false),
            new Message(1, "Chat Message 4", false, false),
            new Message(2, "Chat Message 1", true, false),
            new Message(2, "Chat Message 2", false, false),
    };


    @BeforeEach
    public void setUp() {
        try {
            db = new SQLiteConnection("testing");
            connection = db.getInstance();
            chatController = new ChatController();

            UserDAO userDAO = new UserDAO(db);
            for (User user : Users) { userDAO.createUser(user); }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void testCreateNewChat() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        assertNotNull(newChat);
        assertEquals(1, newChat.getId()); // 0 or 1 for first autoIncrement ID?
    }

    @Test
    public void testCreateNewChatInvalidUserId() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                -1, chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );

        assertNull(newChat);
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

        Chat chat = Chats[0];
        long userChatsCount = Arrays.stream(Chats).filter(userChat -> userChat.getUserId() == chat.getUserId()).count();

        List<Chat> userChats = chatController.getUserChats(chat.getUserId());

        assertNotNull(userChats);
        assertEquals(userChatsCount, userChats.size());
    }

    @Test
    public void testGetUserChatsInvalidUserId() {
        for (Chat chat : Chats) {
            chatController.createNewChat(
                    chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }

        List<Chat> userChats = chatController.getUserChats(-1);

        assertNotNull(userChats);
        assertEquals(0, userChats.size());
    }

    @Test
    public void testGetUserChat() {
        for (Chat chat : Chats) {
            chatController.createNewChat(
                    chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }

        Chat chat = Chats[0];
        Chat userChat = chatController.getChat(chat.getId());

        assertNotNull(userChat);
        assertEquals(chat.getId(), userChat.getId());
    }

    @Test
    public void testGetUserChatInvalidId() {
        Chat userChat = chatController.getChat(-1);
        assertNull(userChat);
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

    @Test
    public void testCreateNewChatMessage() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertNotNull(newMessage);
        assertEquals(1, newMessage.getId()); // 0 or 1 for first autoIncrement ID?
    }

    @Test
    public void testCreateNewChatMessageInvalidChatId() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                -1, message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        assertNull(newMessage);
    }

    @Test
    public void testCreateNewChatMessageEmptyContent() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), "", message.getFromUser(), message.getIsQuiz()
        );

        assertNull(newMessage);
    }

    @Test
    public void testCreateNewChatMessageNullContent() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), null, message.getFromUser(), message.getIsQuiz()
        );

        assertNull(newMessage);
    }

    @Test
    public void testGenerateChatMessageResponse() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), true, message.getIsQuiz()
        );
        assertNotNull(newMessage);

        Message responseMessage = chatController.generateChatMessageResponse(newMessage);

        assertNotNull(responseMessage);
        assertFalse(responseMessage.getFromUser());
    }

    @Test
    public void testGenerateChatMessageResponseInvalidUserMessage() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        Message newMessage = chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), false, message.getIsQuiz()
        );
        assertNotNull(newMessage);

        Message responseMessage = chatController.generateChatMessageResponse(newMessage);

        assertNull(responseMessage);
    }

    @Test
    public void testLoadChatMessages() {
        for (Chat chat : Chats) {
            chatController.createNewChat(
                    chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
            );
        }

        for (Message message : Messages) {
            chatController.createNewChatMessage(
                    message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
            );
        }

        Chat chat = Chats[0];
        long userChatMessagesCount = Arrays.stream(Messages).filter(chatMessage -> chatMessage.getChatId() == chat.getId()).count();

        List<Message> chatMessages = chatController.getChatMessages(chat.getId());

        assertNotNull(chatMessages);
        assertEquals(userChatMessagesCount, chatMessages.size());
    }

    @Test
    public void testLoadNoneChatMessages() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        List<Message> messages = chatController.getChatMessages(newChat.getId());

        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    public void testLoadNoneChatMessagesInvalidChatId() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message message = Messages[0];
        chatController.createNewChatMessage(
                message.getChatId(), message.getContent(), message.getFromUser(), message.getIsQuiz()
        );

        List<Message> messages = chatController.getChatMessages(-1);

        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    public void testCreateNewQuiz() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Valid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);

        assertNotNull(quiz);
        assertEquals(responseMessage.getId(), quiz.getMessageId());
    }

    @Test
    public void testCreateNewQuizInvalidMessageContent() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Invalid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Incorrect Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNull(quiz);
    }

    @Test
    public void testCreateNewQuizInvalidMessageUser() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Invalid Quiz Message", true, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNull(quiz);
    }

    @Test
    public void testCreateNewQuizInvalidMessageNotQuiz() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Invalid Quiz Message", false, false
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNull(quiz);
    }

    @Test
    public void testCreateNewQuizQuestion() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Valid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNotNull(quiz);

        String questionFormattedContent = "[Correct Quiz Question Content Format]";
        QuizQuestion quizQuestion = chatController.createNewQuizQuestion(questionFormattedContent, quiz);
        assertNotNull(quizQuestion);
        assertEquals(quiz.getMessageId(), quizQuestion.getMessageId());
    }

    @Test
    public void testCreateNewQuizQuestionInvalidQuestionContent() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Valid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNotNull(quiz);

        String questionFormattedContent = "[Incorrect Quiz Question Content Format]";
        QuizQuestion quizQuestion = chatController.createNewQuizQuestion(questionFormattedContent, quiz);
        assertNull(quizQuestion);
    }

    @Test
    public void testCreateNewQuizQuestionAnswerOption() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Valid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNotNull(quiz);

        String questionFormattedContent = "[Correct Quiz Question Content Format]";
        QuizQuestion quizQuestion = chatController.createNewQuizQuestion(questionFormattedContent, quiz);
        assertNotNull(quizQuestion);

        String answerOptionFormattedContent = "[Correct Question Answer Option Content Format]";
        AnswerOption answerOption = chatController.createNewQuestionAnswerOption(answerOptionFormattedContent, quizQuestion);
        assertNotNull(answerOption);
        assertEquals(quizQuestion.getMessageId(), answerOption.getMessageId());
        assertEquals(quizQuestion.getNumber(), answerOption.getQuestionNumber());
    }

    @Test
    public void testCreateNewQuizQuestionAnswerOptionInvalidAnswerContent() {
        Chat chat = Chats[0];
        Chat newChat = chatController.createNewChat(
                chat.getUserId(), chat.getName(), chat.getResponseAttitude(), chat.getQuizDifficulty(), chat.getEducationLevel(), chat.getStudyArea()
        );
        assertNotNull(newChat);

        Message responseMessage = chatController.createNewChatMessage(
                chat.getId(), "Valid Quiz Message", false, true
        );
        assertNotNull(responseMessage);

        String quizFormattedContent = "[Correct Quiz Content Format]";
        Quiz quiz = chatController.createNewQuiz(quizFormattedContent, responseMessage);
        assertNotNull(quiz);

        String questionFormattedContent = "[Correct Quiz Question Content Format]";
        QuizQuestion quizQuestion = chatController.createNewQuizQuestion(questionFormattedContent, quiz);
        assertNotNull(quizQuestion);

        String answerOptionFormattedContent = "[Incorrect Question Answer Option Content Format]";
        AnswerOption answerOption = chatController.createNewQuestionAnswerOption(answerOptionFormattedContent, quizQuestion);
        assertNull(answerOption);
    }
}
