package tests;

import ai.tutor.cab302exceptionalhandlers.controller.QuizController;
import ai.tutor.cab302exceptionalhandlers.model.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuizControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private QuizController quizController;

    private static final User user = new User("TestUser", "password");
    private static final Chat chat = new Chat(1, "Test Chat 1", "regular", "normal", "University", "IT");
    private static final Message message = new Message(1, "Quiz Message from AI", false, true);
    private static final Quiz quiz = new Quiz(1, "Quiz 1", "normal");

    private static final Map<String, QuizQuestion> QuizQuestions = new HashMap<>();
    static {
        QuizQuestions.put("question1", new QuizQuestion(1, 1, "Question 1"));
        QuizQuestions.put("question2", new QuizQuestion(1, 2, "Question 2"));
    }

    private static final Map<String, AnswerOption> Q1AnswerOptions = new HashMap<>();
    static {
        Q1AnswerOptions.put("a", new AnswerOption(1, 1, "a", "Option A", true));
        Q1AnswerOptions.put("b", new AnswerOption(1, 1, "b", "Option B", false));
        Q1AnswerOptions.put("c", new AnswerOption(1, 1, "c", "Option C", false));
        Q1AnswerOptions.put("d", new AnswerOption(1, 1, "d", "Option D", false));
    }

    private static final Map<String, AnswerOption> Q2AnswerOptions = new HashMap<>();
    static {
        Q2AnswerOptions.put("true", new AnswerOption(1, 2, "true", "True", true));
        Q2AnswerOptions.put("false", new AnswerOption(1, 2, "false", "False", false));
    }

    private static final Map<String, UserAnswer> UserAnswers = new HashMap<>();
    static {
        UserAnswers.put("question1Answer", new UserAnswer(1, 1, 1, "a"));
        UserAnswers.put("question2Answer", new UserAnswer(1, 1, 2, "false"));
    }


    @BeforeEach
    public void setUp(TestInfo testInfo) throws SQLException, IllegalStateException {
        System.out.println("Running test: " + testInfo.getDisplayName());
        db = new SQLiteConnection(true);
        connection = db.getInstance();

        UserDAO userDAO = new UserDAO(db);
        userDAO.createUser(user);
        ChatDAO chatDAO = new ChatDAO(db);
        chatDAO.createChat(chat);
        MessageDAO messageDAO = new MessageDAO(db);
        messageDAO.createMessage(message);
        QuizDAO quizDAO = new QuizDAO(db);
        quizDAO.createQuiz(quiz);

        QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO(db);
        for (QuizQuestion quizQuestion : QuizQuestions.values()) {
            quizQuestionDAO.createQuizQuestion(quizQuestion);
        }
        AnswerOptionDAO answerOptionDAO = new AnswerOptionDAO(db);
        for (AnswerOption answerOption : Q1AnswerOptions.values()) {
            answerOptionDAO.createAnswerOption(answerOption);
        }
        for (AnswerOption answerOption : Q2AnswerOptions.values()) {
            answerOptionDAO.createAnswerOption(answerOption);
        }

        quizController = new QuizController(db, quiz, user);
    }


    @AfterEach
    public void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testGetQuiz() {
        Quiz quiz = quizController.getQuiz();
        assertNotNull(quiz);
        assertEquals(quiz.getMessageId(), quiz.getMessageId());
    }

    @Test
    public void testGetQuizQuestions() {
        List<QuizQuestion> quizQuestions = quizController.getQuizQuestions();
        assertNotNull(quizQuestions);
        assertEquals(QuizQuestions.size(), quizQuestions.size());
    }

    @Test
    public void testGetQuizQuestion() {
        QuizQuestion findQuizQuestion = QuizQuestions.get("question1");
        QuizQuestion quizQuestion = quizController.getQuizQuestion(findQuizQuestion.getNumber());
        assertNotNull(quizQuestion);
        assertEquals(findQuizQuestion.getMessageId(), quizQuestion.getMessageId());
        assertEquals(findQuizQuestion.getNumber(), quizQuestion.getNumber());
    }

    @Test
    public void testGetQuestionAnswerOptions() {
        QuizQuestion quizQuestion = QuizQuestions.get("question1");
        List<AnswerOption> answerOptions = quizController.getQuestionAnswerOptions(quizQuestion.getNumber());
        assertNotNull(answerOptions);
        assertEquals(Q1AnswerOptions.size(), answerOptions.size());
    }

    // Clarification: Get question answer option 'a'
    @Test
    public void testGetQuestionAnsweraOption() {
        AnswerOption findAnswerOption = Q1AnswerOptions.get("a");
        AnswerOption answerOption = quizController.getQuestionAnswerOption(findAnswerOption.getQuestionNumber(), findAnswerOption.getOption());
        assertNotNull(answerOption);
        assertEquals(findAnswerOption.getMessageId(), answerOption.getMessageId());
        assertEquals(findAnswerOption.getQuestionNumber(), answerOption.getQuestionNumber());
        assertEquals(findAnswerOption.getOption(), answerOption.getOption());
    }

    @Test
    public void testCreateNewUserAnswer() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer newUserAnswer = quizController.createNewUserAnswer(
                userAnswer.getQuestionNumber(), userAnswer.getAnswerOption()
        );
        assertNotNull(newUserAnswer);
        assertEquals(userAnswer.getMessageId(), newUserAnswer.getMessageId());
        assertEquals(1, newUserAnswer.getAttempt());
        assertEquals(newUserAnswer.getQuestionNumber(), newUserAnswer.getQuestionNumber());
    }

    @Test
    @Disabled
    public void testCreateNewUserAnswerMuiltipleAttempts() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer firstAttempt = quizController.createNewUserAnswer(userAnswer.getQuestionNumber(), userAnswer.getAnswerOption());
        UserAnswer secondAttempt = quizController.createNewUserAnswer(userAnswer.getQuestionNumber(), userAnswer.getAnswerOption());
        assertNotNull(secondAttempt);
        assertEquals(userAnswer.getMessageId(), secondAttempt.getMessageId());
        assertEquals(2, secondAttempt.getAttempt());
        assertEquals(userAnswer.getQuestionNumber(), secondAttempt.getQuestionNumber());
        assertNotEquals(firstAttempt.getAttempt(), secondAttempt.getAttempt());
    }

    @Test
    public void testCreateNewUserAnswerInvalidQuestion() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer newUserAnswer = quizController.createNewUserAnswer(
                -1, userAnswer.getAnswerOption()
        );
        assertNull(newUserAnswer);
    }

    @Test
    public void testCreateNewUserAnswerInvalidOption() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer newUserAnswer = quizController.createNewUserAnswer(
                userAnswer.getQuestionNumber(), "WrongOption"
        );
        assertNull(newUserAnswer);
    }

    @Test
    public void testGetQuestionUserAnswer() {
        for (UserAnswer userAnswer : UserAnswers.values()) {
            quizController.createNewUserAnswer(
                    userAnswer.getQuestionNumber(), userAnswer.getAnswerOption()
            );
        }
        UserAnswer findUserAnswer = UserAnswers.get("question1Answer");
        UserAnswer userAnswer = quizController.getQuestionUserAnswer(findUserAnswer.getAttempt(), findUserAnswer.getQuestionNumber());
        assertNotNull(userAnswer);
        assertEquals(findUserAnswer.getMessageId(), userAnswer.getMessageId());
        assertEquals(findUserAnswer.getAttempt(), userAnswer.getAttempt());
        assertEquals(findUserAnswer.getQuestionNumber(), userAnswer.getQuestionNumber());
    }

    @Test
    public void testGetQuestionUserAnswerInvalidAttempt() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer newUserAnswer = quizController.createNewUserAnswer(
                userAnswer.getQuestionNumber(), userAnswer.getAnswerOption()
        );
        assertNotNull(newUserAnswer);

        UserAnswer invalidUserAnswer = quizController.getQuestionUserAnswer(-1, userAnswer.getQuestionNumber());
        assertNull(invalidUserAnswer);
    }

    @Test
    public void testGetQuestionUserAnswerInvalidNumber() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        UserAnswer newUserAnswer = quizController.createNewUserAnswer(
                userAnswer.getQuestionNumber(), userAnswer.getAnswerOption()
        );
        assertNotNull(newUserAnswer);

        UserAnswer invalidUserAnswer = quizController.getQuestionUserAnswer(userAnswer.getAttempt(), -1);
        assertNull(invalidUserAnswer);
    }

    @Test
    public void testGetQuizUserAnswers() {
        for (UserAnswer userAnswer : UserAnswers.values()) {
            quizController.createNewUserAnswer(
                    userAnswer.getQuestionNumber(), userAnswer.getAnswerOption()
            );
        }

        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        List<UserAnswer> userAnswers = quizController.getQuizUserAnswers(userAnswer.getAttempt());
        assertNotNull(userAnswers);
        assertEquals(UserAnswers.size(), userAnswers.size());
    }

    @Test
    public void testGetNoneQuizUserAnswers() {
        UserAnswer userAnswer = UserAnswers.get("question1Answer");
        List<UserAnswer> userAnswers = quizController.getQuizUserAnswers(userAnswer.getAttempt());
        assertNotNull(userAnswers);
        assertEquals(0, userAnswers.size());
    }
}
