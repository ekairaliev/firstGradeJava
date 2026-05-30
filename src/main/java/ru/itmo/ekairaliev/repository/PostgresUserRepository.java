package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.db.JdbcConnectionFactory;
import ru.itmo.ekairaliev.model.User;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class PostgresUserRepository implements UserRepository {
    private final JdbcConnectionFactory connectionFactory;

    public PostgresUserRepository(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, login, password_hash FROM users ORDER BY id";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new ValidationException("Ошибка чтения пользователей из БД: " + e.getMessage());
        }
    }

    @Override
    public User insert(String login, String passwordHash) {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, login);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getLong(1), login, passwordHash);
                }
            }
            throw new ValidationException("Ошибка БД: не удалось получить id пользователя");
        } catch (SQLException e) {
            throw new ValidationException("Ошибка сохранения пользователя в БД: " + e.getMessage());
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash")
        );
    }
}
