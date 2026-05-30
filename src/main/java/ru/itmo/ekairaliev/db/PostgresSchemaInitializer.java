package ru.itmo.ekairaliev.db;

import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class PostgresSchemaInitializer {
    private final JdbcConnectionFactory connectionFactory;

    public PostgresSchemaInitializer(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initialize() {
        try (Connection connection = connectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id BIGSERIAL PRIMARY KEY,
                        login VARCHAR(64) NOT NULL UNIQUE,
                        password_hash VARCHAR(64) NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS samples (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(64) NOT NULL,
                        hold_status VARCHAR(16) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,
                        owner_id BIGINT REFERENCES users(id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS seals (
                        id BIGSERIAL PRIMARY KEY,
                        sample_id BIGINT NOT NULL REFERENCES samples(id) ON DELETE RESTRICT,
                        seal_number VARCHAR(64) NOT NULL,
                        status VARCHAR(16) NOT NULL,
                        owner_username VARCHAR(64) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,
                        owner_id BIGINT REFERENCES users(id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS custody_events (
                        id BIGSERIAL PRIMARY KEY,
                        sample_id BIGINT NOT NULL REFERENCES samples(id) ON DELETE RESTRICT,
                        from_user VARCHAR(64) NOT NULL,
                        to_user VARCHAR(64) NOT NULL,
                        location VARCHAR(64) NOT NULL,
                        comment VARCHAR(128),
                        transferred_at TIMESTAMPTZ NOT NULL,
                        owner_username VARCHAR(64) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,
                        owner_id BIGINT REFERENCES users(id)
                    )
                    """);
        } catch (SQLException e) {
            throw new ValidationException("Ошибка подготовки схемы БД: " + e.getMessage());
        }
    }
}
