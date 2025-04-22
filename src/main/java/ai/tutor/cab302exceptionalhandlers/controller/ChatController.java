package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.ChatDAO;
import ai.tutor.cab302exceptionalhandlers.model.Message;
import ai.tutor.cab302exceptionalhandlers.model.MessageDAO;

import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;

public class ChatController {
    @FXML
    private
    private ChatDAO chatDAO;
    private MessageDAO messageDAO;

    public ChatController(ChatDAO chatDAO, MessageDAO messageDAO) {
        this.chatDAO = chatDAO;
        this.messageDAO = messageDAO;

    }

    // Creates a new chat and adds the first messageDAO
    public Chat createNewChat(int userId, String name, String responseAttitude,
                              String quizDifficulty, String educationLevel,
                              String studyArea) {

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
            System.err.println("Failed to create chat or messageDAO: " + e.getMessage());
            return null;
        }
    }

    // Get all chats for a user
    public List<Chat> getUserChats(int userId) {
        return chatDAO.getAllUserChats(userId);
    }

    // Load messages for a specific chat
    public List<Message> loadChatMessages(int chatId) {
        return messageDAO.getAllChatMessages(chatId);
    }

    // Get specific chat with id
    public Chat getChatById(int chatId) {return chatDAO.getChat(chatId);}

    // Update a chat's name
    public boolean updateChatName(int chatId, String newName) {
        if (chatId <= 0) {
            System.err.println("Invalid chat ID");
            return false;
        }
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
                Chat updatedChat = new Chat(currentChat.getUserId(), newName, currentChat.getResponseAttitude(),
                        currentChat.getQuizDifficulty(), currentChat.getEducationLevel(),
                        currentChat.getStudyArea());

                chatDAO.updateChat(updatedChat);
                return true;
            } catch (SQLException e) {
                System.err.println("Failed to update chat name: " + e.getMessage());
                return false;
            }
        }
    }
