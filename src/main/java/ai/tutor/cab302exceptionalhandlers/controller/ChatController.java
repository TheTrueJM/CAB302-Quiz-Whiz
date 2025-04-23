package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;

import java.util.List;

public class ChatController {
    public Chat createNewChat(int userId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        return null;
    }

    public List<Chat> getUserChats(int userId) {
        return null;
    }

    public Chat getChat(int id) {
        return null;
    }

    public boolean updateChatName(int id, String newName) {
        return false;
    }

    public List<Message> loadChatMessages(int id) {
        return null;
    }
}
