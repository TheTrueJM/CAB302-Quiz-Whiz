package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.SceneManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.helpers.Util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class QuizController {
    @FXML private Button returnButton;
    @FXML private Button searchButton;
    @FXML private Button quizSettingsButton;
    @FXML private Button userDetailsButton;
    @FXML private Button configureQuiz;
    @FXML private Button chatModeButton;
    @FXML private Button quizModeButton;
    @FXML private TextField quizNameField;
    @FXML private TextField noQuizField;
    @FXML private TextField welcomeTitle;
    @FXML private ListView questionListView;
    @FXML private ScrollPane quizScrollPane;
    @FXML private VBox quizQuestion;
    @FXML private VBox quizAnswersVBox;
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

    // Dynamic loading
    // NOTE: maps a list of answer option pairs to a messageID for ui layout purposes
    private Map<Integer, List<AnswerOption[]>> answerOptionPairs = new HashMap<>();
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
        //Calculate the attempt number
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
    // TODO: FIX PAIRING???>??
    private void setupQuestions(){
        try {
            quizQuestions = quizQuestionDAO.getAllQuizQuestions(currentQuiz.getMessageId());
            if (quizQuestions == null || quizQuestions.isEmpty()) {
                Utils.showErrorAlert("No questions found for the selected quiz.");
                return;
            }

            // Get List of answer pairs, then map them to a messageID
            List<AnswerOption[]> allPairs = new ArrayList<>();
            AnswerOption[] pair = new AnswerOption[2];

            for(QuizQuestion question : quizQuestions){ // Foreach question
                int qNumber = question.getNumber();

                List<AnswerOption> options = answerOptionDAO.getAllQuestionAnswerOptions(
                        currentQuiz.getMessageId(), qNumber); // Get its options

                for(int i = 0; i < options.size(); i+=2){ // Pair all options up
                    pair[0] = options.get(i);
                    if(i + 1 < options.size()){
                        pair[1] = options.get(i + 1);
                    }
                    allPairs.add(Arrays.copyOf(pair, pair.length));
                }

                answerOptionPairs.put(qNumber, new ArrayList<>(allPairs)); // (qNumber: array of options as pairs)
                allPairs.clear();
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

    // Display the questions on the buttons, and sets up the event handler for the answer buttons
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

        // Clear previous or existing buttons
        for (HBox dynamic : dynamicHBoxCollection){
            if(quizAnswersVBox.getChildren().contains(dynamic)){
                quizAnswersVBox.getChildren().remove(dynamic); // Remove if exists
            }
            quizAnswersVBox.getChildren().remove(dynamic);
        }

        dynamicHBoxCollection.clear(); // Apparently garbage collection will remove it if no references

        // Load questions into VBox as pairs in child Vbox's
        List<AnswerOption[]> allPairs = answerOptionPairs.get(questionNumber);

        for(AnswerOption[] pair : allPairs){ // Foreach pair we have for this question
            HBox childHBox = new HBox(); // New pair, new vbox

            for(int i = 0; i < pair.length; i++){ // Create a button for each
                AnswerOption a = pair[i];
                if(a != null){
                    Button answerButton = new Button();
                    answerButton.setText(a.getValue());

                    // Apply toggled style if toggled
                    String opt = a.getOption();
                    if(questionAnswers.containsKey(questionNumber)){
                        String style = questionAnswers.get(questionNumber).equals(opt.toLowerCase())
                                ? "option-button-toggled"
                                : "option-button";
                        answerButton.getStyleClass().add(style);
                    }
                    else{
                        answerButton.getStyleClass().add("option-button");
                    }

                    // Set mouse click event
                    answerButton.setOnMouseClicked(mouseEvent -> {
                        // Reset all style classes
                        dynamicHBoxCollection.stream()
                                .flatMap(hbox -> hbox.getChildren().stream())
                                .filter(node -> node instanceof Button)
                                .map(node -> (Button) node)
                                .forEach(btn -> {
                                    btn.getStyleClass().setAll("option-button");
                                });

                        answerButton.getStyleClass().remove("option-button");
                        answerButton.getStyleClass().add("option-button-toggled");
                        answerButton.setText(a.getValue());

                        if(!questionAnswers.containsKey(questionNumber))
                            questionAnswers.put(questionNumber, a.getOption()); // Update answer
                    });

                    if(quizCompleted){
                        String selected = questionAnswers.get(questionNumber);
                        answerButton.getStyleClass().add("option-button");
                        if(selected.toLowerCase().equals(a.getOption().toLowerCase())){
                            if(!a.getIsAnswer())
                                answerButton.getStyleClass().add("incorrect-answer");
                        }

                        if(a.getIsAnswer()){
                            answerButton.getStyleClass().add("correct-answer");
                        }
                    }

                    // Set margin
                    double left = i == 0 ? 30 : 0;
                    double right = i == 0 ? 0 : 30;
                    HBox.setMargin(answerButton, new Insets(0, right, 0, left));
                    childHBox.getChildren().add(answerButton);

                    // Add region between two buttons
                    if(i == 0){
                        Region r = new Region();
                        r.setPrefWidth(76);
                        r.setPrefHeight(40);
                        childHBox.getChildren().add(r);
                    }
                }
            }

            // Add the pair of buttons to a new HBox and add to the question template
            dynamicHBoxCollection.add(childHBox); // Save for deletion
            quizAnswersVBox.getChildren().add(childHBox);
        }

        if(quizCompleted){
            // Disable buttons
            dynamicHBoxCollection.stream()
                    .flatMap(hbox -> hbox.getChildren().stream())
                    .filter(node -> node instanceof Button)
                    .map(node -> (Button) node)
                    .forEach(btn -> {
                        btn.setDisable(true);
                    });

            submitQuizButton.setDisable(true);
        }
        questionListView.refresh();
    }

    //Submit Answers
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

    //check the answers, restyle the list view
    private void checkAnswers() throws SQLException, IOException {
        questionListView.setCellFactory(lv -> {
            ListCell<QuizQuestion> cell = new ListCell<QuizQuestion>(){
                @Override
                protected void updateItem(QuizQuestion item, boolean empty){
                    super.updateItem(item, empty);

                    if(item == null || empty){
                        setText(null);
                        setStyle("");
                        return;
                    }

                    int qNumber = item.getNumber();
                    setText("Question: " + qNumber);

                    // Clear previous style classes
                    getStyleClass().removeAll("correct-answer", "incorrect-answer");

                    // Get user's selected option and correct option
                    String userAnswer = questionAnswers.get(qNumber);
                    List<AnswerOption> options = answerOptions.get(qNumber);

                    if (userAnswer != null && options != null) {
                        AnswerOption correct = options.stream()
                                .filter(AnswerOption::getIsAnswer)
                                .findFirst()
                                .orElse(null);

                        if (correct != null) {
                            if (userAnswer.toLowerCase().equals(correct.getOption().toLowerCase())) {
                                getStyleClass().add("correct-answer");
                            } else {
                                getStyleClass().add("incorrect-answer");
                            }
                        }
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                int qNumber = questionListView.getSelectionModel().getSelectedIndex() + 1;
                displayQuestion(qNumber);
            });

            return cell;
        });

        questionListView.refresh();
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

    //Calculates the current attempt number quiz
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
            public QuizQuestion getQuizQuestion(int questionNumber) {
                try {
                    return quizQuestionDAO.getQuizQuestion(currentQuiz.getMessageId(), questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz question: " + e.getMessage());
                    return null;
                }
            }

            // Retrieve Answer Option records for a specific Question
            public List<AnswerOption> getQuestionAnswerOptions(int questionNumber) {
                try {
                    return answerOptionDAO.getAllQuestionAnswerOptions(currentQuiz.getMessageId(), questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question answer options: " + e.getMessage());
                    return null;
                }
            }

            // Retrieve a specific Answer Option record
            public AnswerOption getQuestionAnswerOption(int questionNumber, String option) {
                try {
                    return answerOptionDAO.getQuestionAnswerOption(currentQuiz.getMessageId(), questionNumber, option);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question answer option: " + e.getMessage());
                    return null;
                }
            }

            // Create a new User Answer record using UI user input
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

            // Retrieve a specific User Answer record
            public UserAnswer getQuestionUserAnswer(int attempt, int questionNumber) {
                try {
                    return userAnswerDAO.getUserQuestionAnswer(currentQuiz.getMessageId(), attempt, questionNumber);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read question user answer: " + e.getMessage());
                    return null;
                }
            }

            // Retrieve User Answer records for a specific Quiz
            public List<UserAnswer> getQuizUserAnswers(int attempt) {
                try {
                    return userAnswerDAO.getAllUserQuizAnswers(currentQuiz.getMessageId(), attempt);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to read quiz user answers: " + e.getMessage());
                    return null;
                }
            }
        }
}
