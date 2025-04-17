package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IAnswerOptionDAO {
    public void createAnswerOption(AnswerOption answerOption);

    public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option);

    public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber);
}
