package ai.tutor.cab302exceptionalhandlers.model;

public class Message {
	private int id;
	private final int chatId;
	private final String content;
	private final boolean fromUser;
	private final boolean isQuiz;

	public Message(int chatId, String content, boolean fromUser, boolean isQuiz) {
		this.chatId = chatId;
		this.content = content;
		this.fromUser = fromUser;
		this.isQuiz = isQuiz;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChatId() {
		return chatId;
	}

	public String getContent() {
		return content;
	}

	public boolean getFromUser() {
		return fromUser;
	}

	public boolean getIsQuiz() {
		return isQuiz;
	}
}
