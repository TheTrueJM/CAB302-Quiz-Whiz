package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IMessageDAO {
    public void createMessage(Message message) throws SQLException;

    public List<Message> getAllChatMessages(int chatId);
}
