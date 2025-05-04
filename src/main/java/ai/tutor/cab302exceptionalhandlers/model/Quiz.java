package ai.tutor.cab302exceptionalhandlers.model;

public class Quiz {
    private final int messageId;
    private final String name;
    private final String difficulty;

    public Quiz(int messageId, String name, String difficulty) {
        this.messageId = messageId;
        this.name = name;
        this.difficulty = difficulty;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getName() {
        return name;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
