package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO implements IQuizDAO {
    private Connection connection;


    public QuizDAO() {
        connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS quizzes ("
                            + "messageId INTEGER PRIMARY KEY NOT NULL,"
                            + "name VARCHAR NOT NULL,"
                            + "difficulty VARCHAR NOT NULL,"
                            + "FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createChat(Quiz quiz) {
        try {
            PreparedStatement createQuiz = connection.prepareStatement(
                    "INSERT INTO quizzes (messageId, name, difficulty) VALUES (?, ?, ?)"
            );
            createQuiz.setInt(1, quiz.getMessageId());
            createQuiz.setString(2, quiz.getName());
            createQuiz.setString(3, quiz.getDifficulty());
            createQuiz.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Quiz getQuiz(int messageId) {
        try {
            PreparedStatement readQuiz = connection.prepareStatement("SELECT * FROM quizzes WHERE id = ?");
            readQuiz.setInt(1, messageId);
            ResultSet resultSet = readQuiz.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String difficulty = resultSet.getString("difficulty");
                return new Quiz(messageId, name, difficulty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Quiz> getAllChatQuizzes(int chatId) {
        List<Quiz> chatQuizzes = new ArrayList<>();
        try {
            PreparedStatement readChatQuizzes = connection.prepareStatement("SELECT id FROM messages WHERE chatId = ? AND isQuiz = ?");
            readChatQuizzes.setInt(1, chatId);
            readChatQuizzes.setInt(2, true ? 1 : 0);
            ResultSet resultSet = readChatQuizzes.executeQuery();
            while (resultSet.next()) {
                int messageId = resultSet.getInt("id");
                Quiz quiz = getQuiz(messageId);
                chatQuizzes.add(quiz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatQuizzes;
    }

    @Override
    public List<Quiz> getAllUserQuizzes(int userId) {
        List<Quiz> userQuizzes = new ArrayList<>();
        try {
            PreparedStatement readUserChats = connection.prepareStatement("SELECT id FROM chats WHERE userId = ?");
            readUserChats.setInt(1, userId);
            ResultSet resultSet = readUserChats.executeQuery();
            while (resultSet.next()) {
                int chatId = resultSet.getInt("id");
                List<Quiz> chatQuizzes = getAllChatQuizzes(chatId);
                userQuizzes.addAll(chatQuizzes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userQuizzes;
    }
}
