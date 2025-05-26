package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for conducting database CRUD operations for quiz answer options.
 * <p>
 * This Data Access Object (DAO) provides methods to create and retrieve
 * answer options for quiz questions in the SQLite database.
 * It manages the `answerOptions` table, which stores
 * answer options associated with quiz questions.
 *
 * @author Joshua M.
 */
public class AnswerOptionDAO implements IAnswerOptionDAO {
    private final Connection connection;

    /**
     * Constructor for {@link AnswerOptionDAO} initializing answerOptions table.
     *
     * @param sqliteConnection the {@link SQLiteConnection} instance for database access
     * @throws SQLException if a database error occurs when creating answerOptions table
     * @throws RuntimeException if the SQLite connection cannot be established
     * @see #createTable()
     */
    public AnswerOptionDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    /**
     * Creates the {@link answerOptions} table in the SQLite database.
     * <p>
     * If the table does not exist, it creates a new table named answerOptions.
     *
     * @throws SQLException if a database error occurs during table creation
     * @see {@link quizQuestions}
     */
    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS answerOptions ("
                    + "messageId INTEGER,"
                    + "questionNumber INTEGER,"
                    + "option VARCHAR,"
                    + "value VARCHAR NOT NULL,"
                    + "isAnswer INTEGER NOT NULL,"
                    + "PRIMARY KEY (messageId, questionNumber, option),"
                    + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE"
                    + ")"
            );
        }
    }

    /**
     * Inserts an {@link AnswerOption} into the database.
     * <p>
     * This method inserts an {@link AnswerOption} into the {@code answerOptions} table,
     * storing its message ID, question number, option identifier, value, and whether it
     * is the correct answer.
     *
     * @param answerOption the {@link AnswerOption} object to insert
     * @throws SQLException if a database error occurs during insertion
     */
    @Override
    public void createAnswerOption(AnswerOption answerOption) throws SQLException {
        String sql = "INSERT INTO answerOptions (messageId, questionNumber, option, value, isAnswer) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement createAnswerOption = connection.prepareStatement(sql)) {
            createAnswerOption.setInt(1, answerOption.getMessageId());
            createAnswerOption.setInt(2, answerOption.getQuestionNumber());
            createAnswerOption.setString(3, answerOption.getOption());
            createAnswerOption.setString(4, answerOption.getValue());
            createAnswerOption.setInt(5, answerOption.getIsAnswer() ? 1 : 0);
            createAnswerOption.executeUpdate();
        }
    }


    /**
     * Retrieves a specific {@code AnswerOption} for a quiz question.
     * <p>
     * This method fetches an {@code AnswerOption} from the {@code answerOptions} table using
     * the provided message ID, question number, and option identifier. It returns the matching
     * answer option or {@code null} if no such option exists.
     *
     * @param messageId the ID of the quiz message
     * @param questionNumber the number of the question within the quiz
     * @param option the identifier of the answer option
     * @return the {@code AnswerOption} object if found, or {@code null} if no matching option exists
     * @throws IllegalArgumentException if {@code messageId} or {@code questionNumber} is negative, or {@code option} is {@code null} or empty
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) throws IllegalArgumentException, SQLException {
        String sql = "SELECT * FROM answerOptions WHERE messageId = ? AND questionNumber = ? AND option = ?";
        try (PreparedStatement readQuestionAnswerOption = connection.prepareStatement(sql)) {
            readQuestionAnswerOption.setInt(1, messageId);
            readQuestionAnswerOption.setInt(2, questionNumber);
            readQuestionAnswerOption.setString(3, option);
            ResultSet resultSet = readQuestionAnswerOption.executeQuery();

            if (resultSet.next()) {
                String value = resultSet.getString("value");
                int isAnswer = resultSet.getInt("isAnswer");
                return new AnswerOption(messageId, questionNumber, option, value, isAnswer == 1);
            }
        }
        return null;
    }

    /**
     * Retrieves all {@code AnswerOption} entities for a specific quiz question.
     * <p>
     * This method fetches all answer options associated with a quiz question identified by
     * message ID and question number. It returns a list of answer options, which may be empty
     * if no options exist.
     *
     * @param messageId the ID of the quiz message
     * @param questionNumber the number of the question within the quiz
     * @return a {@code List} of {@link AnswerOption} objects for the question, or an empty list if none exist
     * @throws IllegalArgumentException if {@code messageId} or {@code questionNumber} is negative
     * @throws SQLException if a database error occurs during fetching
     */

    @Override
    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) throws IllegalArgumentException, SQLException {
        List<AnswerOption> questionAnswerOptions = new ArrayList<>();
        String sql = "SELECT * FROM answerOptions WHERE messageId = ? AND questionNumber = ?";
        try (PreparedStatement readQuestionAnswerOptions = connection.prepareStatement(sql)) {
            readQuestionAnswerOptions.setInt(1, messageId);
            readQuestionAnswerOptions.setInt(2, questionNumber);
            ResultSet resultSet = readQuestionAnswerOptions.executeQuery();

            while (resultSet.next()) {
                String option = resultSet.getString("option");
                String value = resultSet.getString("value");
                int isAnswer = resultSet.getInt("isAnswer");
                AnswerOption answerOption = new AnswerOption(messageId, questionNumber, option, value, isAnswer == 1);
                questionAnswerOptions.add(answerOption);
            }
        }
        return questionAnswerOptions;
    }
}
