package tests;

import ai.tutor.cab302exceptionalhandlers.model.Quiz;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuizTest {
    private static final Map<String, Integer> QuizMessageIds = new HashMap<>();
    static {
        QuizMessageIds.put("valid", 1);
        QuizMessageIds.put("invalidLow", 0);
    }

    private static final Map<String, String> QuizNames = new HashMap<>();
    static {
        QuizNames.put("valid", "Quiz-Name 1");
        QuizNames.put("invalidLength", "LongQuizNameOver25Characters");
        QuizNames.put("invalidEmpty", "");
        QuizNames.put("invalidNull", null);
    }

    private static final Map<String, String> QuizDifficulties = new HashMap<>();
    static {
        QuizDifficulties.put("valid", "Balanced");
        QuizDifficulties.put("invalidLength", "LongQuizDifficultyOver25Characters");
        QuizDifficulties.put("invalidEmpty", "");
        QuizDifficulties.put("invalidNull", null);
    }


    @Test
    void validQuizObject() {
        int messageId = QuizMessageIds.get("valid");
        String name = QuizNames.get("valid");
        String difficulty = QuizDifficulties.get("valid");
        Quiz quiz = new Quiz(messageId, name, difficulty);

        assertNotNull(quiz);
        assertEquals(quiz.getMessageId(), messageId);
        assertEquals(quiz.getName(), name);
        assertEquals(quiz.getDifficulty(), difficulty);
    }


    @Test
    void invalidQuizObjectMessageIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("invalidLow"), QuizNames.get("valid"), QuizDifficulties.get("valid"))
        );
    }


    @Test
    void invalidChatObjectNameLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validLength"), QuizDifficulties.get("valid"))
        );
    }

    @Test
    void invalidChatObjectNameEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validEmpty"), QuizDifficulties.get("valid"))
        );
    }

    @Test
    void invalidChatObjectNameNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validNull"), QuizDifficulties.get("valid"))
        );
    }


    @Test
    void invalidChatObjectResponseAttitudeLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validLength"), QuizDifficulties.get("valid"))
        );
    }

    @Test
    void invalidChatObjectResponseAttitudeEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validEmpty"), QuizDifficulties.get("valid"))
        );
    }

    @Test
    void invalidChatObjectResponseAttitudeNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Quiz(QuizMessageIds.get("valid"), QuizNames.get("validNull"), QuizDifficulties.get("valid"))
        );
    }
}
