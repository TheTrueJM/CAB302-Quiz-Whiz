package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IChatDAO {
	public void createChat(Chat chat);

	public void updateChat(Chat chat);

	public void deleteChat(Chat chat);

	public Chat getChat(int id);

	public List<Chat> getAllUserChats(int userId);
}
