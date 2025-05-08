package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class QuizController {
    @FXML private Button returnButton;
    @FXML private Button searchButton;
    @FXML private Button editQuizName;
    @FXML private Button confirmEditQuizName;
    @FXML private Button quizSettingsButton;
    @FXML private Button userDetailsButton;
    @FXML private Button answerA;
    @FXML private Button answerB;
    @FXML private Button answerC;
    @FXML private Button answerD;
    @FXML private Button configureQuiz;
    @FXML private Button chatModeButton;
    @FXML private Button quizModeButton;
    @FXML private TextField quizNameField;
    @FXML private TextField noQuizField;
    @FXML private TextField welcomeTitle;
    @FXML private ListView questionListView;
    @FXML private ScrollPane quizScrollPane;
    @FXML private VBox quizQuestion;
    @FXML private VBox quizAnswers;
    @FXML private VBox greetingContainer;
    @FXML private StackPane quizQuestionsContainer;
    @FXML private StackPane quizListContainer1;
    @FXML private Label quizQuestionLabel;

    private SQLiteConnection db;
    private Quiz currentQuiz;
    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private AnswerOptionDAO answerOptionDAO;
    private UserAnswerDAO userAnswerDAO;

    @FXML
    public void initialize() {

    }

    public QuizController(SQLiteConnection db, Quiz chosenQuiz) throws IllegalStateException {
        if (chosenQuiz == null) {
            throw new IllegalStateException("No quiz was chosen");
        }
        try {
            this.db = db;
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

    // Calculate the current attempt number for a specific question
    private int calculateCurrentAttempt(int questionNumber) {
        try {
            return userAnswerDAO.getAllUserQuestionAttempts(currentQuiz.getMessageId(), questionNumber).size() + 1;
        } catch (SQLException e) {
            System.err.println("Failed to calculate current attempt: " + e.getMessage());
            return -1; // This should trigger an exception when creating a new UserAnswer
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
            int currentAttempt = calculateCurrentAttempt(questionNumber);
            AnswerOption answerOption = answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);

            if (answerOption == null) {
                throw new SQLException("Invalid answer option was given");
            }

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
