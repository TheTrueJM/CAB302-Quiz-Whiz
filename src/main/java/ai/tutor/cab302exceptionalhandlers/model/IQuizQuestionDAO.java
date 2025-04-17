package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IQuizQuestionDAO {
    public void createQuizQuestion(QuizQuestion quizQuestion);

    public QuizQuestion getQuizQuestion(int messageId, int number);

    public List<QuizQuestion> getAllQuizQuestions(int messageId);
}
