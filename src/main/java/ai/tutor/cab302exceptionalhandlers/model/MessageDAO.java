package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO implements IMessageDAO {
    private final Connection connection;


    public MessageDAO(SQLiteConnection sqliteConnection) {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS messages ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "chatId INTEGER NOT NULL"
                            + "content VARCHAR NOT NULL,"
                            + "fromUser INTEGER NOT NULL,"
                            + "isQuiz INTEGER NOT NULL,"
                            + "FOREIGN KEY(chatId) REFERENCES chats(id) ON DELETE CASCADE"
                            + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createMessage(Message message) throws SQLException{

        String sql = "INSERT INTO messages (chatId, content, fromUser, isQuiz) VALUES (?, ?, ?, ?)";
        try (PreparedStatement createMessage = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            createMessage.setInt(1, message.getChatId());
            createMessage.setString(2, message.getContent());
            createMessage.setInt(3, message.getFromUser() ? 1 : 0);
            createMessage.setInt(4, message.getIsQuiz() ? 1 : 0);
            createMessage.executeUpdate();

            // Set the id of the new Chat
            try (ResultSet generatedKeys = createMessage.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Message> getAllChatMessages(int chatId) throws SQLException{
        List<Message> chatMessages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE chatId = ?";
        try (PreparedStatement readUserChats = connection.prepareStatement(sql)) {
            readUserChats.setInt(1, chatId);
            ResultSet resultSet = readUserChats.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String content = resultSet.getString("content");
                int fromUser = resultSet.getInt("fromUser");
                int isQuiz = resultSet.getInt("isQuiz");
                Message message = new Message(chatId, content, fromUser == 1, isQuiz == 1);
                message.setId(id);
                chatMessages.add(message);
            }
        }
        return chatMessages;
    }
}
