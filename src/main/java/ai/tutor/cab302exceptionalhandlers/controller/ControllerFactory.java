package ai.tutor.cab302exceptionalhandlers.controller;

import java.io.IOException;
import java.sql.SQLException;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Quiz;

public class ControllerFactory extends AbstractControllerFactory {
    public ControllerFactory(SQLiteConnection db) {
        super(db);
    }

    @Override
    public AuthController createAuthController(String type) {
        try {
            switch (type) {
                case "login":
                    return new LoginController(db);
                case "signup":
                    return new SignUpController(db);
                default:
                    throw new IllegalArgumentException("Invalid controller type: " + type);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create auth controller: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatController createChatController(User currentUser) {
        try {
            return new ChatController(db, currentUser);
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to create chat controller: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatSetupController createChatSetupController(User currentUser, String type) {
        return createChatSetupController(currentUser, type, null);
    }

    @Override
    public ChatSetupController createChatSetupController(User currentUser, String type, Chat currentChat) {
        try {
            switch (type) {
                case "create":
                    return new ChatCreateController(db, currentUser);
                case "update":
                    if (currentChat == null) {
                        throw new IllegalArgumentException("Missing currentChat argument for ChatUpdateController");
                    }
                    return new ChatUpdateController(db, currentUser, currentChat);
                default:
                    throw new IllegalArgumentException("Invalid controller type: " + type);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create chat setup controller: " + e.getMessage(), e);
        }
    }

    @Override
    public UserSettingsController createUserSettingsController(User currentUser) {
        try {
            return new UserSettingsController(db, currentUser);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user settings controller: " + e.getMessage(), e);
        }
    }

    @Override
    public QuizController createQuizController(Quiz quiz, User currentUser) {
        return new QuizController(db, quiz, currentUser);
    }
}
