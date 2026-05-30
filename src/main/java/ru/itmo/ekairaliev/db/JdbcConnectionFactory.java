package ru.itmo.ekairaliev.db;

import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class JdbcConnectionFactory {
    private final DatabaseConfig config;

    public JdbcConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(config.url(), config.user(), config.password());
        } catch (SQLException e) {
            throw new ValidationException("Ошибка подключения к БД: " + e.getMessage());
        }
    }
}
