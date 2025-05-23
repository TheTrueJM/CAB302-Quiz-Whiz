package ai.tutor.cab302exceptionalhandlers.model;

public class Message {
    private int id;
    private final int chatId;
    private final String content;
    private final boolean fromUser;
    private final boolean isQuiz;


    public Message(int chatId, String content, boolean fromUser, boolean isQuiz) throws IllegalArgumentException {
        if (chatId < 1) { throw new IllegalArgumentException("Invalid Chat Id: Must be greater than 1"); }
        this.chatId = chatId;

        if (content == null || content.isEmpty()) { throw new IllegalArgumentException("Invalid Content: Cannot be empty"); }
        this.content = content;

        this.fromUser = fromUser;
        this.isQuiz = isQuiz;
    }


    public int getId() { return id; }

    public void setId(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Invalid Id: Must be greater than 1");
        }

        this.id = id;
    }

    public int getChatId() { return chatId; }

    public String getContent() { return content; }

    public boolean getFromUser() { return fromUser; }

    public boolean getIsQuiz() { return isQuiz; }
}
