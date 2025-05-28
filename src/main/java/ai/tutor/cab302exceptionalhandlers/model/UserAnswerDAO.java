package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data operations for {@code UserAnswer} entities in the SQLite database.
 * <p>
 * This Data Access Object (DAO) provides methods to perform CRUD operations and queries
 * on the {@code userAnswers} table in the SQLite database. User answers are associated
 * with quiz questions via a composite key of message ID, attempt number, and question
 * number, representing user responses to quiz questions.
 *
 * @author Joshua M.
 */
public class UserAnswerDAO implements IUserAnswerDAO {
    private final Connection connection;

    /**
     * Constructs a sqlite {@code UserAnswerDAO} connection for database operations.
     *
     * @param sqliteConnection the {@code SQLiteConnection} instance for database access
     * @throws SQLException if a database error occurs during initialisation
     * @throws RuntimeException if the SQLite connection cannot be established
     */
    public UserAnswerDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    /**
     * Creates the {@code userAnswers} table in the SQLite database.
     *
     * @throws SQLException if a database error occurs during table creation
     */
    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS userAnswers ("
                    + "messageId INTEGER,"
                    + "attempt INTEGER CHECK (attempt >= 1),"
                    + "questionNumber INTEGER CHECK (questionNumber >= 1),"
                    + "answerOption VARCHAR,"
                    + "PRIMARY KEY (messageId, attempt, questionNumber),"
                    + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE"
                    + ")"
            );
        }
    }

    /**
     * Inserts a new {@code UserAnswer} entity to the database.
     * <p>
     * This method inserts a {@code UserAnswer} entity into the {@code userAnswers} table,
     * storing its message ID, attempt number, question number, and answer option. The
     * message ID and question number must correspond to an existing quiz question in the
     * {@code quizQuestions} table.
     *
     * @param userAnswer the {@code UserAnswer} entity to insert
     * @throws SQLException if a database error occurs during insertion
     */
    @Override
    public void createUserAnswer(UserAnswer userAnswer) throws SQLException {
        String sql = "INSERT INTO userAnswers (messageId, attempt, questionNumber, answerOption) VALUES (?, ?, ?, ?)";
        try (PreparedStatement createUserAnswer = connection.prepareStatement(sql)) {
            createUserAnswer.setInt(1, userAnswer.getMessageId());
            createUserAnswer.setInt(2, userAnswer.getAttempt());
            createUserAnswer.setInt(3, userAnswer.getQuestionNumber());
            createUserAnswer.setString(4, userAnswer.getAnswerOption());
            createUserAnswer.executeUpdate();
        }
    }

    /**
     * Retrieves a {@code UserAnswer} entity
     * <p>
     * This method fetches a single {@code UserAnswer} entity from the {@code userAnswers}
     * table that matches the specified composite key. Returns {@code null} if no user
     * answer is found for the given key.
     *
     * @param messageId the ID of the associated quiz
     * @param attempt the attempt number for the quiz
     * @param questionNumber the question number within the quiz
     * @return the {@code UserAnswer} entity, or {@code null} if none exists
     * @throws IllegalArgumentException if {@code messageId}, {@code attempt}, or {@code questionNumber} is negative
     * @throws SQLException if a database error occurs during retrieval
     */
    @Override
    public UserAnswer getUserQuestionAnswer(int messageId, int attempt, int questionNumber) throws IllegalArgumentException, SQLException {
        String sql = "SELECT * FROM userAnswers WHERE messageId = ? AND attempt = ? AND questionNumber = ?";
        try (PreparedStatement readUserQuestionAnswer = connection.prepareStatement(sql)) {
            readUserQuestionAnswer.setInt(1, messageId);
            readUserQuestionAnswer.setInt(2, attempt);
            readUserQuestionAnswer.setInt(3, questionNumber);
            ResultSet resultSet = readUserQuestionAnswer.executeQuery();

            if (resultSet.next()) {
                String answerOption = resultSet.getString("answerOption");
                return new UserAnswer(messageId, attempt, questionNumber, answerOption);
            }
        }
        return null;
    }

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz question across all attempts.
     * <p>
     * This method fetches all user answers associated with the specified message ID and
     * question number from the {@code userAnswers} table. Returns a list of
     * {@code UserAnswer} entities, which may be empty if no answers are found for the
     * question.
     *
     * @param messageId the ID of the associated quiz
     * @param questionNumber the question number within the quiz
     * @return a {@code List} of {@code UserAnswer} entities for the question, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} or {@code questionNumber} is negative
     * @throws SQLException if a database error occurs during retrieval
     */
    @Override
    public List<UserAnswer> getAllUserQuestionAttempts(int messageId, int questionNumber) throws IllegalArgumentException, SQLException {
        List<UserAnswer> userQuestionAttempts = new ArrayList<>();
        String sql = "SELECT * FROM userAnswers WHERE messageId = ? AND questionNumber = ?";
        try (PreparedStatement readUserQuestionAttempts = connection.prepareStatement(sql)) {
            readUserQuestionAttempts.setInt(1, messageId);
            readUserQuestionAttempts.setInt(2, questionNumber);
            ResultSet resultSet = readUserQuestionAttempts.executeQuery();

            while (resultSet.next()) {
                int attempt = resultSet.getInt("attempt");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userAttempt = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userQuestionAttempts.add(userAttempt);
            }
        }
        return userQuestionAttempts;
    }

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz attempt.
     * <p>
     * This method fetches all user answers associated with the specified message ID and
     * attempt number from the {@code userAnswers} table. Returns a list of
     * {@code UserAnswer} entities, which may be empty if no answers are found for the
     * attempt.
     *
     * @param messageId the ID of the associated quiz
     * @param attempt the attempt number for the quiz
     * @return a {@code List} of {@code UserAnswer} entities for the attempt, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} or {@code attempt} is negative
     * @throws SQLException if a database error occurs during retrieval
     */
    @Override
    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) throws IllegalArgumentException, SQLException {
        List<UserAnswer> userQuizAnswers = new ArrayList<>();
        String sql = "SELECT * FROM userAnswers WHERE messageId = ? AND attempt = ?";
        try (PreparedStatement readUserQuizAnswers = connection.prepareStatement(sql)) {
            readUserQuizAnswers.setInt(1, messageId);
            readUserQuizAnswers.setInt(2, attempt);
            ResultSet resultSet = readUserQuizAnswers.executeQuery();

            while (resultSet.next()) {
                int questionNumber = resultSet.getInt("questionNumber");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userQuestionAnswer = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userQuizAnswers.add(userQuestionAnswer);
            }
        }
        return userQuizAnswers;
    }

    /**
     * Retrieves all {@code UserAnswer} entities for a specific quiz across all attempts.
     * <p>
     * This method fetches all user answers associated with the specified message ID
     * from the {@code userAnswers} table. Returns a list of {@code UserAnswer} entities,
     * which may be empty if no answers are found for the quiz.
     *
     * @param messageId the ID of the associated quiz
     * @return a {@code List} of {@code UserAnswer} entities for the quiz, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */
    @Override
    public List<UserAnswer> getAllUserQuizAttempts(int messageId) throws IllegalArgumentException, SQLException {
        List<UserAnswer> userQuizAttempts = new ArrayList<>();
        String sql = "SELECT * FROM userAnswers WHERE messageId = ?";
        try (PreparedStatement readUserQuizAttempts = connection.prepareStatement(sql)) {
            readUserQuizAttempts.setInt(1, messageId);
            ResultSet resultSet = readUserQuizAttempts.executeQuery();

            while (resultSet.next()) {
                int attempt = resultSet.getInt("attempt");
                int questionNumber = resultSet.getInt("questionNumber");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userQuestionAttempt = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userQuizAttempts.add(userQuestionAttempt);
            }
        }
        return userQuizAttempts;
    }
}
