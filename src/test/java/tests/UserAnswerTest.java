package tests;

import ai.tutor.cab302exceptionalhandlers.model.UserAnswer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserAnswerTest {
    private static final Map<String, Integer> UserAnswerMessageIds = new HashMap<>();
    static {
        UserAnswerMessageIds.put("valid", 1);
        UserAnswerMessageIds.put("invalidLow", 0);
    }

    private static final Map<String, Integer> UserAnswerAttempts = new HashMap<>();
    static {
        UserAnswerAttempts.put("valid", 1);
        UserAnswerAttempts.put("invalidLow", 0);
    }

    private static final Map<String, Integer> UserAnswerQuestionNumbers = new HashMap<>();
    static {
        UserAnswerQuestionNumbers.put("valid", 1);
        UserAnswerQuestionNumbers.put("invalidLow", 0);
    }

    private static final Map<String, String> UserAnswerOptions = new HashMap<>();
    static {
        UserAnswerOptions.put("valid", "option");
        UserAnswerOptions.put("validEmpty", "");
        UserAnswerOptions.put("validNull", null);
        UserAnswerOptions.put("invalidLength", "LongAnswerOptionOver25Characters");
    }


    @Test
    void validUserAnswerObject() {
        int messageId = UserAnswerMessageIds.get("valid");
        int attempt = UserAnswerAttempts.get("valid");
        int number = UserAnswerQuestionNumbers.get("valid");
        String option = UserAnswerOptions.get("valid");
        UserAnswer userAnswer = new UserAnswer(messageId, attempt, number, option);

        assertNotNull(userAnswer);
        assertEquals(userAnswer.getMessageId(), messageId);
        assertEquals(userAnswer.getAttempt(), attempt);
        assertEquals(userAnswer.getQuestionNumber(), number);
        assertEquals(userAnswer.getAnswerOption(), option);
    }

    @Test
    void validUserAnswerObjectOptionEmpty() {
        int messageId = UserAnswerMessageIds.get("valid");
        int attempt = UserAnswerAttempts.get("valid");
        int number = UserAnswerQuestionNumbers.get("valid");
        String option = UserAnswerOptions.get("validEmpty");
        UserAnswer userAnswer = new UserAnswer(messageId, attempt, number, option);

        assertNotNull(userAnswer);
        assertEquals(userAnswer.getMessageId(), messageId);
        assertEquals(userAnswer.getAttempt(), attempt);
        assertEquals(userAnswer.getQuestionNumber(), number);
        assertEquals(userAnswer.getAnswerOption(), option);
    }

    @Test
    void validUserAnswerObjectOptionNull() {
        int messageId = UserAnswerMessageIds.get("valid");
        int attempt = UserAnswerAttempts.get("valid");
        int number = UserAnswerQuestionNumbers.get("valid");
        String option = UserAnswerOptions.get("validNull");
        UserAnswer userAnswer = new UserAnswer(messageId, attempt, number, option);

        assertNotNull(userAnswer);
        assertEquals(userAnswer.getMessageId(), messageId);
        assertEquals(userAnswer.getAttempt(), attempt);
        assertEquals(userAnswer.getQuestionNumber(), number);
        assertEquals(userAnswer.getAnswerOption(), option);
    }


    @Test
    void invalidUserAnswerObjectMessageIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserAnswer(UserAnswerMessageIds.get("invalidLow"), UserAnswerAttempts.get("valid"), UserAnswerQuestionNumbers.get("valid"), UserAnswerOptions.get("valid"))
        );
    }


    @Test
    void invalidUserAnswerObjectAttemptLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserAnswer(UserAnswerMessageIds.get("valid"), UserAnswerAttempts.get("invalidLow"), UserAnswerQuestionNumbers.get("valid"), UserAnswerOptions.get("valid"))
        );
    }


    @Test
    void invalidUserAnswerObjectQuestionNumberLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserAnswer(UserAnswerMessageIds.get("valid"), UserAnswerAttempts.get("valid"), UserAnswerQuestionNumbers.get("invalidLow"), UserAnswerOptions.get("valid"))
        );
    }


    @Test
    void invalidUserAnswerObjectOptionLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserAnswer(UserAnswerMessageIds.get("valid"), UserAnswerAttempts.get("valid"), UserAnswerQuestionNumbers.get("valid"), UserAnswerOptions.get("invalidLength"))
        );
    }
}
