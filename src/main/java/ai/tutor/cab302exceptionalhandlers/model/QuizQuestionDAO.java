package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages {@code QuizQuestion} entities for quizzes.
 * <p>
 * This Data Access Object (DAO) provides methods to perform CRUD operations on the
 * {@code quizQuestions} table in the SQLite database. Quiz questions are associated with
 * quizzes via a message ID and a question number, representing individual questions within
 * a quiz, and support features such as quiz delivery and evaluation.
 *
 * @author Jack
 */

public class QuizQuestionDAO implements IQuizQuestionDAO {
    private final Connection connection;


    /**
     * Initialises the {@code QuizQuestionDAO} with an SQLite database connection.
     * <p>
     * This constructor establishes a connection using the provided {@code SQLiteConnection}
     * and creates the {@code quizQuestions} table if it does not exist. The table includes
     * a composite primary key (message ID and question number) and a foreign key to the
     * {@code quizzes} table with cascading deletion to ensure quiz questions are removed
     * when their associated quiz is deleted.
     *
     * @param sqliteConnection the {@code SQLiteConnection} instance for database access
     * @throws SQLException if a database error occurs during initialisation
     * @throws RuntimeException if the SQLite connection cannot be established
     */

    public QuizQuestionDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    /**
     * Creates the {@code quizQuestions} table in the SQLite database.
     * <p>
     * This method defines the schema for the {@code quizQuestions} table, including columns
     * for message ID, question number (with a constraint ensuring it is at least 1), and
     * question content. The table uses a composite primary key (message ID and question
     * number) and a foreign key to the {@code quizzes} table with cascading deletion.
     *
     * @throws SQLException if a database error occurs during table creation
     */

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS quizQuestions ("
                    + "messageId INTEGER,"
                    + "number INTEGER CHECK (number >= 1),"
                    + "question VARCHAR NOT NULL,"
                    + "PRIMARY KEY (messageId, number),"
                    + "FOREIGN KEY(messageId) REFERENCES quizzes(messageId) ON DELETE CASCADE"
                    + ")"
            );
        }
    }

    /**
     * Saves a new {@code QuizQuestion} entity to the database.
     * <p>
     * This method inserts a {@code QuizQuestion} entity into the {@code quizQuestions} table,
     * storing its message ID, question number, and question content. The message ID must
     * correspond to an existing quiz in the {@code quizzes} table, and the question number
     * must be unique for that message ID.
     *
     * @param quizQuestion the {@code QuizQuestion} entity to save
     * @throws SQLException if a database error occurs during insertion
     */

    @Override
    public void createQuizQuestion(QuizQuestion quizQuestion) throws SQLException {
        String sql = "INSERT INTO quizQuestions (messageId, number, question) VALUES (?, ?, ?)";
        try (PreparedStatement createQuizQuestion = connection.prepareStatement(sql)) {
            createQuizQuestion.setInt(1, quizQuestion.getMessageId());
            createQuizQuestion.setInt(2, quizQuestion.getNumber());
            createQuizQuestion.setString(3, quizQuestion.getQuestion());
            createQuizQuestion.executeUpdate();
        }
    }

    /**
     * Retrieves a {@code QuizQuestion} entity by its message ID and question number.
     * <p>
     * This method fetches a single {@code QuizQuestion} entity from the {@code quizQuestions}
     * table that matches the specified message ID and question number. Returns {@code null}
     * if no quiz question is found for the given key.
     *
     * @param messageId the ID of the associated quiz
     * @param number the question number within the quiz
     * @return the {@code QuizQuestion} entity, or {@code null} if none exists
     * @throws IllegalArgumentException if {@code messageId} or {@code number} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public QuizQuestion getQuizQuestion(int messageId, int number) throws IllegalArgumentException, SQLException {
        String sql = "SELECT * FROM quizQuestions WHERE messageId = ? AND number = ?";
        try (PreparedStatement readQuizQuestion = connection.prepareStatement(sql)) {
            readQuizQuestion.setInt(1, messageId);
            readQuizQuestion.setInt(2, number);
            ResultSet resultSet = readQuizQuestion.executeQuery();

            if (resultSet.next()) {
                String question = resultSet.getString("question");
                return new QuizQuestion(messageId, number, question);
            }
        }
        return null;
    }

    /**
     * Retrieves all {@code QuizQuestion} entities for a specific quiz.
     * <p>
     * This method fetches all quiz questions associated with the specified message ID
     * from the {@code quizQuestions} table. Returns a list of {@code QuizQuestion} entities,
     * which may be empty if no questions are found for the quiz.
     *
     * @param messageId the ID of the associated quiz
     * @return a {@code List} of {@code QuizQuestion} entities for the quiz, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} is negative
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public List<QuizQuestion> getAllQuizQuestions(int messageId) throws IllegalArgumentException, SQLException {
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        String sql = "SELECT * FROM quizQuestions WHERE messageId = ?";
        try (PreparedStatement readQuizQuestions = connection.prepareStatement(sql)) {
            readQuizQuestions.setInt(1, messageId);
            ResultSet resultSet = readQuizQuestions.executeQuery();

            while (resultSet.next()) {
                int number = resultSet.getInt("number");
                String question = resultSet.getString("question");
                QuizQuestion quizQuestion = new QuizQuestion(messageId, number, question);
                quizQuestions.add(quizQuestion);
            }
        }
        return quizQuestions;
    }
}
