package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;

public class QuizController {
    @FXML private Button returnButton;
    @FXML private Button answerA;
    @FXML private Button answerB;
    @FXML private Button answerC;
    @FXML private Button answerD;
    @FXML private ListView questionListView;
    @FXML private ScrollPane quizScrollPane;
    @FXML private VBox quizQuestion;
    @FXML private VBox quizAnswers;
    @FXML private StackPane quizQuestionsContainer;
    @FXML private StackPane quizListContainer1;
    @FXML private Label quizQuestionLabel;
    @FXML private Label quizTitle;
    @FXML private Button submitQuizButton;

    private SQLiteConnection db;
    private Quiz currentQuiz;
    private UserDAO userDAO;
    private User currentUser;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private AnswerOptionDAO answerOptionDAO;
    private UserAnswerDAO userAnswerDAO;
    //Extra added for quiz functionality
    private int questionNumber;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    //For question answers
    private final Map<Integer, List<AnswerOption>> answerOptions = new HashMap<>();
    //For User Answers
    private final Map<Integer, String> questionAnswers = new HashMap<>();
    private boolean quizCompleted;


    public QuizController(SQLiteConnection db, Quiz chosenQuiz, User currentUser) throws IllegalStateException {
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
            this.currentUser = currentUser;
            quizCompleted = false;
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }

    private Stage getStage() {
        return (Stage) quizTitle.getScene().getWindow();
    }

    // Intialisation for assets(currently none but might need this)
    @FXML
    public void initialize() {
        setupQuestions();
        setupQuizListView();
        setupReturnButton();
    }

    //A function that places each question into a list to use later
    private void setupQuestions(){
        try {
            quizQuestions = quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());
            if (quizQuestions == null || quizQuestions.isEmpty()) {
                Utils.showErrorAlert("No questions found for the selected quiz.");
                return;
            }

            questionListView.getItems().setAll(quizQuestions);

            for (int i = 0; i < quizQuestions.size(); i++) {
                int questionNum = i + 1;
                List<AnswerOption> options = answerOptionDAO.getAllQuestionAnswerOptions(
                        currentQuiz.getMessageId(), questionNum);
                answerOptions.put(questionNum, options);
            }
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to load quiz questions: " + e.getMessage());
        }
    }

    // Set up the ListView to display questions with a toggle indicating answered state
    private void setupQuizListView() {
        questionListView.setCellFactory(listView -> new ListCell<QuizQuestion>() {
            private final Button selectQuestion = new Button();
            private final ToggleButton toggleAnswered = new ToggleButton();
            private final Region spacer = new Region();
            private final HBox container = new HBox(selectQuestion, spacer, toggleAnswered);

            {
                // Configure HBox
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox.setMargin(toggleAnswered, new Insets(0, 5, 0, 10)); // Margin for toggle
                HBox.setMargin(selectQuestion, new Insets(5, 0, 5, 5)); // Margin for button

                // Handle question selection
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
                    container.setStyle("");
                    setStyle("-fx-background-color: #535353;");
                } else {
                    // Apply style classes
                    getStyleClass().setAll("question-cell");
                    container.getStyleClass().setAll("question-container");
                    selectQuestion.getStyleClass().setAll("select-question");
                    toggleAnswered.getStyleClass().setAll("toggle-answered");

                    // Configure buttons
                    selectQuestion.setAlignment(Pos.CENTER_LEFT);
                    toggleAnswered.setAlignment(Pos.CENTER);
                    toggleAnswered.setPrefSize(25, 25);

                    // Set question text
                    selectQuestion.setText("Question " + (getIndex() + 1));
                    quizTitle.setText("Question " + (getIndex() + 1));

                    // Set toggle state based on questionAnswers map
                    toggleAnswered.setSelected(questionAnswers.containsKey(getIndex() + 1));
                    toggleAnswered.setDisable(true);

                    // Apply correct/incorrect styles if quiz is completed
                    if (quizCompleted) {
                        container.getChildren().remove(toggleAnswered);
                        checkAnswer(this, container);
                    }

                    // Set the container as the cell's graphic
                    setGraphic(container);
                }
            }
        });
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                int index = questionListView.getItems().indexOf(newItem);
                if (index >= 0) {
                    questionNumber = index + 1;
                    displayQuestion(questionNumber);
                }
            }
        });
    }

    private void checkAnswer(ListCell<QuizQuestion> cell, HBox container) {
        container.getStyleClass().setAll("");
        String userAnswer = questionAnswers.get(cell.getIndex() + 1);
        List<AnswerOption> options = answerOptions.get(cell.getIndex() + 1);
        if (userAnswer != null && options != null) {
            AnswerOption correct = options.stream()
                    .filter(AnswerOption::getIsAnswer)
                    .findFirst()
                    .orElse(null);
            if (correct != null) {
                if (userAnswer.equals(correct.getOption())) {
                    container.getStyleClass().add("correct-answer");
                } else {
                    container.getStyleClass().add("incorrect-answer");
                }
            } else {
                System.err.println("No correct answer found for question " + (cell.getIndex() + 1));
            }
        }
    }

    //Display the questions on the buttons, and sets up the event handler for the answer buttons
    private void displayQuestion(int questionNumber) {
        QuizQuestion question = quizQuestions.get(questionNumber - 1);
        quizQuestionLabel.setText(question.getQuestion());

        // Get the answer options
        List<AnswerOption> options = answerOptions.get(questionNumber);
        if (options == null || options.size() < 4) {
            Utils.showErrorAlert("Answer options are missing");
            return;
        }

        // Sort answer options
        options.sort(Comparator.comparing(AnswerOption::getOption));

        // Assign text
        answerA.setText(options.get(0).getValue());
        answerB.setText(options.get(1).getValue());
        answerC.setText(options.get(2).getValue());
        answerD.setText(options.get(3).getValue());

        if(quizCompleted){
            // Disable buttons
            answerA.setDisable(true);
            answerB.setDisable(true);
            answerC.setDisable(true);
            answerD.setDisable(true);
            submitQuizButton.setDisable(true);

            // Get the user's selected answer
            String selected = questionAnswers.get(questionNumber);

            // Apply correct/incorrect CSS classes
            for (AnswerOption opt : options) {
                Button targetButton = null;
                if (opt.getOption().equals("A")) targetButton = answerA;
                if (opt.getOption().equals("B")) targetButton = answerB;
                if (opt.getOption().equals("C")) targetButton = answerC;
                if (opt.getOption().equals("D")) targetButton = answerD;

                if (targetButton != null) {
                    targetButton.getStyleClass().removeAll("correct-answer", "incorrect-answer");
                    if (opt.getIsAnswer()) {
                        targetButton.getStyleClass().add("correct-answer");
                    } else if (opt.getOption().equals(selected)) {
                        targetButton.getStyleClass().add("incorrect-answer");
                    }
                }
            }

        } else{
            // Reset button styles
            Button[] answerButtons = {answerA, answerB, answerC, answerD};
            for (Button btn : answerButtons) {
                btn.getStyleClass().removeAll("option-button", "option-button-toggled");
                btn.getStyleClass().add("option-button");
            }

            // Restore toggled state if an answer was previously selected
            String selectedAnswer = questionAnswers.get(questionNumber);
            if (selectedAnswer != null) {
                Button selectedButton = switch (selectedAnswer) {
                    case "A" -> answerA;
                    case "B" -> answerB;
                    case "C" -> answerC;
                    case "D" -> answerD;
                    default -> null;
                };
                if (selectedButton != null) {
                    selectedButton.getStyleClass().remove("option-button");
                    selectedButton.getStyleClass().add("option-button-toggled");
                }
            }

            answerA.setOnAction(e -> registerAnswer(questionNumber,"A"));
            answerB.setOnAction(e -> registerAnswer(questionNumber,"B"));
            answerC.setOnAction(e -> registerAnswer(questionNumber,"C"));
            answerD.setOnAction(e -> registerAnswer(questionNumber, "D"));
            //Activates the submit Button
            submitQuizButton.setOnAction(e -> submitAnswers());
        }
    }

    //register the answers to map
    private void registerAnswer(int questionNumber, String answerOption){
        questionAnswers.put(questionNumber, answerOption);

        // Reset styles for all buttons
        Button[] answerButtons = {answerA, answerB, answerC, answerD};
        for (Button btn : answerButtons) {
            btn.getStyleClass().removeAll("option-button", "option-button-toggled");
            btn.getStyleClass().add("option-button");
        }

        // Apply toggled style to the selected button
        Button selectedButton = switch (answerOption) {
            case "A" -> answerA;
            case "B" -> answerB;
            case "C" -> answerC;
            case "D" -> answerD;
            default -> null;
        };
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("option-button");
            selectedButton.getStyleClass().add("option-button-toggled");
        }

        questionListView.refresh();
    }

    //Submit Answers
    private void submitAnswers() {
        int messageId = currentQuiz.getMessageId();
        int attempt = 1;
        quizCompleted = true;
        try {
            saveAnswers(messageId, attempt, questionAnswers, userAnswerDAO);
            // Debug: Log saved answers
            System.out.println("Saved answers: " + questionAnswers);
            questionListView.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showErrorAlert("Failed to submit answers: " + e.getMessage());
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

    //Return to chat
    private void setupReturnButton() {
        returnButton.setOnAction(actionEvent -> {
            try {
                Utils.loadView("chat", new ChatController(db, currentUser), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Returning To Chat: " + e);
            }
        });
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
