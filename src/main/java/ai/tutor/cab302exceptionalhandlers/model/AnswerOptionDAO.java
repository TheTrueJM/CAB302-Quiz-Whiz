package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerOptionDAO implements IAnswerOptionDAO {
    private Connection connection;


    public AnswerOptionDAO() {
        connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS answerOptions ("
                            + "messageId INTEGER PRIMARY KEY NOT NULL,"
                            + "questionNumber INTEGER PRIMARY KEY NOT NULL,"
                            + "option VARCHAR NOT NULL,"
                            + "value VARCHAR NOT NULL,"
                            + "FOREIGN KEY(messageId, questionNumber) REFERENCES quizQuestions(messageId, number) ON DELETE CASCADE"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createAnswerOption(AnswerOption answerOption) {
        try {
            PreparedStatement createQuiz = connection.prepareStatement(
                    "INSERT INTO answerOptions (messageId, questionNumber, option, value) VALUES (?, ?, ?, ?)"
            );
            createQuiz.setInt(1, answerOption.getMessageId());
            createQuiz.setInt(2, answerOption.getQuestionNumber());
            createQuiz.setString(3, answerOption.getOption());
            createQuiz.setString(4, answerOption.getValue());
            createQuiz.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) {
        try {
            PreparedStatement readQuestionAnswerOption = connection.prepareStatement("SELECT * FROM answerOptions WHERE messageId = ? AND questionNumber = ? AND option = ?");
            readQuestionAnswerOption.setInt(1, messageId);
            readQuestionAnswerOption.setInt(2, questionNumber);
            readQuestionAnswerOption.setString(3, option);
            ResultSet resultSet = readQuestionAnswerOption.executeQuery();
            if (resultSet.next()) {
                String value = resultSet.getString("value");
                return new AnswerOption(messageId, questionNumber, option, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) {
        List<AnswerOption> questionAnswerOptions = new ArrayList<>();
        try {
            PreparedStatement readQuestionAnswerOptions = connection.prepareStatement("SELECT * FROM quizQuestions WHERE messageId = ? AND questionNumber = ?");
            readQuestionAnswerOptions.setInt(1, messageId);
            readQuestionAnswerOptions.setInt(2, questionNumber);
            ResultSet resultSet = readQuestionAnswerOptions.executeQuery();
            while (resultSet.next()) {
                String option = resultSet.getString("option");
                String value = resultSet.getString("value");
                AnswerOption answerOption = new AnswerOption(messageId, questionNumber, option, value);
                questionAnswerOptions.add(answerOption);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionAnswerOptions;
    }
}
