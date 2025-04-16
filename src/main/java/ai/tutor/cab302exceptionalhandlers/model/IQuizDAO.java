package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IQuizDAO {
    public void createChat(Quiz quiz);

    public Quiz getQuiz(int messageId);

    public List<Quiz> getAllChatQuizzes(int chatId);

    public List<Quiz> getAllUserQuizzes(int userId);
}
