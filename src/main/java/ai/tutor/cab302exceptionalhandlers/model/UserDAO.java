package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {
	private Connection connection;


	public UserDAO() {
		connection = SQLiteConnection.getInstance();
		createTable();
	}

	private void createTable() {
		try {
			Statement createTable = connection.createStatement();
			createTable.execute(
					"CREATE TABLE IF NOT EXISTS users ("
							+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
							+ "username VARCHAR NOT NULL,"
							+ "password VARCHAR NOT NULL"
							+ ")"
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void createUser(User user) {
		try {
			PreparedStatement createUser = connection.prepareStatement(
					"INSERT INTO users (firstName, lastName) VALUES (?, ?)"
			);
			createUser.setString(1, user.getUsername());
			createUser.setString(2, user.getPassword());
			createUser.executeUpdate();

			// Set the id of the new User
			Statement getKey = connection.createStatement();
			ResultSet generatedKeys = getKey.getGeneratedKeys();
			if (generatedKeys.next()) {
				user.setId(generatedKeys.getInt(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateUser(User user) {
		try {
			PreparedStatement updateUser = connection.prepareStatement(
					"UPDATE users SET username = ?, password = ? WHERE id = ?"
			);
			updateUser.setString(1, user.getUsername());
			updateUser.setString(2, user.getPassword());
			updateUser.setInt(3, user.getId());
			updateUser.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteUser(User user) {
		try {
			PreparedStatement deleteUser = connection.prepareStatement(
					"DELETE FROM users WHERE id = ?"
			);
			deleteUser.setInt(1, user.getId());
			deleteUser.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public User getUser(int id) {
		try {
			PreparedStatement readUser = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
			readUser.setInt(1, id);
			ResultSet resultSet = readUser.executeQuery();
			if (resultSet.next()) {
				String username = resultSet.getString("firstName");
				String password = resultSet.getString("lastName");
				User user = new User(username, password);
				user.setId(id);
				return user;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		try {
			Statement readUsers = connection.createStatement();
			ResultSet resultSet = readUsers.executeQuery("SELECT * FROM users");
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String username = resultSet.getString("username");
				String password = resultSet.getString("password");
				User user = new User(username, password);
				user.setId(id);
				users.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users;
	}
}
