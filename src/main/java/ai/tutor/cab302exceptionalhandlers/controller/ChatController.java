package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import java.sql.SQLException;
import java.util.List;

public class ChatController {
    private SQLiteConnection db;
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;

    public ChatController() {
        try {
            db = new SQLiteConnection();
            this.chatDAO = new ChatDAO(db);
            this.messageDAO = new MessageDAO(db);
        } catch (SQLException | RuntimeException e) {
            System.err.println("SQL database connection error: " + e.getMessage());
        }
    }

    // Creates a new chat and adds the first messageDAO
    public Chat createNewChat(int userId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return null;
        }

        // Create new chat object
        Chat newChat = new Chat(userId, name, responseAttitude, quizDifficulty, educationLevel, studyArea);

        try {
            // Add new chat to database
            chatDAO.createChat(newChat);
            return newChat;
        } catch (SQLException e) {
            System.err.println("Failed to create chat: " + e.getMessage());
            return null;
        }
    }

    // Get all chats for a user
    public List<Chat> getUserChats(int userId) {
        try {
            return chatDAO.getAllUserChats(userId);
        } catch (SQLException e) {
            System.err.println("Failed to read chat: " + e.getMessage());
            return null;
        }
    }

    public Chat getChat(int chatId) {
        try {
            return chatDAO.getChat(chatId);
        } catch (SQLException e) {
            System.err.println("Failed to read chat: " + e.getMessage());
            return null;
        }
    }

    public boolean updateChatName(int chatId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            System.err.println("Chat name cannot be empty");
            return false;
        }

        try {
            Chat currentChat = chatDAO.getChat(chatId);
            if (currentChat == null) {
                System.err.println("Chat not found with ID: " + chatId);
                return false;
            }

            currentChat.setName(newName);
            chatDAO.updateChatName(currentChat);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update chat name: " + e.getMessage());
            return false;
        }
    }

    public List<Message> loadChatMessages(int chatId) {
        try {
            return messageDAO.getAllChatMessages(chatId);
        } catch (SQLException e) {
            System.err.println("Failed to read chat: " + e.getMessage());
            return null;
        }
    }
}
