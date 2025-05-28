package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;

/**
 * Controller for managing the quiz-taking interface in the AI tutor application.
 * <p>
 * Handles the display of quiz questions, collection of user answers, submission of quiz
 * results, and navigation back to the chat screen. Interacts with the database via
 * {@link QuizDAO}, {@link QuizQuestionDAO}, {@link AnswerOptionDAO}, and
 * {@link UserAnswerDAO} to retrieve quiz details and store user answers. Uses JavaFX for
 * rendering the quiz UI, including question lists, answer buttons, and submission logic.
 * </p>
 * @see QuizDAO
 * @see QuizQuestionDAO
 * @see AnswerOptionDAO
 * @see UserAnswerDAO
 * @see SceneManager
 * @see Quiz
 * @see User
 */

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
    private int currentAttempt;

    /**
     * Constructs a QuizController with a database connection, chosen quiz, and authenticated user.
     * <p>
     * Initializes the database connection, quiz, user, and DAO instances ({@link UserDAO},
     * {@link ChatDAO}, {@link MessageDAO}, {@link QuizDAO}, {@link QuizQuestionDAO},
     * {@link AnswerOptionDAO}, {@link UserAnswerDAO}). Calculates the current attempt number
     * using {@link #calculateCurrentAttempt()}. Throws an exception if the user or quiz is null.
     * </p>
     * @param db The SQLite database connection
     * @param chosenQuiz The quiz to be taken
     * @param currentUser The currently authenticated user
     * @throws IllegalStateException If the user or quiz is null
     * @throws RuntimeException If unexpected errors occur during setup
     * @throws SQLException If database initialization fails
     */

    public QuizController(SQLiteConnection db, Quiz chosenQuiz, User currentUser) throws IllegalStateException, RuntimeException, SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("No user was authenticated");
        }
        if (chosenQuiz == null) {
            throw new IllegalStateException("No quiz was chosen");
        }

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
        //Calculate the attempt number
        calculateCurrentAttempt();
    }

    /**
     * Initializes the quiz-taking screen’s UI components and event handlers.
     * <p>
     * Sets the quiz title with {@link #setQuizNameField()}, loads quiz questions with
     * {@link #setupQuestions()}, configures the question list view with
     * {@link #setupQuizListView()}, and sets up the return button with
     * {@link #setupReturnButton()}. Called automatically by JavaFX when the FXML is loaded.
     * </p>
     */

    @FXML
    public void initialize() {
        setQuizNameField();
        setupQuestions();
        setupQuizListView();
        setupReturnButton();
    }

    /**
     * Sets the quiz title in the UI.
     * <p>
     * Updates {@link #quizTitle} with the name of {@link #currentQuiz}.
     * </p>
     */

    private void setQuizNameField(){
        String quizName = currentQuiz.getName();
        quizTitle.setText(quizName);
    }

    /**
     * Loads quiz questions and their answer options from the database.
     * <p>
     * Retrieves questions for {@link #currentQuiz} using {@link #quizQuestionDAO}, populates
     * {@link #quizQuestions}, and fetches answer options for each question using
     * {@link #answerOptionDAO}, storing them in {@link #answerOptions}. Displays an error
     * alert if loading fails.
     * </p>
     * @throws SQLException If database operations fail
     */

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

    /**
     * Configures the list view to display quiz questions with answered state indicators.
     * <p>
     * Sets up {@link #questionListView} with a custom {@link ListCell} containing a select
     * button and a toggle indicating whether the question has been answered. Updates the
     * display with {@link #displayQuestion(int)} when a question is selected, and shows
     * correct/incorrect styling with {@link #checkAnswer(ListCell, HBox)} if the quiz is
     * completed.
     * </p>
     */

    private void setupQuizListView() {
        questionListView.setCellFactory(listView -> new ListCell<QuizQuestion>() {
            private final Button selectQuestion = new Button();
            private final ToggleButton toggleAnswered = new ToggleButton();
            private final Region spacer = new Region();
            private final HBox container = new HBox(selectQuestion, spacer, toggleAnswered);
            {
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox.setMargin(toggleAnswered, new Insets(0, 5, 0, 10));
                HBox.setMargin(selectQuestion, new Insets(5, 0, 5, 5));
            }
            @Override
            protected void updateItem(QuizQuestion item, boolean empty) {
                super.updateItem(item, empty);
                // Clear previous styles and graphic
                setGraphic(null);
                container.getStyleClass().clear();
                setStyle("-fx-background-color: #535353;");

                if (empty || item == null) {
                    return;
                }

                getStyleClass().setAll("question-cell");
                container.getStyleClass().setAll("question-container");
                selectQuestion.getStyleClass().setAll("select-question");
                toggleAnswered.getStyleClass().setAll("toggle-answered");

                selectQuestion.setAlignment(Pos.CENTER_LEFT);
                toggleAnswered.setAlignment(Pos.CENTER);
                toggleAnswered.setPrefSize(25, 25);

                int questionIndex = getIndex() + 1;
                selectQuestion.setText("Question " + questionIndex);

                toggleAnswered.setSelected(questionAnswers.containsKey(questionIndex));
                toggleAnswered.setDisable(true);

                if (quizCompleted) {
                    container.getChildren().remove(toggleAnswered);
                    checkAnswer(this, container);
                } else {
                    container.getChildren().setAll(selectQuestion, spacer, toggleAnswered);
                }

                setGraphic(container);
            }
        });


        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                int index = questionListView.getItems().indexOf(newItem);
                if (index >= 0) {
                    questionNumber = index + 1;
                    Platform.runLater(() -> displayQuestion(questionNumber));
                }
            }
        });
    }

    /**
     * Styles a question in the list view based on the user’s answer correctness.
     * <p>
     * Updates the {@link HBox} container’s styling to indicate whether the user’s answer
     * for the question (identified by index) is correct or incorrect, based on
     * {@link #questionAnswers} and {@link #answerOptions}. Used after quiz completion.
     * </p>
     * @param cell The list cell representing the question
     * @param container The container holding the question UI elements
     */

    private void checkAnswer(ListCell<QuizQuestion> cell, HBox container) {
        int questionIndex = cell.getIndex() + 1;
        if (questionIndex < 1 || questionIndex > quizQuestions.size()) {
            container.getStyleClass().setAll("question-container");
            return;
        }

        String userAnswer = questionAnswers.get(questionIndex);
        List<AnswerOption> options = answerOptions.get(questionIndex);

        // Clear all styles to prevent reuse issues
        container.getStyleClass().clear();
        container.getStyleClass().add("question-container");

        if (userAnswer != null && options != null) {
            AnswerOption correct = options.stream()
                    .filter(AnswerOption::getIsAnswer)
                    .findFirst()
                    .orElse(null);
            if (correct != null) {
                if (userAnswer.equalsIgnoreCase(correct.getOption())) {
                    container.getStyleClass().setAll("correct-question");
                } else {
                    container.getStyleClass().setAll("incorrect-question");
                }
            }
        } else {
            container.getStyleClass().setAll("incorrect-question");
        }
    }


    /**
     * Displays a specific quiz question and its answer options in the UI.
     * <p>
     * Updates {@link #quizQuestionLabel} with the question text and sets the text of answer
     * buttons ({@link #answerA}, {@link #answerB}, {@link #answerC}, {@link #answerD})
     * with the options from {@link #answerOptions}. Configures button actions to register
     * answers with {@link #registerAnswer(int, String)} unless the quiz is completed, in
     * which case it shows the results with correct/incorrect styling.
     * </p>
     * @param questionNumber The number of the question to display
     */

    private void displayQuestion(int questionNumber) {
        if (questionNumber < 1 || questionNumber > quizQuestions.size()) {
            Utils.showErrorAlert("Invalid question number: " + questionNumber);
            return;
        }
        QuizQuestion question = quizQuestions.get(questionNumber - 1);
        quizQuestionLabel.setText(question.getQuestion());

        List<AnswerOption> options = answerOptions.get(questionNumber);
        if (options == null || options.size() < 4) {
            Utils.showErrorAlert("Answer options are missing for question " + questionNumber);
            return;
        }

        options.sort(Comparator.comparing(AnswerOption::getOption));

        answerA.setText(options.get(0).getValue());
        answerB.setText(options.get(1).getValue());
        answerC.setText(options.get(2).getValue());
        answerD.setText(options.get(3).getValue());

        // Reset button styles
        Button[] answerButtons = {answerA, answerB, answerC, answerD};
        for (Button btn : answerButtons) {
            btn.getStyleClass().clear();
            btn.getStyleClass().add("option-button");
            btn.setDisable(false);
        }

        if (quizCompleted) {
            for (Button btn : answerButtons) {
                btn.setDisable(true);
                btn.setOpacity(0.8);
            }
            submitQuizButton.setDisable(true);

            String selected = questionAnswers.get(questionNumber);

            for (AnswerOption opt : options) {
                Button targetButton = null;
                if (opt.getOption().equalsIgnoreCase("A")) targetButton = answerA;
                if (opt.getOption().equalsIgnoreCase("B")) targetButton = answerB;
                if (opt.getOption().equalsIgnoreCase("C")) targetButton = answerC;
                if (opt.getOption().equalsIgnoreCase("D")) targetButton = answerD;

                if (targetButton != null) {
                    targetButton.getStyleClass().clear();
                    targetButton.getStyleClass().add("option-button");
                    if (opt.getIsAnswer()) {
                        targetButton.getStyleClass().add("correct-answer");
                    } else if (selected != null && opt.getOption().equalsIgnoreCase(selected)) {
                        targetButton.getStyleClass().add("incorrect-answer");
                    }
                }
            }
        } else {
            for (Button btn : answerButtons) {
                btn.setOnAction(null);
            }

            final int currentQuestionNumber = questionNumber;
            answerA.setOnAction(e -> registerAnswer(currentQuestionNumber, "A"));
            answerB.setOnAction(e -> registerAnswer(currentQuestionNumber, "B"));
            answerC.setOnAction(e -> registerAnswer(currentQuestionNumber, "C"));
            answerD.setOnAction(e -> registerAnswer(currentQuestionNumber, "D"));

            String selectedAnswer = questionAnswers.get(questionNumber);
            if (selectedAnswer != null) {
                Button selectedButton = switch (selectedAnswer.toUpperCase()) {
                    case "A" -> answerA;
                    case "B" -> answerB;
                    case "C" -> answerC;
                    case "D" -> answerD;
                    default -> null;
                };
                if (selectedButton != null) {
                    selectedButton.getStyleClass().clear();
                    selectedButton.getStyleClass().add("option-button-toggled");
                }
            }

            submitQuizButton.setOnAction(e -> submitAnswers());
        }
    }

    /**
     * Registers the user’s answer for a specific question.
     * <p>
     * Stores the selected answer option in {@link #questionAnswers}, updates the styling of
     * answer buttons ({@link #answerA}, {@link #answerB}, {@link #answerC}, {@link #answerD})
     * to highlight the selection, and refreshes {@link #questionListView} to reflect the
     * answered state.
     * </p>
     * @param questionNumber The number of the question being answered
     * @param answerOption The selected answer option (e.g., "A", "B", "C", "D")
     */

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

    /**
     * Submits the user’s answers for the quiz.
     * <p>
     * Marks the quiz as completed by setting {@link #quizCompleted} to true, saves the
     * answers to the database using {@link #saveAnswers(int, int, Map, UserAnswerDAO)},
     * and refreshes {@link #questionListView} to show the results. Displays an error alert
     * if submission fails.
     * </p>
     */

    private void submitAnswers() {
        int messageId = currentQuiz.getMessageId();
        quizCompleted = true;
        try {
            saveAnswers(messageId, currentAttempt, questionAnswers, userAnswerDAO);
            Platform.runLater(() -> questionListView.refresh());
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showErrorAlert("Failed to submit answers: " + e.getMessage());
        }
    }

    /**
     * Saves the user’s answers to the database.
     * <p>
     * Creates a {@link UserAnswer} record for each question answer in the provided map and
     * saves it to the database using the specified {@link UserAnswerDAO}. Logs any database
     * errors but continues processing remaining answers.
     * </p>
     * @param messageId The ID of the quiz’s associated message
     * @param attempt The attempt number for this quiz
     * @param answers The map of question numbers to selected answer options
     * @param dao The data access object for user answers
     */

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

    /**
     * Configures the return button to navigate back to the chat screen.
     * <p>
     * Sets up {@link #returnButton} to trigger navigation to the chat screen for
     * {@link #currentUser} using {@link SceneManager}. Displays an error alert if navigation
     * fails.
     * </p>
     */

    private void setupReturnButton() {
        returnButton.setOnAction(actionEvent -> {
            try {
                SceneManager.getInstance().navigateToChat(currentUser);
            } catch (Exception e) {
                Utils.showErrorAlert("Error Returning To Chat: " + e);
            }
        });
    }

    /**
     * Calculates the current attempt number for the quiz.
     * <p>
     * Retrieves past attempts for the first question of {@link #currentQuiz} using
     * {@link #userAnswerDAO}, determines the highest attempt number, and sets
     * {@link #currentAttempt} to the next number. Displays an error alert and returns -1 if
     * calculation fails.
     * </p>
     * @return The current attempt number, or -1 if calculation fails
     */

    private int calculateCurrentAttempt(){
        try {
            //Question number is 1, as all quizzes will have an answer for question 1, even if the answer is null
            //Creates the list using messageID, as that is the quiz identifier
            List<UserAnswer> pastQuizes = userAnswerDAO.getAllUserQuestionAttempts(currentQuiz.getMessageId(), 1);
            //Finds the highest previous attempt
            int latestAttempt = pastQuizes.size();
            //current attempt is 1 attempt after latest attempt
            currentAttempt = latestAttempt + 1;
            return currentAttempt;
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to calculate current attempt: " + e.getMessage());
            return -1; // This should trigger an exception when creating a new UserAnswer
        }
    }

            /**
             * Retrieves the current quiz from the database.
             * <p>
             * Fetches the quiz details for {@link #currentQuiz}’s message ID using {@link #quizDAO}.
             * Displays an error alert and returns null if retrieval fails.
             * </p>
             * @return The {@link Quiz} object, or null if retrieval fails
             */

            public Quiz getQuiz() {
                try {
                    return quizDAO.getQuiz(currentQuiz.getMessageId());
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves all questions for the current quiz from the database.
             * <p>
             * Fetches all questions for {@link #currentQuiz}’s message ID using
             * {@link #quizQuestionDAO}. Displays an error alert and returns null if retrieval fails.
             * </p>
             * @return A list of {@link QuizQuestion} objects, or null if retrieval fails
             */

            public List<QuizQuestion> getQuizQuestions() {
                try {
                    return quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz questions: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves a specific quiz question from the database.
             * <p>
             * Fetches the question for the given number in {@link #currentQuiz} using
             * {@link #quizQuestionDAO}. Displays an error alert and returns null if retrieval fails.
             * </p>
             * @param questionNumber The number of the question to retrieve
             * @return The {@link QuizQuestion} object, or null if retrieval fails
             */

            public QuizQuestion getQuizQuestion(int questionNumber) {
                try {
                    return quizQuestionDAO.getQuizQuestion(currentQuiz.getMessageId(), questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz question: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves all answer options for a specific quiz question from the database.
             * <p>
             * Fetches the answer options for the given question number in {@link #currentQuiz} using
             * {@link #answerOptionDAO}. Displays an error alert and returns null if retrieval fails.
             * </p>
             * @param questionNumber The number of the question
             * @return A list of {@link AnswerOption} objects, or null if retrieval fails
             */

            public List<AnswerOption> getQuestionAnswerOptions(int questionNumber) {
                try {
                    return answerOptionDAO.getAllQuestionAnswerOptions(currentQuiz.getMessageId(), questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question answer options: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves a specific answer option for a quiz question from the database.
             * <p>
             * Fetches the answer option for the given question number and option letter in
             * {@link #currentQuiz} using {@link #answerOptionDAO}. Displays an error alert and
             * returns null if retrieval fails.
             * </p>
             * @param questionNumber The number of the question
             * @param option The option letter (e.g., "A", "B", "C", "D")
             * @return The {@link AnswerOption} object, or null if retrieval fails
             */

            public AnswerOption getQuestionAnswerOption(int questionNumber, String option) {
                try {
                    return answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question answer option: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Creates a new user answer record based on UI input.
             * <p>
             * Validates the question number and answer option, creates a {@link UserAnswer} object
             * with the current attempt number from {@link #calculateCurrentAttempt()}, and saves it
             * to the database using {@link #userAnswerDAO}. Displays an error alert and returns null
             * if creation fails.
             * </p>
             * @param questionNumber The number of the question being answered
             * @param option The selected answer option (e.g., "A", "B", "C", "D")
             * @return The created {@link UserAnswer} object, or null if creation fails
             * @throws IllegalArgumentException If the question number or answer option is invalid
             */

            public UserAnswer createNewUserAnswer(int questionNumber, String option) {
                try {
                    if (questionNumber < 1) {
                        throw new IllegalArgumentException("Invalid question number was given");
                    }

                    int currentAttempt = calculateCurrentAttempt();
                    AnswerOption answerOption = answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);

                    if (answerOption == null) {
                        throw new IllegalArgumentException("Invalid answer option was given");
                    }

                    UserAnswer userAnswer = new UserAnswer(currentQuiz.getMessageId(), currentAttempt, questionNumber, option);
                    userAnswerDAO.createUserAnswer(userAnswer);
                    return userAnswer;
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to create user answer: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves a specific user answer for a quiz question from the database.
             * <p>
             * Fetches the user answer for the given attempt and question number in
             * {@link #currentQuiz} using {@link #userAnswerDAO}. Displays an error alert and returns
             * null if retrieval fails.
             * </p>
             * @param attempt The attempt number
             * @param questionNumber The number of the question
             * @return The {@link UserAnswer} object, or null if retrieval fails
             */

            public UserAnswer getQuestionUserAnswer(int attempt, int questionNumber) {
                try {
                    return userAnswerDAO.getUserQuestionAnswer(currentQuiz.getMessageId(), attempt, questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question user answer: " + e.getMessage());
                    return null;
                }
            }

            /**
             * Retrieves all user answers for a specific quiz attempt from the database.
             * <p>
             * Fetches all user answers for the given attempt in {@link #currentQuiz} using
             * {@link #userAnswerDAO}. Displays an error alert and returns null if retrieval fails.
             * </p>
             * @param attempt The attempt number
             * @return A list of {@link UserAnswer} objects, or null if retrieval fails
             */

            public List<UserAnswer> getQuizUserAnswers(int attempt) {
                try {
                    return userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz user answers: " + e.getMessage());
                    return null;
                }
            }
}
