package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {
    public void createUser(User user) throws SQLException;

    public void updateUser(User user) throws SQLException;

    public void deleteUser(User user) throws SQLException;

    public User getUser(int id) throws SQLException;

    public User getUser(String username) throws SQLException;

    public List<User> getAllUsers() throws SQLException;
}
