package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import java.sql.SQLException;
import java.util.List;

public class QuizController {
    private Quiz currentQuiz;
    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private AnswerOptionDAO answerOptionDAO;
    private UserAnswerDAO userAnswerDAO;

    public QuizController(SQLiteConnection db, Quiz chosenQuiz) throws IllegalStateException {
        if (chosenQuiz == null) {
            throw new IllegalStateException("No quiz was chosen");
        }

        try {
            this.currentQuiz = chosenQuiz;
            this.userDAO = new UserDAO(db);
            this.chatDAO = new ChatDAO(db);
            this.messageDAO = new MessageDAO(db);
            this.quizDAO = new QuizDAO(db);
            this.quizQuestionDAO = new QuizQuestionDAO(db);
            this.answerOptionDAO = new AnswerOptionDAO(db);
            this.userAnswerDAO = new UserAnswerDAO(db);
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }

    // Retrieve a specific Quiz record
    public Quiz getQuiz() {
        try {
            return quizDAO.getQuiz(currentQuiz.getMessageId());
        } catch (SQLException e) {
            System.err.println("Failed to read quiz: " + e.getMessage());
            return null;
        }
    }

    // Retrieve Quiz Question records for a specific Quiz
    public List<QuizQuestion> getQuizQuestions() {
        try {
            return quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());
        } catch (SQLException e) {
            System.err.println("Failed to read quiz questions: " + e.getMessage());
            return null;
        }
    }

    // Retrieve a specific Quiz Question record
    public QuizQuestion getQuizQuestion(int questionNumber) {
        try {
            return quizQuestionDAO.getQuizQuestion(currentQuiz.getMessageId(), questionNumber);
        } catch (SQLException e) {
            System.err.println("Failed to read quiz question: " + e.getMessage());
            return null;
        }
    }

    // Retrieve Answer Option records for a specific Question
    public List<AnswerOption> getQuestionAnswerOptions(int questionNumber) {
        try {
            return answerOptionDAO.getAllQuestionAnswerOptions(currentQuiz.getMessageId(), questionNumber);
        } catch (SQLException e) {
            System.err.println("Failed to read question answer options: " + e.getMessage());
            return null;
        }
    }

    // Retrieve a specific Answer Option record
    public AnswerOption getQuestionAnswerOption(int questionNumber, String option) {
        try {
            return answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);
        } catch (SQLException e) {
            System.err.println("Failed to read question answer option: " + e.getMessage());
            return null;
        }
    }

    // Create a new User Answer record using UI user input
    public UserAnswer createNewUserAnswer(int questionNumber, String option) {
        try {
            int currentAttempt = userAnswerDAO.getAllUserQuestionAttempts(currentQuiz.getMessageId(), questionNumber).size() + 1;
            UserAnswer userAnswer = new UserAnswer(currentQuiz.getMessageId(), currentAttempt, questionNumber, option);
            userAnswerDAO.createUserAnswer(userAnswer);
            return userAnswer;
        } catch (SQLException e) {
            System.err.println("Failed to create user answer: " + e.getMessage());
            return null;
        }
    }

    // Retrieve a specific User Answer record
    public UserAnswer getQuestionUserAnswer(int attempt, int questionNumber) {
        try {
            return userAnswerDAO.getUserQuestionAnswer(currentQuiz.getMessageId(), attempt, questionNumber);
        } catch (SQLException e) {
            System.err.println("Failed to read question user answer: " + e.getMessage());
            return null;
        }
    }

    // Retrieve User Answer records for a specific Quiz
    public List<UserAnswer> getQuizUserAnswers(int attempt) {
        try {
            return userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
        } catch (SQLException e) {
            System.err.println("Failed to read quiz user answers: " + e.getMessage());
            return null;
        }
    }
}
