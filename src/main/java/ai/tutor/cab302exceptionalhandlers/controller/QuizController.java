package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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

    // Dynamic loading
    // NOTE: maps a list of answer option pairs to a messageID for ui layout purposes
    private Map<Integer, List<AnswerOption[]>> answerOptionPairs = new HashMap<>();
    private List<HBox> dynamicHBoxCollection = new ArrayList<>(); // Save reference so they can be removed


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
        return (Stage) quizNameField.getScene().getWindow();
    }

    // Intialisation for assets(currently none but might need this)
    @FXML
    public void initialize() {
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

    //A function that places each question into a list to use later
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

    // same way its done in the chat sidebar, just without the delete functionality
    private void setupQuizListView() {
        ObservableList<QuizQuestion> ol = FXCollections.observableArrayList(quizQuestions);
        questionListView.setItems(ol);

        questionListView.setCellFactory(lv -> {
            ListCell<QuizQuestion> cell = new ListCell<QuizQuestion>() {
                @Override
                protected void updateItem(QuizQuestion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        String label = "Question " + item.getNumber();
                        setText(label);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                int qNumber = questionListView.getSelectionModel().getSelectedIndex() + 1;
                displayQuestion(qNumber);
            });

            return cell;
        });
    }

    //Display the questions on the buttons, and sets up the event handler for the answer buttons
    private void displayQuestion(int questionNumber) {
        QuizQuestion question = quizQuestions.get(questionNumber - 1);
        quizQuestionLabel.setText(question.getQuestion());

        // Get the answer options
        List<AnswerOption> options = answerOptions.get(questionNumber);

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
    }

    //Submit Answers
    private void submitAnswers(){
        int messageId = currentQuiz.getMessageId();
        int attempt = 1;  //Hardcoded until that system is explained
        quizCompleted = true;
        try {
            saveAnswers(messageId, attempt, questionAnswers, userAnswerDAO);
            checkAnswers();
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
