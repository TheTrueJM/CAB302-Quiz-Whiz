package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerOptionDAO implements IAnswerOptionDAO {
    private final Connection connection;


    public AnswerOptionDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS answerOptions ("
                    + "messageId INTEGER PRIMARY KEY,"
                    + "questionNumber INTEGER PRIMARY KEY,"
                    + "option VARCHAR PRIMARY KEY,"
                    + "value VARCHAR NOT NULL,"
                    + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE"
                    + ")"
            );
        }
    }


    @Override
    public void createAnswerOption(AnswerOption answerOption) throws SQLException {
        String sql = "INSERT INTO answerOptions (messageId, questionNumber, option, value) VALUES (?, ?, ?, ?)";
        try (PreparedStatement createAnswerOption = connection.prepareStatement(sql)) {
            createAnswerOption.setInt(1, answerOption.getMessageId());
            createAnswerOption.setInt(2, answerOption.getQuestionNumber());
            createAnswerOption.setString(3, answerOption.getOption());
            createAnswerOption.setString(4, answerOption.getValue());
            createAnswerOption.executeUpdate();
        }
    }

    @Override
    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) throws SQLException {
        String sql = "SELECT * FROM answerOptions WHERE messageId = ? AND questionNumber = ? AND option = ?";
        try (PreparedStatement readQuestionAnswerOption = connection.prepareStatement(sql)) {
            readQuestionAnswerOption.setInt(1, messageId);
            readQuestionAnswerOption.setInt(2, questionNumber);
            readQuestionAnswerOption.setString(3, option);
            ResultSet resultSet = readQuestionAnswerOption.executeQuery();

            if (resultSet.next()) {
                String value = resultSet.getString("value");
                return new AnswerOption(messageId, questionNumber, option, value);
            }
        }
        return null;
    }

    @Override
    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) throws SQLException {
        List<AnswerOption> questionAnswerOptions = new ArrayList<>();
        String sql = "SELECT * FROM quizQuestions WHERE messageId = ? AND questionNumber = ?";
        try (PreparedStatement readQuestionAnswerOptions = connection.prepareStatement(sql)) {
            readQuestionAnswerOptions.setInt(1, messageId);
            readQuestionAnswerOptions.setInt(2, questionNumber);
            ResultSet resultSet = readQuestionAnswerOptions.executeQuery();

            while (resultSet.next()) {
                String option = resultSet.getString("option");
                String value = resultSet.getString("value");
                AnswerOption answerOption = new AnswerOption(messageId, questionNumber, option, value);
                questionAnswerOptions.add(answerOption);
            }
        }
        return questionAnswerOptions;
    }
}
