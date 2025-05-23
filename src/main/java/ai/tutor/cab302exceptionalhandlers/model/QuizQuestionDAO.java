package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionDAO implements IQuizQuestionDAO {
    private final Connection connection;


    public QuizQuestionDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

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
