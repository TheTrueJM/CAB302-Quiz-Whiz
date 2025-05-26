package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages {@code Quiz} entities for chat sessions.
 * <p>
 * This Data Access Object (DAO) provides methods to perform CRUD operations and queries
 * on the {@code quizzes} table in the SQLite database. Quizzes are associated with
 * messages via a message ID, representing AI-generated quiz content within chat sessions.
 *
 * @author Jack
 */

public class QuizDAO implements IQuizDAO {
    private final Connection connection;

    /**
     * Initialises the {@code QuizDAO} with an SQLite database connection.
     * <p>
     * This constructor establishes a connection using the provided {@code SQLiteConnection}
     * and creates the {@code quizzes} table if it does not exist. The table includes a
     * foreign key to the {@code messages} table with cascading deletion to ensure quizzes
     * are removed when their associated message is deleted.
     *
     * @param sqliteConnection the {@code SQLiteConnection} instance for database access
     * @throws SQLException if a database error occurs during initialisation
     * @throws RuntimeException if the SQLite connection cannot be established
     */

    public QuizDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    /**
     * Creates the {@code quizzes} table in the database.
     * <p>
     * This method defines the schema for the {@code quizzes} table, including columns for
     * message ID (primary key), name, and difficulty. The table includes a foreign
     * key to the {@code messages} table with cascading deletion.
     *
     * @throws SQLException if a database error occurs during table creation
     */

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS quizzes ("
                    + "messageId INTEGER PRIMARY KEY,"
                    + "name VARCHAR NOT NULL,"
                    + "difficulty VARCHAR NOT NULL,"
                    + "FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE"
                    + ")"
            );
        }
    }

    /**
     * Saves a new {@code Quiz} entity to the database.
     * <p>
     * This method inserts a {@code Quiz} entity into the {@code quizzes} table, storing
     * its message ID, name, and difficulty. The message ID is the primary key and
     * must correspond to an existing message in the {@code messages} table.
     *
     * @param quiz the {@code Quiz} entity to save
     * @throws SQLException if a database error occurs during insertion
     */

    @Override
    public void createQuiz(Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quizzes (messageId, name, difficulty) VALUES (?, ?, ?)";
        try (PreparedStatement createQuiz = connection.prepareStatement(sql)) {
            createQuiz.setInt(1, quiz.getMessageId());
            createQuiz.setString(2, quiz.getName());
            createQuiz.setString(3, quiz.getDifficulty());
            createQuiz.executeUpdate();
        }
    }

    /**
     * Retrieves a {@code Quiz} entity by its associated message ID.
     * <p>
     * This method fetches a single {@code Quiz} entity from the {@code quizzes} table
     * that matches the specified message ID. Returns {@code null} if no quiz is found
     * for the given message ID.
     *
     * @param messageId the ID of the associated message
     * @return the {@code Quiz} entity, or {@code null} if none exists
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public Quiz getQuiz(int messageId) throws SQLException {
        String sql = "SELECT * FROM quizzes WHERE messageId = ?";
        try (PreparedStatement readQuiz = connection.prepareStatement(sql)) {
            readQuiz.setInt(1, messageId);
            ResultSet resultSet = readQuiz.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String difficulty = resultSet.getString("difficulty");
                return new Quiz(messageId, name, difficulty);
            }
        }
        return null;
    }

    /**
     * Retrieves all {@code Quiz} entities for a specific chat session.
     * <p>
     * This method fetches all quizzes associated with AI-generated messages marked as
     * quizzes (where {@code fromUser} is false and {@code isQuiz} is true) within the
     * specified chat session. Returns a list of {@code Quiz} entities, which may be empty
     * if no quizzes are found for the chat ID.
     *
     * @param chatId the ID of the chat session
     * @return a {@code List} of {@code Quiz} entities for the chat, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public List<Quiz> getAllChatQuizzes(int chatId) throws SQLException {
        List<Quiz> chatQuizzes = new ArrayList<>();
        String sql = "SELECT id FROM messages WHERE chatId = ? AND fromUser = ? AND isQuiz = ?";
        try (PreparedStatement readChatQuizzes = connection.prepareStatement(sql)) {
            readChatQuizzes.setInt(1, chatId);
            readChatQuizzes.setInt(2, false ? 1 : 0);
            readChatQuizzes.setInt(3, true ? 1 : 0);
            ResultSet resultSet = readChatQuizzes.executeQuery();

            while (resultSet.next()) {
                int messageId = resultSet.getInt("id");
                Quiz quiz = getQuiz(messageId);
                chatQuizzes.add(quiz);
            }
        }
        return chatQuizzes;
    }

    /**
     * Retrieves all {@code Quiz} entities for a specific user across all their chat sessions.
     * <p>
     * This method fetches all quizzes associated with AI-generated messages marked as
     * quizzes from all chat sessions linked to the given user ID. Returns a list of
     * {@code Quiz} entities, which may be empty if no quizzes are found for the user.
     *
     * @param userId the ID of the user
     * @return a {@code List} of {@code Quiz} entities for the user, or an empty list if none exist
     * @throws SQLException if a database error occurs during retrieval
     */

    @Override
    public List<Quiz> getAllUserQuizzes(int userId) throws SQLException {
        List<Quiz> userQuizzes = new ArrayList<>();
        String sql = "SELECT id FROM chats WHERE userId = ?";
        try (PreparedStatement readUserChats = connection.prepareStatement(sql)) {
            readUserChats.setInt(1, userId);
            ResultSet resultSet = readUserChats.executeQuery();

            while (resultSet.next()) {
                int chatId = resultSet.getInt("id");
                List<Quiz> chatQuizzes = getAllChatQuizzes(chatId);
                userQuizzes.addAll(chatQuizzes);
            }
        }
        return userQuizzes;
    }
}
