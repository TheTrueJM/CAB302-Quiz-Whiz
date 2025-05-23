package tests;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTest {
    private static final Map<String, Integer> ChatUserIds = new HashMap<>();
    static {
        ChatUserIds.put("valid", 1);
        ChatUserIds.put("invalidLow", 0);
    }

    private static final Map<String, String> ChatNames = new HashMap<>();
    static {
        ChatNames.put("valid", "Chat-Name 1");
        ChatNames.put("invalidLength", "SuperDuperLongChatNameWhichIsOverThe50CharacterLimit");
        ChatNames.put("invalidEmpty", "");
        ChatNames.put("invalidNull", null);
    }

    private static final Map<String, String> ChatResponseAttitudes = new HashMap<>();
    static {
        ChatResponseAttitudes.put("valid", "Balanced");
        ChatResponseAttitudes.put("invalidLength", "LongResponseAttitudeOver25Characters");
        ChatResponseAttitudes.put("invalidEmpty", "");
        ChatResponseAttitudes.put("invalidNull", null);
    }

    private static final Map<String, String> ChatQuizDifficulties = new HashMap<>();
    static {
        ChatQuizDifficulties.put("valid", "Normal");
        ChatQuizDifficulties.put("invalidLength", "LongQuizDifficultyOver25Characters");
        ChatQuizDifficulties.put("invalidEmpty", "");
        ChatQuizDifficulties.put("invalidNull", null);
    }

    private static final Map<String, Integer> ChatQuizLengths = new HashMap<>();
    static {
        ChatQuizLengths.put("valid", 1);
        ChatQuizLengths.put("invalidLow", Chat.MIN_QUIZ_LENGTH - 1);
        ChatQuizLengths.put("invalidHigh", Chat.MAX_QUIZ_LENGTH + 1);
    }

    private static final Map<String, String> ChatEducationLevels = new HashMap<>();
    static {
        ChatEducationLevels.put("valid", "University");
        ChatEducationLevels.put("validEmpty", "");
        ChatEducationLevels.put("validNull", null);
        ChatEducationLevels.put("invalidLength", "SuperDuperLongEducationLevelWhichIsOverThe50CharacterLimit");
    }

    private static final Map<String, String> ChatStudyAreas = new HashMap<>();
    static {
        ChatStudyAreas.put("valid", "Information Technology");
        ChatStudyAreas.put("validEmpty", "");
        ChatStudyAreas.put("validNull", null);
        ChatStudyAreas.put("invalidLength", "SuperDuperLongStudyAreaWhichIsOverThe50CharacterLimit");
    }

    private static final Map<String, Integer> ChatIds = new HashMap<>();
    static {
        ChatIds.put("valid", 1);
        ChatIds.put("invalidLow", 0);
    }

    @Test
    void validChatObject() {
        int userId = ChatUserIds.get("valid");
        String name = ChatNames.get("valid");
        String responseAttitude = ChatResponseAttitudes.get("valid");
        String quizDifficulty = ChatQuizDifficulties.get("valid");
        int quizLength = ChatQuizLengths.get("valid");
        String educationLevel = ChatEducationLevels.get("valid");
        String studyArea = ChatStudyAreas.get("valid");
        Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, quizLength, educationLevel, studyArea);

        assertNotNull(chat);
        assertEquals(chat.getUserId(), userId);
        assertEquals(chat.getName(), name);
        assertEquals(chat.getResponseAttitude(), responseAttitude);
        assertEquals(chat.getQuizDifficulty(), quizDifficulty);
        assertEquals(chat.getQuizLength(), quizLength);
        assertEquals(chat.getEducationLevel(), educationLevel);
        assertEquals(chat.getStudyArea(), studyArea);
    }

    @Test
    void validChatObjectEducationLevelEmpty() {
        int userId = ChatUserIds.get("valid");
        String name = ChatNames.get("valid");
        String responseAttitude = ChatResponseAttitudes.get("valid");
        String quizDifficulty = ChatQuizDifficulties.get("valid");
        int quizLength = ChatQuizLengths.get("valid");
        String studyArea = ChatStudyAreas.get("valid");
        Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, quizLength, ChatEducationLevels.get("validEmpty"), studyArea);

        assertNotNull(chat);
        assertEquals(chat.getUserId(), userId);
        assertEquals(chat.getName(), name);
        assertEquals(chat.getResponseAttitude(), responseAttitude);
        assertEquals(chat.getQuizDifficulty(), quizDifficulty);
        assertEquals(chat.getQuizLength(), quizLength);
        assertNull(chat.getEducationLevel());
        assertEquals(chat.getStudyArea(), studyArea);
    }

    @Test
    void validChatObjectEducationLevelNull() {
        int userId = ChatUserIds.get("valid");
        String name = ChatNames.get("valid");
        String responseAttitude = ChatResponseAttitudes.get("valid");
        String quizDifficulty = ChatQuizDifficulties.get("valid");
        int quizLength = ChatQuizLengths.get("valid");
        String studyArea = ChatStudyAreas.get("valid");
        Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, quizLength, ChatEducationLevels.get("validNull"), studyArea);

        assertNotNull(chat);
        assertEquals(chat.getUserId(), userId);
        assertEquals(chat.getName(), name);
        assertEquals(chat.getResponseAttitude(), responseAttitude);
        assertEquals(chat.getQuizDifficulty(), quizDifficulty);
        assertEquals(chat.getQuizLength(), quizLength);
        assertNull(chat.getEducationLevel());
        assertEquals(chat.getStudyArea(), studyArea);
    }

    @Test
    void validChatObjectStudyAreaEmpty() {
        int userId = ChatUserIds.get("valid");
        String name = ChatNames.get("valid");
        String responseAttitude = ChatResponseAttitudes.get("valid");
        String quizDifficulty = ChatQuizDifficulties.get("valid");
        int quizLength = ChatQuizLengths.get("valid");
        String educationLevel = ChatEducationLevels.get("valid");
        Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, quizLength, educationLevel, ChatStudyAreas.get("validEmpty"));

        assertNotNull(chat);
        assertEquals(chat.getUserId(), userId);
        assertEquals(chat.getName(), name);
        assertEquals(chat.getResponseAttitude(), responseAttitude);
        assertEquals(chat.getQuizDifficulty(), quizDifficulty);
        assertEquals(chat.getQuizLength(), quizLength);
        assertEquals(chat.getEducationLevel(), educationLevel);
        assertNull(chat.getStudyArea());
    }

    @Test
    void validChatObjectStudyAreaNull() {
        int userId = ChatUserIds.get("valid");
        String name = ChatNames.get("valid");
        String responseAttitude = ChatResponseAttitudes.get("valid");
        String quizDifficulty = ChatQuizDifficulties.get("valid");
        int quizLength = ChatQuizLengths.get("valid");
        String educationLevel = ChatEducationLevels.get("valid");
        Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, quizLength, educationLevel, ChatStudyAreas.get("validNull"));

        assertNotNull(chat);
        assertEquals(chat.getUserId(), userId);
        assertEquals(chat.getName(), name);
        assertEquals(chat.getResponseAttitude(), responseAttitude);
        assertEquals(chat.getQuizDifficulty(), quizDifficulty);
        assertEquals(chat.getQuizLength(), quizLength);
        assertEquals(chat.getEducationLevel(), educationLevel);
        assertNull(chat.getStudyArea());
    }


    @Test
    void invalidChatObjectUserIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("invalidLow"), ChatNames.get("invalidLength"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectNameLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("invalidLength"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectNameEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("invalidEmpty"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectNameNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("invalidNull"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectResponseAttitudeLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("invalidLength"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectResponseAttitudeEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("invalidEmpty"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectResponseAttitudeNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("invalidNull"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectQuizDifficultyLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("invalidLength"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectQuizDifficultyEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("invalidEmpty"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectQuizDifficultyNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("invalidNull"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectQuizLengthLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("invalidLow"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }

    @Test
    void invalidChatObjectQuizLengthHigh() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("invalidHigh"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectEducationLevelLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("invalidLength"), ChatStudyAreas.get("valid"))
        );
    }


    @Test
    void invalidChatObjectStudyAreaLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("invalidLength"))
        );
    }


    @Test
    void validUserSetId() {
        Chat chat = new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"));
        assertNotNull(chat);

        int id = ChatIds.get("valid");
        chat.setId(id);
        assertEquals(chat.getId(), id);
    }

    @Test
    void validUserSetIdInvalidLow() {
        Chat chat = new Chat(ChatUserIds.get("valid"), ChatNames.get("valid"), ChatResponseAttitudes.get("valid"), ChatQuizDifficulties.get("valid"), ChatQuizLengths.get("valid"), ChatEducationLevels.get("valid"), ChatStudyAreas.get("valid"));
        assertNotNull(chat);

        assertThrows(
                IllegalArgumentException.class,
                () -> chat.setId(ChatIds.get("invalidLow"))
        );
    }
}
