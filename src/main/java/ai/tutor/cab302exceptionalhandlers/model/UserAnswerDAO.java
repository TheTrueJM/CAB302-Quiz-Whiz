package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAnswerDAO implements IUserAnswerDAO {
    private Connection connection;


    public UserAnswerDAO() {
        connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS userAnswers ("
                            + "messageId INTEGER PRIMARY KEY NOT NULL,"
                            + "attempt INTEGER PRIMARY KEY NOT NULL,"
                            + "questionNumber INTEGER PRIMARY KEY NOT NULL,"
                            + "answerOption VARCHAR NOT NULL,"
                            + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE"
                            + "FOREIGN KEY(answerOption) REFERENCES answerOptions(option)"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createUserAnswer(UserAnswer userAnswer) {
        try {
            PreparedStatement createUserAnswer = connection.prepareStatement(
                    "INSERT INTO userAnswers (messageId, attempt, questionNumber, answerOption) VALUES (?, ?, ?, ?)"
            );
            createUserAnswer.setInt(1, userAnswer.getMessageId());
            createUserAnswer.setInt(2, userAnswer.getAttempt());
            createUserAnswer.setInt(3, userAnswer.getQuestionNumber());
            createUserAnswer.setString(4, userAnswer.getAnswerOption());
            createUserAnswer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserAnswer getUserAnswer(int messageId, int attempt, int questionNumber) {
        try {
            PreparedStatement readUserAnswer = connection.prepareStatement(
                    "SELECT * FROM userAnswers WHERE messageId = ? AND attempt = ? AND questionNumber = ?"
            );
            readUserAnswer.setInt(1, messageId);
            readUserAnswer.setInt(2, attempt);
            readUserAnswer.setInt(3, questionNumber);
            ResultSet resultSet = readUserAnswer.executeQuery();
            if (resultSet.next()) {
                String answerOption = resultSet.getString("answerOption");
                return new UserAnswer(messageId, attempt, questionNumber, answerOption);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UserAnswer> getAllUserAttempts(int messageId, int questionNumber) {
        List<UserAnswer> userAttempts = new ArrayList<>();
        try {
            PreparedStatement readUserAttempts = connection.prepareStatement(
                    "SELECT * FROM userAnswers WHERE messageId = ? AND questionNumber = ?"
            );
            readUserAttempts.setInt(1, messageId);
            readUserAttempts.setInt(2, questionNumber);
            ResultSet resultSet = readUserAttempts.executeQuery();
            while (resultSet.next()) {
                int attempt = resultSet.getInt("attempt");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userAttempt = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userAttempts.add(userAttempt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userAttempts;
    }

    @Override
    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) {
        List<UserAnswer> userQuizAnswers = new ArrayList<>();
        try {
            PreparedStatement readUserQuizAnswers = connection.prepareStatement(
                    "SELECT * FROM userAnswers WHERE messageId = ? AND attempt = ?"
            );
            readUserQuizAnswers.setInt(1, messageId);
            readUserQuizAnswers.setInt(2, attempt);
            ResultSet resultSet = readUserQuizAnswers.executeQuery();
            while (resultSet.next()) {
                int questionNumber = resultSet.getInt("questionNumber");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userAnswer = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userQuizAnswers.add(userAnswer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userQuizAnswers;
    }

    @Override
    public List<UserAnswer> getAllUserQuizAttempts(int messageId) {
        List<UserAnswer> userQuizAttempts = new ArrayList<>();
        try {
            PreparedStatement readUserQuizAttempts = connection.prepareStatement(
                    "SELECT * FROM userAnswers WHERE messageId = ?"
            );
            readUserQuizAttempts.setInt(1, messageId);
            ResultSet resultSet = readUserQuizAttempts.executeQuery();
            while (resultSet.next()) {
                int attempt = resultSet.getInt("attempt");
                int questionNumber = resultSet.getInt("questionNumber");
                String answerOption = resultSet.getString("answerOption");
                UserAnswer userAttempt = new UserAnswer(messageId, attempt, questionNumber, answerOption);
                userQuizAttempts.add(userAttempt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userQuizAttempts;
    }
}
