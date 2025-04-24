package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAnswerDAO implements IUserAnswerDAO {
    private final Connection connection;


    public UserAnswerDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS userAnswers ("
                    + "messageId INTEGER PRIMARY KEY NOT NULL,"
                    + "attempt INTEGER PRIMARY KEY NOT NULL,"
                    + "questionNumber INTEGER PRIMARY KEY NOT NULL,"
                    + "answerOption VARCHAR NOT NULL,"
                    + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE,"
                    + "FOREIGN KEY(answerOption) REFERENCES answerOptions(option)"
                    + ")"
            );
        }
    }


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

    @Override
    public UserAnswer getUserQuestionAnswer(int messageId, int attempt, int questionNumber) throws SQLException {
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

    @Override
    public List<UserAnswer> getAllUserQuestionAttempts(int messageId, int questionNumber) throws SQLException {
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

    @Override
    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) throws SQLException {
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

    @Override
    public List<UserAnswer> getAllUserQuizAttempts(int messageId) throws SQLException {
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
