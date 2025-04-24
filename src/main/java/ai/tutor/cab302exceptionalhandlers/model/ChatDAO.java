package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO implements IChatDAO {
    private final Connection connection;


    public ChatDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS chats ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "userId INTEGER NOT NULL,"
                    + "name VARCHAR NOT NULL,"
                    + "responseAttitude VARCHAR NOT NULL,"
                    + "quizDifficulty VARCHAR NOT NULL,"
                    + "educationLevel VARCHAR,"
                    + "studyArea VARCHAR,"
                    + "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE"
                    + ")"
            );
        }
    }


    @Override
    public void createChat(Chat chat) throws SQLException {
        String sql = "INSERT INTO chats (userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement createChat = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            createChat.setInt(1, chat.getUserId());
            createChat.setString(2, chat.getName());
            createChat.setString(3, chat.getResponseAttitude());
            createChat.setString(4, chat.getQuizDifficulty());
            createChat.setString(5, chat.getEducationLevel());
            createChat.setString(6, chat.getStudyArea());
            createChat.executeUpdate();

            try (ResultSet generatedKeys = createChat.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chat.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void updateChat(Chat chat) throws SQLException {
        String sql = "UPDATE chats SET name = ?, responseAttitude = ?, quizDifficulty = ?, educationLevel = ?, studyArea = ? WHERE id = ?";
        try (PreparedStatement updateChat = connection.prepareStatement(sql)) {
            updateChat.setString(1, chat.getName());
            updateChat.setString(2, chat.getResponseAttitude());
            updateChat.setString(3, chat.getQuizDifficulty());
            updateChat.setString(4, chat.getEducationLevel());
            updateChat.setString(5, chat.getStudyArea());
            updateChat.setInt(6, chat.getId());
            updateChat.executeUpdate();
        }
    }

    @Override
    public void updateChatName(Chat chat) throws SQLException {
        String sql = "UPDATE chats SET name = ? WHERE id = ?";
        try (PreparedStatement updateChatName = connection.prepareStatement(sql)) {
            updateChatName.setString(1, chat.getName());
            updateChatName.setInt(2, chat.getId());
            updateChatName.executeUpdate();
        }
    }

    @Override
    public void deleteChat(Chat chat) throws SQLException {
        String sql = "DELETE FROM chats WHERE id = ?";
        try (PreparedStatement deleteChat = connection.prepareStatement(sql)) {
            deleteChat.setInt(1, chat.getId());
            deleteChat.executeUpdate();
        }
    }

    @Override
    public Chat getChat(int id) throws SQLException {
        String sql = "SELECT * FROM chats WHERE id = ?";
        try (PreparedStatement readChat = connection.prepareStatement(sql)) {
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
        }
        return null;
    }

    @Override
    public List<Chat> getAllUserChats(int userId) throws SQLException {
        List<Chat> userChats = new ArrayList<>();
        String sql = "SELECT * FROM chats WHERE userId = ?";
        try (PreparedStatement readUserChats = connection.prepareStatement(sql)) {
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
        }
        return userChats;
    }
}
