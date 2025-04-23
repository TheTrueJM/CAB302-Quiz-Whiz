package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.*;

import java.util.List;

public class ChatController {
    // Create a Chat object using UI user input
    public Chat createNewChat(int userId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        return null;
    }

    // Retrieve Chat objects for a specific User
    public List<Chat> getUserChats(int userId) {
        return null;
    }

    // Retrieve a specific Chat object
    public Chat getChat(int id) {
        return null;
    }

    // Update the name of a Chat object
    public boolean updateChatName(int id, String newName) {
        return false;
    }

    // Create a Message object using UI user input
    public Message createNewChatMessage(int chatId, String content, boolean fromUser, boolean isQuiz) {
        return null;
    }

    // Create a Message object from the AI's response output using a user's Message object as input
    // If AI generation fails, create the Message object with default feedback content
    public Message generateChatMessageResponse(Message userMessage) {
        return null;
    }

    // Retrieve Chat objects for a specific Chat
    public List<Message> getChatMessages(int chatId) {
        return null;
    }

    // Create a Quiz object from the AI's response message if it is a quiz message
    public Quiz createNewQuiz(String quizContent, Message responseMessage) {
        return null;
    }

    // Create a QuizQuestion object from the AI's response message if it is a quiz message
    public QuizQuestion createNewQuizQuestion(String questionContent, Quiz quiz) {
        return null;
    }

    // Create an AnswerOption object from the AI's response message if it is a quiz message
    public AnswerOption createNewQuestionAnswerOption(String answerOptionContent, QuizQuestion quizQuestion) {
        return null;
    }
}
