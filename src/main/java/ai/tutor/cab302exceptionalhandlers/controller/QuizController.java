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
    @FXML private ListView questionListView;
    @FXML private Label quizQuestionLabel;
    @FXML private Label quizTitle;
    @FXML private Button submitQuizButton;
    @FXML private VBox childAnswerVBox;
    @FXML private ComboBox attemptsDropdown;

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

    // Dynamic loading
    private List<HBox> dynamicHBoxCollection = new ArrayList<>(); // Save reference so they can be removed

    /**
     * Constructs a new QuizController with the specified database connection, quiz, and user.
     * <p>
     * Initializes the controller with the provided database connection, the chosen quiz, and the
     * authenticated user. Throws exceptions if the user or quiz is null or if database operations fail.
     * </p>
     * @param db The SQLite database connection
     * @param chosenQuiz The quiz to be taken
     * @param currentUser The authenticated user taking the quiz
     * @throws IllegalStateException If the user or quiz is null
     * @throws RuntimeException If initialization fails
     * @throws SQLException If database operations fail during initialization
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
        calculateCurrentAttempt();
    }

    /**
     * Initializes the quiz interface components.
     * <p>
     * Sets up the quiz name, loads questions, configures the question list view, return button,
     * submit button, and attempts dropdown. Displays the first question by default.
     * </p>
     */

    @FXML
    public void initialize() {
        setQuizNameField();
        setupQuestions();
        setupQuizListView();
        setupReturnButton();
        setupSubmitButton();
        setupAttemptsDropdown();
        displayQuestion(1);
    }

    /**
     * Sets up the submit button event handler.
     * <p>
     * Configures the submit button to validate that all questions are answered before submitting.
     * Displays a warning if not all questions are answered and submits the answers otherwise.
     * </p>
     */

    private void setupSubmitButton(){
        submitQuizButton.setOnMouseClicked(mouseEvent -> {
            if(questionAnswers.size() != quizQuestions.size()){ // Make sure we have answered all question
                Utils.showWarningAlert("Make sure to answer all questions before submitting.");
                return;
            }
            submitAnswers();
            displayQuestion(1);
        });
    }

    /**
     * Sets the quiz name in the title label.
     * <p>
     * Retrieves the name of the current quiz and updates the quiz title label.
     * </p>
     */

    private void setQuizNameField(){
        String quizName = currentQuiz.getName();
        quizTitle.setText(quizName);
    }

    /**
     * Loads all quiz questions and their answer options into memory.
     * <p>
     * Retrieves questions for the current quiz using {@link QuizQuestionDAO} and their corresponding
     * answer options using {@link AnswerOptionDAO}. Displays an error if no questions are found or
     * if a database error occurs.
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
            for (QuizQuestion question : quizQuestions) {
                int qNumber = question.getNumber();
                List<AnswerOption> options = answerOptionDAO.getAllQuestionAnswerOptions(
                        currentQuiz.getMessageId(), qNumber);
                answerOptions.put(qNumber, options); // Store options directly
            }
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to load quiz questions: " + e.getMessage());
        }
    }

    /**
     * Configures the ListView to display quiz questions with answered state indicators.
     * <p>
     * Sets up a custom cell factory to display questions with a toggle button indicating whether
     * they’ve been answered. Updates the UI based on quiz completion status.
     * </p>
     */

    private void setupQuizListView() {
        questionListView.getItems().setAll(quizQuestions);
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
                if (index >= 0 && index + 1 != questionNumber) { // Only update if question changes
                    questionNumber = index + 1;
                    displayQuestion(questionNumber);
                }
            }
        });
    }

    /**
     * Checks and styles a question cell based on the user’s answer correctness.
     * <p>
     * Updates the style of the question container to indicate whether the user’s answer was correct
     * or incorrect based on the correct answer option.
     * </p>
     * @param cell The ListCell containing the question
     * @param container The HBox containing the question UI elements
     */

    private void checkAnswer(ListCell<QuizQuestion> cell, HBox container) {
        int questionIndex = cell.getIndex() + 1;
        if (questionIndex < 1 || questionIndex > quizQuestions.size()) {
            container.getStyleClass().setAll("question-container");
            return;
        }

        String userAnswer = questionAnswers.get(questionIndex);
        List<AnswerOption> options = answerOptions.get(questionIndex);

        // Clear all styles to prevent reuse
        container.getStyleClass().clear();
        container.getStyleClass().add("question-container");

        List<String> correctOptions = new ArrayList<>();
        if (userAnswer != null && options != null) {
            for (AnswerOption option : options) {
                if (option.getIsAnswer()) {
                    correctOptions.add(option.getOption());
                }
            }
            if (!correctOptions.isEmpty()) {
                if (correctOptions.contains(userAnswer)) {
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
     * Displays a specific question and its answer options.
     * <p>
     * Updates the quiz question label and creates answer buttons for the specified question number.
     * Clears previous buttons and applies completion styles if the quiz is completed.
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
        clearAnswerButtons();

        List<AnswerOption> options = answerOptions.get(questionNumber);
        int optionIndex = 0;
        for (AnswerOption option : options) {
            HBox childHBox = new HBox();
            addOptionLabel(option, childHBox, options, optionIndex);
            Button answerButton = createAnswerButton(option, questionNumber);
            childHBox.getChildren().add(answerButton);
            dynamicHBoxCollection.add(childHBox);
            childAnswerVBox.getChildren().add(childHBox);
            optionIndex++;
        }

        if (quizCompleted) {
            applyQuizCompletionStyles(questionNumber);
        }
        questionListView.refresh();
    }

    /**
     * Adds a label for an answer option to an HBox.
     * <p>
     * Creates a label with the answer option text and adds it to the specified HBox.
     * </p>
     * @param option The answer option to label
     * @param childHBox The HBox to add the label to
     * @param options The list of all answer options for the question
     * @param optionIndex The index of the option
     */

    private void addOptionLabel(AnswerOption option, HBox childHBox, List<AnswerOption> options, int optionIndex) {
        Label optionLabel = new Label();
        optionLabel.getStyleClass().setAll(("option-label"));

        optionLabel.setText(option.getOption());
        HBox labelContainer = new HBox(optionLabel);
        labelContainer.setAlignment(Pos.CENTER);
        labelContainer.getStyleClass().setAll(("label-container"));
        childHBox.getChildren().add(labelContainer);
    }

    /**
     * Applies completion styles to answer buttons after quiz submission.
     * <p>
     * Styles buttons to indicate correct or incorrect answers based on the user’s selection
     * and the correct answer.
     * </p>
     * @param questionNumber The number of the question to style
     */

    private void applyQuizCompletionStyles(int questionNumber) {
        String selected = questionAnswers.get(questionNumber);
        dynamicHBoxCollection.stream()
                .flatMap(hbox -> hbox.getChildren().stream())
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .forEach(btn -> {
                    String option = btn.getText(); // Assumes text matches AnswerOption value
                    AnswerOption ao = answerOptions.get(questionNumber).stream()
                            .filter(a -> a.getValue().equals(option))
                            .findFirst()
                            .orElse(null);
                    if (ao != null) {
                        styleCompletedButton(btn, ao, selected);
                        // Style the option label too
                        HBox parentHBox = (HBox) btn.getParent();
                        if (parentHBox != null) {
                            parentHBox.setOpacity(0.8);
                        }
                    }
                });
        submitQuizButton.setDisable(true);
    }

    /**
     * Creates an answer button for a given answer option.
     * <p>
     * Configures the button with the option text, styles it based on the user’s selection,
     * and sets up an event handler to update the selected answer.
     * </p>
     * @param option The answer option for the button
     * @param questionNumber The number of the question the button belongs to
     * @return The created answer button
     */

    private Button createAnswerButton(AnswerOption option, int questionNumber) {
        Button answerButton = new Button(option.getValue());
        String opt = option.getOption();
        String style = questionAnswers.getOrDefault(questionNumber, "").equalsIgnoreCase(opt)
                ? "option-button-toggled"
                : "option-button";
        answerButton.getStyleClass().add(style);

        answerButton.setOnMouseClicked(mouseEvent -> {
            // Reset styles
            dynamicHBoxCollection.stream()
                    .flatMap(hbox -> hbox.getChildren().stream())
                    .filter(node -> node instanceof Button)
                    .map(node -> (Button) node)
                    .forEach(btn -> btn.getStyleClass().setAll("option-button"));
            answerButton.getStyleClass().setAll("option-button-toggled");
            questionAnswers.put(questionNumber, opt);
        });

        return answerButton;
    }

    /**
     * Clears all answer buttons from the UI.
     * <p>
     * Removes all dynamically created HBox elements and clears the collection.
     * </p>
     */

    private void clearAnswerButtons() {
        childAnswerVBox.getChildren().clear(); // Clear all children directly
        dynamicHBoxCollection.clear();
    }

    /**
     * Styles a completed answer button based on correctness.
     * <p>
     * Applies styles to indicate whether the user’s answer was correct or incorrect compared
     * to the correct answer.
     * </p>
     * @param button The button to style
     * @param option The answer option associated with the button
     * @param selected The user’s selected answer
     */

    private void styleCompletedButton(Button button, AnswerOption option, String selected) {
        button.getStyleClass().add("option-button");
        if (selected != null && selected.equalsIgnoreCase(option.getOption())) {
            if (!option.getIsAnswer()) {
                button.getStyleClass().add("incorrect-answer");
            }
        }
        if (option.getIsAnswer()) {
            button.getStyleClass().add("correct-answer");
        }
        button.setDisable(true);
        button.setOpacity(0.8);
    }

    /**
     * Submits the user’s answers for the current quiz attempt.
     * <p>
     * Saves the answers to the database using {@link UserAnswerDAO} and updates the UI to
     * reflect completion.
     * </p>
     */

    private void submitAnswers () {
        int messageId = currentQuiz.getMessageId();
        quizCompleted = true;
        try {
            saveAnswers(messageId, currentAttempt, questionAnswers, userAnswerDAO);
            Platform.runLater(() -> questionListView.refresh());
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to submit answers: " + e.getMessage());
        }
    }

    /**
     * Saves user answers to the database.
     * <p>
     * Creates and persists a {@link UserAnswer} record for each user-selected answer.
     * </p>
     * @param messageId The message ID of the quiz
     * @param attempt The attempt number
     * @param answers The map of question numbers to answers
     * @param dao The UserAnswerDAO for database operations
     */

    public void saveAnswers(int messageId, int attempt, Map<Integer, String > answers, UserAnswerDAO dao) {
        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            int questionNumber = entry.getKey();
            String answerOption = entry.getValue();
            try {
                UserAnswer newAnswer = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                dao.createUserAnswer(newAnswer);

            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to save answers: " + e.getMessage());
            }
        }
    }

    /**
     * Sets up the return button event handler.
     * <p>
     * Configures the return button to navigate back to the chat screen using {@link SceneManager}.
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
     * Sets up the attempts dropdown with available attempt numbers.
     * <p>
     * Populates the dropdown with past and current attempt numbers, selects the current attempt,
     * and adds a listener to load answers for the selected attempt.
     * </p>
     */

    private void setupAttemptsDropdown() {
        if (attemptsDropdown != null) {
            attemptsDropdown.getItems().clear();
            List<Integer> attempts = getAllAttempts();

            if (attempts.isEmpty()) {
                attemptsDropdown.getItems().add("Attempt " + currentAttempt);
            } else {
                for (Integer attempt : attempts) {
                    attemptsDropdown.getItems().add("Attempt " + attempt);
                }
            }
            // Select the current attempt
            attemptsDropdown.getSelectionModel().select("Attempt " + currentAttempt);

            // Add listener for selection changes
            attemptsDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    int selectedAttempt = Integer.parseInt(((String) newValue).replace("Attempt ", ""));
                    if (selectedAttempt != currentAttempt) {
                        currentAttempt = selectedAttempt;
                        // Optionally: Load answers for this attempt and update UI
                        loadAttemptAnswers(selectedAttempt);
                    }
                    if (currentAttempt == attemptsDropdown.getItems().size() && !quizCompleted){
                        // Re-enable submit button for current attempt
                        submitQuizButton.setDisable(false);
                    }
                }
            });
        }
    }

    /**
     * Loads user answers for a specific attempt.
     * <p>
     * Clears current answers, retrieves answers for the specified attempt from the database,
     * and updates the UI accordingly.
     * </p>
     * @param attempt The attempt number to load
     */

    private void loadAttemptAnswers(int attempt) {
        try {
            questionAnswers.clear(); // Clear current answers
            List<UserAnswer> userAnswers = userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
            for (UserAnswer answer : userAnswers) {
                questionAnswers.put(answer.getQuestionNumber(), answer.getAnswerOption());
            }
            quizCompleted = !userAnswers.isEmpty(); // Set quizCompleted based on whether answers exist
            questionListView.refresh();
            displayQuestion(1); // Display first question
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to load answers for attempt " + attempt + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves all unique attempt numbers for the current quiz.
     * <p>
     * Queries the database for past attempts and includes the current attempt if not already present.
     * </p>
     * @return A sorted list of attempt numbers, or null if a database error occurs
     */

    private List<Integer> getAllAttempts() {
        List<Integer> attempts = new ArrayList<>();
        try {
            List<UserAnswer> pastQuizzes = userAnswerDAO.getAllUserQuizAttempts(currentQuiz.getMessageId());
            Set<Integer> pastAttemptNumbers = new HashSet<>();
            // Loop through past attempts and add to set to get unique attempts only
            for (UserAnswer answer : pastQuizzes) {
                pastAttemptNumbers.add(answer.getAttempt());
            }
            // Convert to list and sort
            attempts.addAll(pastAttemptNumbers);
            Collections.sort(attempts);
            // Add the current attempt if not already present
            if (!attempts.contains(currentAttempt)) {
                attempts.add(currentAttempt);
            }
            return attempts;
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to retrieve quiz attempts for User");
            return null;
        }
    }

    /**
     * Calculates the current attempt number for the quiz.
     * <p>
     * Determines the highest previous attempt from the database and increments it by one.
     * </p>
     * @return The calculated current attempt number, or -1 if a database error occurs
     * @throws SQLException If database operations fail
     */

    private int calculateCurrentAttempt() {
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
     * Retrieves the current quiz record from the database.
     * <p>
     * Fetches the quiz details using the message ID from {@link QuizDAO}.
     * </p>
     * @return The current quiz, or null if a database error occurs
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
     * Retrieves all quiz question records for the current quiz.
     * <p>
     * Fetches all questions associated with the quiz using {@link QuizQuestionDAO}.
     * </p>
     * @return A list of quiz questions, or null if a database error occurs
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
     * Retrieves a specific quiz question record by number.
     * <p>
     * Fetches a single question for the current quiz using {@link QuizQuestionDAO}.
     * </p>
     * @param questionNumber The number of the question to retrieve
     * @return The quiz question, or null if a database error occurs
     */

    public QuizQuestion getQuizQuestion(int questionNumber){
        try {
            return quizQuestionDAO.getQuizQuestion(currentQuiz.getMessageId(), questionNumber);
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to read quiz question: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all answer option records for a specific question.
     * <p>
     * Fetches answer options for the specified question number using {@link AnswerOptionDAO}.
     * </p>
     * @param questionNumber The number of the question to retrieve options for
     * @return A list of answer options, or null if a database error occurs
     */

    public List<AnswerOption> getQuestionAnswerOptions(int questionNumber){
        try {
            return answerOptionDAO.getAllQuestionAnswerOptions(currentQuiz.getMessageId(), questionNumber);
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to read question answer options: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a specific answer option record by question number and option value.
     * <p>
     * Fetches a single answer option using {@link AnswerOptionDAO}.
     * </p>
     * @param questionNumber The number of the question
     * @param option The option value to retrieve
     * @return The answer option, or null if a database error occurs
     */

    public AnswerOption getQuestionAnswerOption(int questionNumber, String option){
        try {
            return answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to read question answer option: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new user answer record based on user input.
     * <p>
     * Validates the question number and answer option before creating and persisting a
     * {@link UserAnswer} record using {@link UserAnswerDAO}.
     * </p>
     * @param questionNumber The number of the question
     * @param option The user’s selected answer option
     * @return The created user answer, or null if a database error occurs
     * @throws IllegalArgumentException If the question number or option is invalid
     */

    public UserAnswer createNewUserAnswer(int questionNumber, String option){
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
     * Retrieves a specific user answer record by attempt and question number.
     * <p>
     * Fetches a user answer using {@link UserAnswerDAO}.
     * </p>
     * @param attempt The attempt number
     * @param questionNumber The number of the question
     * @return The user answer, or null if a database error occurs
     */

    public UserAnswer getQuestionUserAnswer(int attempt, int questionNumber){
        try {
            return userAnswerDAO.getUserQuestionAnswer(currentQuiz.getMessageId(), attempt, questionNumber);
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to read question user answer: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all user answer records for a specific quiz attempt.
     * <p>
     * Fetches all answers for the specified attempt using {@link UserAnswerDAO}.
     * </p>
     * @param attempt The attempt number
     * @return A list of user answers, or null if a database error occurs
     */

    public List<UserAnswer> getQuizUserAnswers(int attempt){
        try {
            return userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to read quiz user answers: " + e.getMessage());
            return null;
        }
    }
}
