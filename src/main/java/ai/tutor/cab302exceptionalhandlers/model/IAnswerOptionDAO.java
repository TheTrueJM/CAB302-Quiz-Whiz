package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IAnswerOptionDAO {
	public void createAnswerOption(AnswerOption answerOption) throws SQLException;

	public AnswerOption getQuestionAnswerOption(int messageId, int questionNumber, String option) throws SQLException;

	public List<AnswerOption> getAllQuestionAnswerOptions(int messageId, int questionNumber) throws SQLException;
}
