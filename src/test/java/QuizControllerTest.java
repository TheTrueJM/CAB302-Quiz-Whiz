import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.controller.QuizController;
import ai.tutor.cab302exceptionalhandlers.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;

public class QuizControllerTest {
    private SQLiteConnection db;
    private Connection connection;
    private QuizController quizController;

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
            new Message(1, "Chat Message 1", true, true),
            new Message(1, "Chat Message 2", false, true),
            new Message(1, "Chat Message 3", true, false),
            new Message(1, "Chat Message 4", false, false),
            new Message(1, "Chat Message 5", true, true),
            new Message(1, "Chat Message 6", false, true),
            new Message(2, "Chat Message 1", true, false),
            new Message(2, "Chat Message 2", false, false)
    };

    private static final Quiz[] Quizzes = {
            new Quiz(2, "Quiz 1", "normal"),
            new Quiz(5, "Quiz 2", "normal")
    };

    private static final QuizQuestion[] QuizQuestions = {
            new QuizQuestion(2, 1, "Question 1"),
            new QuizQuestion(2, 2, "Question 2"),
            new QuizQuestion(5, 1, "Question 1")
    };

    private static final AnswerOption[] AnswerOptions = {
            new AnswerOption(2, 1, "A", "option A", true),
            new AnswerOption(2, 1, "B", "option B", false),
            new AnswerOption(2, 1, "C", "option C", false),
            new AnswerOption(2, 1, "D", "option D", false),
            new AnswerOption(2, 2, "True", "True", true),
            new AnswerOption(2, 2, "False", "False", false),
            new AnswerOption(5, 1, "True", "True", false),
            new AnswerOption(5, 1, "False", "False", true)
    };


    @BeforeEach
    public void setUp() {
        try {
            db = new SQLiteConnection("testing");
            connection = db.getInstance();
            quizController = new QuizController();

            UserDAO userDAO = new UserDAO(db);
            for (User user : Users) { userDAO.createUser(user); }

            ChatDAO chatDAO = new ChatDAO(db);
            for (Chat chat : Chats) { chatDAO.createChat(chat); }

            MessageDAO messageDAO = new MessageDAO(db);
            for (Message message : Messages) { messageDAO.createMessage(message); }

            QuizDAO quizDAO = new QuizDAO(db);
            for (Quiz quiz : Quizzes) { quizDAO.createQuiz(quiz); }

            QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO(db);
            for (QuizQuestion quizQuestion : QuizQuestions) { quizQuestionDAO.createQuizQuestion(quizQuestion); }

            AnswerOptionDAO answerOptionDAO = new AnswerOptionDAO(db);
            for (AnswerOption answerOption : AnswerOptions) { answerOptionDAO.createAnswerOption(answerOption); }
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
}
