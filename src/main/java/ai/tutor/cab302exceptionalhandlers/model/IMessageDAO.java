package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IMessageDAO {
    public void createMessage(Message message);

    public List<Message> getAllChatMessages(int chatId);
}
