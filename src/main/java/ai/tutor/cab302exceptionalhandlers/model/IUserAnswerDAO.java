package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IUserAnswerDAO {
    public void createUserAnswer(UserAnswer userAnswer);

    public UserAnswer getUserAnswer(int messageId, int attempt, int questionNumber);

    public List<UserAnswer> getAllUserAttempts(int messageId, int questionNumber);

    public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt);

    public List<UserAnswer> getAllUserQuizAttempts(int messageId);
}
