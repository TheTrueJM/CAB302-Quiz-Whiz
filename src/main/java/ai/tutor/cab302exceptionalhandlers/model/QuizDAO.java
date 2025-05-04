package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO implements IQuizDAO {
    private final Connection connection;

    public QuizDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute("CREATE TABLE IF NOT EXISTS quizzes (" + "messageId INTEGER PRIMARY KEY,"
                    + "name VARCHAR NOT NULL," + "difficulty VARCHAR NOT NULL,"
                    + "FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE" + ")");
        }
    }

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
