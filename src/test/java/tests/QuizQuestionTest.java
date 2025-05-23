package tests;

import ai.tutor.cab302exceptionalhandlers.model.QuizQuestion;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuizQuestionTest {
    private static final Map<String, Integer> QuestionMessageIds = new HashMap<>();
    static {
        QuestionMessageIds.put("valid", 1);
        QuestionMessageIds.put("invalidLow", 0);
    }

    private static final Map<String, Integer> QuestionNumbers = new HashMap<>();
    static {
        QuestionNumbers.put("valid", 1);
        QuestionNumbers.put("invalidLow", 0);
    }

    private static final Map<String, String> Questions = new HashMap<>();
    static {
        Questions.put("valid", "Quiz Question 1: Question Text");
        Questions.put("invalidLength", "SuperDuperLongQuizQuestionWhichIsOverThe100CharacterLimitRequiredForTheQuizQuestionObjectsQuestionValue");
        Questions.put("invalidEmpty", "");
        Questions.put("invalidNull", null);
    }


    @Test
    void validQuizQuestionObject() {
        int messageId = QuestionMessageIds.get("valid");
        int number = QuestionNumbers.get("valid");
        String question = Questions.get("valid");
        QuizQuestion quizQuestion = new QuizQuestion(messageId, number, question);

        assertNotNull(quizQuestion);
        assertEquals(quizQuestion.getMessageId(), messageId);
        assertEquals(quizQuestion.getNumber(), number);
        assertEquals(quizQuestion.getQuestion(), question);
    }


    @Test
    void invalidQuizQuestionObjectMessageIdLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizQuestion(QuestionMessageIds.get("invalidLow"),  QuestionNumbers.get("valid"), Questions.get("valid"))
        );
    }

    @Test
    void invalidQuizQuestionObjectQuestionNumberLow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizQuestion(QuestionMessageIds.get("valid"),  QuestionNumbers.get("invalidLow"), Questions.get("valid"))
        );
    }


    @Test
    void invalidQuizQuestionObjectNameLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizQuestion(QuestionMessageIds.get("valid"),  QuestionNumbers.get("valid"), Questions.get("invalidLength"))
        );
    }

    @Test
    void invalidQuizQuestionObjectNameEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizQuestion(QuestionMessageIds.get("valid"),  QuestionNumbers.get("valid"), Questions.get("invalidEmpty"))
        );
    }

    @Test
    void invalidQuizQuestionObjectNameNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuizQuestion(QuestionMessageIds.get("valid"),  QuestionNumbers.get("valid"), Questions.get("invalidNull"))
        );
    }
}
