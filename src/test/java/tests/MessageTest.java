package tests;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageTest {
    private static final Map<String, Integer> MessageChatIds = new HashMap<>();
    static {
        MessageChatIds.put("valid", 1);
        MessageChatIds.put("invalidLow", 0);
    }

    private static final Map<String, String> MessageContents = new HashMap<>();
    static {
        MessageContents.put("valid", "Message Content 1.");
        MessageContents.put("invalidEmpty", "");
        MessageContents.put("invalidNull", null);
    }

    private static final Map<String, Boolean> MessageFromUsers = new HashMap<>();
    static {
        MessageFromUsers.put("validTrue", true);
        MessageFromUsers.put("validFalse", false);
    }

    private static final Map<String, Boolean> MessageIsQuizzes = new HashMap<>();
    static {
        MessageIsQuizzes.put("validTrue", true);
        MessageIsQuizzes.put("validFalse", false);
    }

    private static final Map<String, Integer> MessageIds = new HashMap<>();
    static {
        MessageIds.put("valid", 1);
        MessageIds.put("invalidLow", 0);
    }


    @Test
    void validMessageObjectFromUserIsQuiz() {
        int chatId = MessageChatIds.get("valid");
        String content = MessageContents.get("valid");
        boolean fromUser = MessageFromUsers.get("validTrue");
        boolean isQuiz = MessageIsQuizzes.get("validTrue");
        Message message = new Message(chatId, content, fromUser, isQuiz);

        assertNotNull(message);
        assertEquals(message.getChatId(), chatId);
        assertEquals(message.getContent(), content);
        assertEquals(message.getFromUser(), fromUser);
        assertEquals(message.getIsQuiz(), isQuiz);
    }

    @Test
    void validMessageObjectNotFromUserIsQuiz() {
        int chatId = MessageChatIds.get("valid");
        String content = MessageContents.get("valid");
        boolean fromUser = MessageFromUsers.get("validFalse");
        boolean isQuiz = MessageIsQuizzes.get("validTrue");
        Message message = new Message(chatId, content, fromUser, isQuiz);

        assertNotNull(message);
        assertEquals(message.getChatId(), chatId);
        assertEquals(message.getContent(), content);
        assertEquals(message.getFromUser(), fromUser);
        assertEquals(message.getIsQuiz(), isQuiz);
    }

    @Test
    void validMessageObjectFromUserNotIsQuiz() {
        int chatId = MessageChatIds.get("valid");
        String content = MessageContents.get("valid");
        boolean fromUser = MessageFromUsers.get("validTrue");
        boolean isQuiz = MessageIsQuizzes.get("validFalse");
        Message message = new Message(chatId, content, fromUser, isQuiz);

        assertNotNull(message);
        assertEquals(message.getChatId(), chatId);
        assertEquals(message.getContent(), content);
        assertEquals(message.getFromUser(), fromUser);
        assertEquals(message.getIsQuiz(), isQuiz);
    }

    @Test
    void validMessageObjectNotFromUserNotIsQuiz() {
        int chatId = MessageChatIds.get("valid");
        String content = MessageContents.get("valid");
        boolean fromUser = MessageFromUsers.get("validTrue");
        boolean isQuiz = MessageIsQuizzes.get("validFalse");
        Message message = new Message(chatId, content, fromUser, isQuiz);

        assertNotNull(message);
        assertEquals(message.getChatId(), chatId);
        assertEquals(message.getContent(), content);
        assertEquals(message.getFromUser(), fromUser);
        assertEquals(message.getIsQuiz(), isQuiz);
    }


    @Test
    void invalidMessageObjectChatIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Message(MessageChatIds.get("invalidLow"), MessageContents.get("valid"), MessageFromUsers.get("validTrue"), MessageIsQuizzes.get("validTrue"))
        );
    }


    @Test
    void invalidMessageObjectContentEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Message(MessageChatIds.get("valid"), MessageContents.get("invalidEmpty"), MessageFromUsers.get("validTrue"), MessageIsQuizzes.get("validTrue"))
        );
    }

    @Test
    void invalidMessageObjectContentNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Message(MessageChatIds.get("valid"), MessageContents.get("invalidNull"), MessageFromUsers.get("validTrue"), MessageIsQuizzes.get("validTrue"))
        );
    }


    @Test
    void validMessageSetId() {
        Message message = new Message(MessageChatIds.get("valid"), MessageContents.get("valid"), MessageFromUsers.get("validTrue"), MessageIsQuizzes.get("validTrue"));
        assertNotNull(message);

        int id = MessageIds.get("valid");
        message.setId(id);
        assertEquals(message.getId(), id);
    }

    @Test
    void validMessageSetIdInvalidLow() {
        Message message = new Message(MessageChatIds.get("valid"), MessageContents.get("valid"), MessageFromUsers.get("validTrue"), MessageIsQuizzes.get("validTrue"));
        assertNotNull(message);

        assertThrows(
                IllegalArgumentException.class,
                () -> message.setId(MessageIds.get("invalidLow"))
        );
    }
}
