package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IChatDAO {
    public void createChat(Chat chat) throws SQLException;

    public void updateChat(Chat chat) throws SQLException;

    public void updateChatName(Chat chat) throws SQLException;

    public void deleteChat(Chat chat);

    public Chat getChat(int id);

    public List<Chat> getAllUserChats(int userId) throws SQLException;
}
