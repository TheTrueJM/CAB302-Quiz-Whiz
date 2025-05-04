package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {
    private final Connection connection;

    public UserDAO(SQLiteConnection sqliteConnection) throws SQLException, RuntimeException {
        connection = sqliteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute("CREATE TABLE IF NOT EXISTS users (" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username VARCHAR UNIQUE NOT NULL," + "password VARCHAR NOT NULL" + ")");
        }
    }

    @Override
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement createUser = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            createUser.setString(1, user.getUsername());
            createUser.setString(2, user.getPasswordHash());
            createUser.executeUpdate();

            try (ResultSet generatedKeys = createUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (PreparedStatement updateUser = connection.prepareStatement(sql)) {
            updateUser.setString(1, user.getUsername());
            updateUser.setString(2, user.getPasswordHash());
            updateUser.setInt(3, user.getId());
            updateUser.executeUpdate();
        }
    }

    @Override
    public void deleteUser(User user) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement deleteUser = connection.prepareStatement(sql)) {
            deleteUser.setInt(1, user.getId());
            deleteUser.executeUpdate();
        }
    }

    @Override
    public User getUser(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement readUser = connection.prepareStatement(sql)) {
            readUser.setInt(1, id);
            ResultSet resultSet = readUser.executeQuery();

            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                User user = new User(username, password);
                user.setId(id);
                return user;
            }
        }
        return null;
    }

    @Override
    public User getUser(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement readUser = connection.prepareStatement(sql)) {
            readUser.setString(1, username);
            ResultSet resultSet = readUser.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String password = resultSet.getString("password");
                User user = new User(username, password);
                user.setId(id);
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Statement readUsers = connection.createStatement()) {
            ResultSet resultSet = readUsers.executeQuery("SELECT * FROM users");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                User user = new User(username, password);
                user.setId(id);
                users.add(user);
            }
        }
        return users;
    }
}
