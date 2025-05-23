package tests;

import ai.tutor.cab302exceptionalhandlers.model.AnswerOption;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnswerOptionTest {
    private static final Map<String, Integer> AnswerMessageIds = new HashMap<>();
    static {
        AnswerMessageIds.put("valid", 1);
        AnswerMessageIds.put("invalidLow", 0);
    }

    private static final Map<String, Integer> AnswerQuestionNumbers = new HashMap<>();
    static {
        AnswerQuestionNumbers.put("valid", 1);
        AnswerQuestionNumbers.put("invalidLow", 0);
    }

    private static final Map<String, String> AnswerOptions = new HashMap<>();
    static {
        AnswerOptions.put("valid", "option");
        AnswerOptions.put("invalidLength", "LongAnswerOptionOver25Characters");
        AnswerOptions.put("invalidEmpty", "");
        AnswerOptions.put("invalidNull", null);
    }

    private static final Map<String, String> AnswerValues = new HashMap<>();
    static {
        AnswerValues.put("valid", "Question Answer Option Value");
        AnswerValues.put("invalidLength", "SuperDuperLongAnswerValueWhichIsOverThe100CharacterLimitRequiredForTheAnswerOptionObjectsAnswerValueValue");
        AnswerValues.put("invalidEmpty", "");
        AnswerValues.put("invalidNull", null);
    }

    private static final Map<String, Boolean> IsAnswers = new HashMap<>();
    static {
        IsAnswers.put("validTrue", true);
        IsAnswers.put("validFalse", false);
    }


    @Test
    void validQuizQuestionObjectIsAnswer() {
        int messageId = AnswerMessageIds.get("valid");
        int number = AnswerQuestionNumbers.get("valid");
        String option = AnswerOptions.get("valid");
        String value = AnswerValues.get("valid");
        boolean isAnswer = IsAnswers.get("validTrue");
        AnswerOption answerOption = new AnswerOption(messageId, number, option, value, isAnswer);

        assertNotNull(answerOption);
        assertEquals(answerOption.getMessageId(), messageId);
        assertEquals(answerOption.getQuestionNumber(), number);
        assertEquals(answerOption.getOption(), option);
        assertEquals(answerOption.getValue(), value);
        assertEquals(answerOption.getIsAnswer(), isAnswer);
    }

    @Test
    void validQuizQuestionObjectNotIsAnswer() {
        int messageId = AnswerMessageIds.get("valid");
        int number = AnswerQuestionNumbers.get("valid");
        String option = AnswerOptions.get("valid");
        String value = AnswerValues.get("valid");
        boolean isAnswer = IsAnswers.get("validFalse");
        AnswerOption answerOption = new AnswerOption(messageId, number, option, value, isAnswer);

        assertNotNull(answerOption);
        assertEquals(answerOption.getMessageId(), messageId);
        assertEquals(answerOption.getQuestionNumber(), number);
        assertEquals(answerOption.getOption(), option);
        assertEquals(answerOption.getValue(), value);
        assertEquals(answerOption.getIsAnswer(), isAnswer);
    }


    @Test
    void invalidAnswerOptionObjectMessageIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("invalidLow"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("valid"), AnswerValues.get("valid"), IsAnswers.get("validTrue"))
        );
    }


    @Test
    void invalidAnswerOptionObjectQuestionNumberLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("invalidLow"), AnswerOptions.get("valid"), AnswerValues.get("valid"), IsAnswers.get("validTrue"))
        );
    }


    @Test
    void invalidAnswerOptionObjectOptionLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("invalidLength"), AnswerValues.get("valid"), IsAnswers.get("validTrue"))
        );
    }

    @Test
    void invalidAnswerOptionObjectOptionEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("invalidEmpty"), AnswerValues.get("valid"), IsAnswers.get("validTrue"))
        );
    }

    @Test
    void invalidAnswerOptionObjectOptionNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("invalidNull"), AnswerValues.get("valid"), IsAnswers.get("validTrue"))
        );
    }


    @Test
    void invalidAnswerOptionObjectValueLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("valid"), AnswerValues.get("invalidLength"), IsAnswers.get("validTrue"))
        );
    }

    @Test
    void invalidAnswerOptionObjectValueEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("valid"), AnswerValues.get("invalidEmpty"), IsAnswers.get("validTrue"))
        );
    }

    @Test
    void invalidAnswerOptionObjectValueNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnswerOption(AnswerMessageIds.get("valid"), AnswerQuestionNumbers.get("valid"), AnswerOptions.get("valid"), AnswerValues.get("invalidNull"), IsAnswers.get("validTrue"))
        );
    }
}
