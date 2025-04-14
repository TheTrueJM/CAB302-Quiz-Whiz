package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO implements IMessageDAO {
    private Connection connection;


    public MessageDAO() {
        connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS messages ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "chatId INTEGER NOT NULL"
                            + "text VARCHAR NOT NULL,"
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
    public void createMessage(Message message) {
        try {
            PreparedStatement createMessage = connection.prepareStatement(
                    "INSERT INTO messages (chatId, text, fromUser, isQuiz) VALUES (?, ?, ?, ?)"
            );
            createMessage.setInt(1, message.getChatId());
            createMessage.setString(2, message.getText());
            createMessage.setInt(3, message.getFromUser() ? 1 : 0);
            createMessage.setInt(4, message.getIsQuiz() ? 1 : 0);
            createMessage.executeUpdate();

            // Set the id of the new Chat
            Statement getKey = connection.createStatement();
            ResultSet generatedKeys = getKey.getGeneratedKeys();
            if (generatedKeys.next()) {
                message.setId(generatedKeys.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> getAllChatMessages(int chatId) {
        List<Message> chatMessages = new ArrayList<>();
        try {
            PreparedStatement readUserChats = connection.prepareStatement("SELECT * FROM messages WHERE chatId = ?");
            readUserChats.setInt(1, chatId);
            ResultSet resultSet = readUserChats.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String text = resultSet.getString("text");
                int fromUser = resultSet.getInt("fromUser");
                int isQuiz = resultSet.getInt("isQuiz");
                Message message = new Message(chatId, text, fromUser == 1, isQuiz == 1);
                message.setId(id);
                chatMessages.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatMessages;
    }
}
