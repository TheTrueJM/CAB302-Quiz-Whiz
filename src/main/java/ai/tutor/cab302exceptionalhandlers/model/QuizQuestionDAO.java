package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionDAO implements IQuizQuestionDAO {
    private final Connection connection;


    public QuizQuestionDAO(SQLiteConnection sqliteConnection) {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS quizQuestions ("
                            + "messageId INTEGER PRIMARY KEY NOT NULL,"
                            + "number INTEGER PRIMARY KEY NOT NULL,"
                            + "question VARCHAR NOT NULL,"
                            + "FOREIGN KEY(messageId) REFERENCES quizzes(messageId) ON DELETE CASCADE"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createQuizQuestion(QuizQuestion quizQuestion) {
        try {
            PreparedStatement createQuizQuestion = connection.prepareStatement(
                    "INSERT INTO quizQuestions (messageId, number, question) VALUES (?, ?, ?)"
            );
            createQuizQuestion.setInt(1, quizQuestion.getMessageId());
            createQuizQuestion.setInt(2, quizQuestion.getNumber());
            createQuizQuestion.setString(3, quizQuestion.getQuestion());
            createQuizQuestion.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public QuizQuestion getQuizQuestion(int messageId, int number) {
        try {
            PreparedStatement readQuizQuestion = connection.prepareStatement(
                    "SELECT * FROM quizQuestions WHERE messageId = ? AND number = ?"
            );
            readQuizQuestion.setInt(1, messageId);
            readQuizQuestion.setInt(2, number);
            ResultSet resultSet = readQuizQuestion.executeQuery();
            if (resultSet.next()) {
                String question = resultSet.getString("question");
                return new QuizQuestion(messageId, number, question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<QuizQuestion> getAllQuizQuestions(int messageId) {
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        try {
            PreparedStatement readQuizQuestions = connection.prepareStatement(
                    "SELECT * FROM quizQuestions WHERE messageId = ?"
            );
            readQuizQuestions.setInt(1, messageId);
            ResultSet resultSet = readQuizQuestions.executeQuery();
            while (resultSet.next()) {
                int number = resultSet.getInt("number");
                String question = resultSet.getString("question");
                QuizQuestion quizQuestion = new QuizQuestion(messageId, number, question);
                quizQuestions.add(quizQuestion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quizQuestions;
    }
}
