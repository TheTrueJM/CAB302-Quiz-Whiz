package ai.tutor.cab302exceptionalhandlers.model;

import java.util.List;

public interface IUserDAO {
	public void createUser(User user);

	public void updateUser(User user);

	public void deleteUser(User user);

	public User getUser(int id);

	public List<User> getAllUsers();
}
