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

    // Intialisation for assets
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

    // Set the quiz name
    private void setQuizNameField(){
        String quizName = currentQuiz.getName();
        quizTitle.setText(quizName);
    }

    //A function that places each question into a list to use later
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

    // Set up the ListView to display questions with a toggle indicating answered state
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

    // Display the questions on the buttons, and sets up the event handler for the answer buttons
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
            addOptionLabel(optionIndex, childHBox);
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

    private void addOptionLabel(int optionIndex, HBox childHBox) {
        Label optionLabel = new Label();
        optionLabel.getStyleClass().setAll(("option-label"));

        // Converts index to ASCII letter (e.g, A,B,C)
        String optionLetter = String.valueOf((char)(optionIndex + 'A'));
        optionLabel.setText(optionLetter);

        HBox labelContainer = new HBox(optionLabel);
        labelContainer.setAlignment(Pos.CENTER);
        labelContainer.getStyleClass().setAll(("label-container"));
        childHBox.getChildren().add(labelContainer);
    }

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

    private void clearAnswerButtons() {
        childAnswerVBox.getChildren().clear(); // Clear all children directly
        dynamicHBoxCollection.clear();
    }

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

        //Submit Answers
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

        //Save answers function
        public void saveAnswers(int messageId, int attempt, Map<Integer, String > answers, UserAnswerDAO dao){
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

        //Return to chat
        private void setupReturnButton() {
            returnButton.setOnAction(actionEvent -> {
                try {
                    SceneManager.getInstance().navigateToChat(currentUser);
                } catch (Exception e) {
                    Utils.showErrorAlert("Error Returning To Chat: " + e);
                }
            });
        }

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

        //Calculates the current attempt number quiz
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

        // Retrieve a specific Quiz record
        public Quiz getQuiz() {
            try {
                return quizDAO.getQuiz(currentQuiz.getMessageId());
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read quiz: " + e.getMessage());
                return null;
            }
        }

        // Retrieve Quiz Question records for a specific Quiz
        public List<QuizQuestion> getQuizQuestions() {
            try {
                return quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read quiz questions: " + e.getMessage());
                return null;
            }
        }

        // Retrieve a specific Quiz Question record
        public QuizQuestion getQuizQuestion(int questionNumber){
            try {
                return quizQuestionDAO.getQuizQuestion(currentQuiz.getMessageId(), questionNumber);
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read quiz question: " + e.getMessage());
                return null;
            }
        }

        // Retrieve Answer Option records for a specific Question
        public List<AnswerOption> getQuestionAnswerOptions(int questionNumber){
            try {
                return answerOptionDAO.getAllQuestionAnswerOptions(currentQuiz.getMessageId(), questionNumber);
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read question answer options: " + e.getMessage());
                return null;
            }
        }

        // Retrieve a specific Answer Option record
        public AnswerOption getQuestionAnswerOption(int questionNumber, String option){
            try {
                return answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read question answer option: " + e.getMessage());
                return null;
            }
        }

        // Create a new User Answer record using UI user input
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

        // Retrieve a specific User Answer record
        public UserAnswer getQuestionUserAnswer(int attempt, int questionNumber){
            try {
                return userAnswerDAO.getUserQuestionAnswer(currentQuiz.getMessageId(), attempt, questionNumber);
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read question user answer: " + e.getMessage());
                return null;
            }
        }

        // Retrieve User Answer records for a specific Quiz
        public List<UserAnswer> getQuizUserAnswers(int attempt){
            try {
                return userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to read quiz user answers: " + e.getMessage());
                return null;
            }
        }
}
