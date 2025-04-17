package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO implements IChatDAO {
    private Connection connection;


    public ChatDAO() {
        connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS chats ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "userId INTEGER NOT NULL"
                            + "name VARCHAR NOT NULL,"
                            + "responseAttitude VARCHAR NOT NULL,"
                            + "quizDifficulty VARCHAR NOT NULL,"
                            + "educationLevel VARCHAR,"
                            + "studyArea VARCHAR,"
                            + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createChat(Chat chat) {
        try {
            PreparedStatement createChat = connection.prepareStatement(
                    "INSERT INTO users (userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea) VALUES (?, ?, ?, ?, ?, ?)"
            );
            createChat.setInt(1, chat.getUserId());
            createChat.setString(2, chat.getName());
            createChat.setString(3, chat.getResponseAttitude());
            createChat.setString(4, chat.getQuizDifficulty());
            createChat.setString(5, chat.getEducationLevel());
            createChat.setString(6, chat.getStudyArea());
            createChat.executeUpdate();

            // Set the id of the new Chat
            Statement getKey = connection.createStatement();
            ResultSet generatedKeys = getKey.getGeneratedKeys();
            if (generatedKeys.next()) {
                chat.setId(generatedKeys.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateChat(Chat chat) {
        try {
            PreparedStatement updateChat = connection.prepareStatement(
                    "UPDATE chats SET userId = ?, name = ?, responseAttitude = ?, quizDifficulty = ?, educationLevel = ?, studyArea = ? WHERE id = ?"
            );
            updateChat.setInt(1, chat.getUserId());
            updateChat.setString(2, chat.getName());
            updateChat.setString(3, chat.getResponseAttitude());
            updateChat.setString(4, chat.getQuizDifficulty());
            updateChat.setString(5, chat.getEducationLevel());
            updateChat.setString(6, chat.getStudyArea());
            updateChat.setInt(7, chat.getId());
            updateChat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteChat(Chat chat) {
        try {
            PreparedStatement deleteChat = connection.prepareStatement(
                    "DELETE FROM chats WHERE id = ?"
            );
            deleteChat.setInt(1, chat.getId());
            deleteChat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Chat getChat(int id) {
        try {
            PreparedStatement readChat = connection.prepareStatement(
                    "SELECT * FROM chats WHERE id = ?"
            );
            readChat.setInt(1, id);
            ResultSet resultSet = readChat.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("userId");
                String name = resultSet.getString("name");
                String responseAttitude = resultSet.getString("responseAttitude");
                String quizDifficulty = resultSet.getString("quizDifficulty");
                String educationLevel = resultSet.getString("educationLevel");
                String studyArea = resultSet.getString("studyArea");
                Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea);
                chat.setId(id);
                return chat;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Chat> getAllUserChats(int userId) {
        List<Chat> userChats = new ArrayList<>();
        try {
            PreparedStatement readUserChats = connection.prepareStatement(
                    "SELECT * FROM chats WHERE userId = ?"
            );
            readUserChats.setInt(1, userId);
            ResultSet resultSet = readUserChats.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String responseAttitude = resultSet.getString("responseAttitude");
                String quizDifficulty = resultSet.getString("quizDifficulty");
                String educationLevel = resultSet.getString("educationLevel");
                String studyArea = resultSet.getString("studyArea");
                Chat chat = new Chat(userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea);
                chat.setId(id);
                userChats.add(chat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userChats;
    }
}
