package ai.tutor.cab302exceptionalhandlers.model;

/**
 * Represents a single message in a Chat
 *
 * @author Joshua M.
 * @see {@link ai.tutor.cab302exceptionalhandlers.model.Chat}
 */
public class Message {
    private int id;
    private final int chatId;
    private final String content;
    private final boolean fromUser;
    private final boolean isQuiz;


    /**
     * Constructor for a Message object
     *
     * @param chatId The chat id that this message belongs to, must be greater than 0
     * @param content The content of this message, must not be null or empty
     * @param fromUser Whether this message is from the user or the AI, true if from user, false if from AI
     * @param isQuiz Whether this message is a quiz question or not
     * @throws IllegalArgumentException
     */
    public Message(int chatId, String content, boolean fromUser, boolean isQuiz) throws IllegalArgumentException {
        if (chatId < 1) { throw new IllegalArgumentException("Invalid Chat Id: Must be greater than 1"); }
        this.chatId = chatId;

        if (content == null || content.isEmpty()) { throw new IllegalArgumentException("Invalid Message Content: Cannot be empty"); }
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
