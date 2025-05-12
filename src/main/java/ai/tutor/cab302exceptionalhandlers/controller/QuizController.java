package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.*;

public class QuizController {
    @FXML private Button returnButton;
    @FXML private Button searchButton;
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
    @FXML private StackPane quizQuestionsContainer;
    @FXML private StackPane quizListContainer1;
    @FXML private Label quizQuestionLabel;
    @FXML private Button submitQuizButton;

    private SQLiteConnection db;
    private Quiz currentQuiz;
    private UserDAO userDAO;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private AnswerOptionDAO answerOptionDAO;
    private UserAnswerDAO userAnswerDAO;
    //Extra added for quiz functionality
    private int questionNumber;
    private List<QuizQuestion> quizQuestions;
    //For question answers
    private Map<Integer, List<AnswerOption>> answerOptions = new HashMap<>();
    //For User Answers
    private Map<Integer, String> questionAnswers = new HashMap<>();




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

    // Intialisation for assets(currently none but might need this)
    @FXML
    public void initialize() {
        setupQuestions();
        setupQuizListView();
    }

    //Error alert copy pasted from chat controller
    public void showErrorAlert (String message){
        // Create error alert object
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    //A function that places each question into a list to use later
    private void setupQuestions(){
        try {
            //Get the questions
            List<QuizQuestion> questions = quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());

            if (questions == null || questions.isEmpty()) {
                showErrorAlert("No questions found for the selected quiz.");
            }
            questionListView.getItems().setAll(questions);

            // For each question store its answers
            for (int i = 0; i < quizQuestions.size(); i++) {
                int questionNumber = i + 1;
                QuizQuestion question = quizQuestions.get(i);
                List<AnswerOption> options = answerOptionDAO.getAllQuestionAnswerOptions(
                        currentQuiz.getMessageId(), questionNumber
                );
                answerOptions.put(questionNumber, options);
            }
        } catch (SQLException e) {
            showErrorAlert("Failed to load quiz questions: " + e.getMessage());
        }
    }

    // same way its done in the chat sidebar, just without the delete functionality
    private void setupQuizListView() {
        questionListView.setCellFactory(listView -> new ListCell<QuizQuestion>() {
            private final Button selectQuestion = new Button();
            private final HBox container = new HBox(selectQuestion);

            {
                selectQuestion.setOnAction(event -> {
                    QuizQuestion question = getItem();
                    if (question != null) {
                        questionNumber = getIndex() + 1;
                        displayQuestion(questionNumber);
                    }

                });
            }
            @Override
            protected void updateItem(QuizQuestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    selectQuestion.setText("Question " + (getIndex() + 1));
                    setGraphic(selectQuestion);
                }
            }
        });
    }

    //Display the questions on the buttons, and sets up the event handler for the answer buttons
    private void displayQuestion(int questionNumber) {
        QuizQuestion question = quizQuestions.get(questionNumber - 1);
        quizQuestionLabel.setText(question.getQuestion());

        // Get the answer options
        List<AnswerOption> options = answerOptions.get(questionNumber);
        if (options == null || options.size() < 4) {
            showErrorAlert("Answer options are missing");
            return;
        }

        // Sort answer options
        options.sort(Comparator.comparing(AnswerOption::getOption));

        // Assign text
        answerA.setText(options.get(0).getValue());
        answerB.setText(options.get(1).getValue());
        answerC.setText(options.get(2).getValue());
        answerD.setText(options.get(3).getValue());

        answerA.setOnAction(e -> registerAnswer(questionNumber,"A"));
        answerB.setOnAction(e -> registerAnswer(questionNumber,"B"));
        answerC.setOnAction(e -> registerAnswer(questionNumber,"C"));
        answerD.setOnAction(e -> registerAnswer(questionNumber, "D"));
        //Activates the submit Button
        submitQuizButton.setOnAction(e -> submitAnswers());
    }

    //register the answers to map
    private void registerAnswer(int questionNumber, String answerOption){
        questionAnswers.put(questionNumber, answerOption);
    }

    //Submit Answers
    private void submitAnswers(){
        int messageId = currentQuiz.getMessageId();
        int attempt = 1;  //Hardcoded until that system is explained

        try {
            saveAnswers(messageId, attempt, questionAnswers, userAnswerDAO);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to submit answers: " + e.getMessage());
        }
    }

    //Save answers function
    public void saveAnswers(int messageId, int attempt, Map<Integer, String> answers, UserAnswerDAO dao) {
        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            int questionNumber = entry.getKey();
            String answerOption = entry.getValue();
            try{
                UserAnswer newAnswer = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                dao.createUserAnswer(newAnswer);

            } catch (SQLException e) {
                e.printStackTrace();
            }

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