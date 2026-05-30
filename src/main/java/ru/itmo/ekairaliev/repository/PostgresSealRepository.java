package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.db.JdbcConnectionFactory;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PostgresSealRepository implements SealRepository {
    private final JdbcConnectionFactory connectionFactory;

    public PostgresSealRepository(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Seal> findAll() {
        String sql = """
                SELECT id, sample_id, seal_number, status, owner_username, created_at, updated_at, owner_id
                FROM seals
                ORDER BY id
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Seal> seals = new ArrayList<>();
            while (resultSet.next()) {
                seals.add(mapSeal(resultSet));
            }
            return seals;
        } catch (SQLException e) {
            throw new ValidationException("Ошибка чтения seal из БД: " + e.getMessage());
        }
    }

    @Override
    public Seal insert(long sampleId, SealStatus status, String sealNumber, String ownerUsername,
                       Instant createdAt, Instant updatedAt, long ownerId) {
        String sql = """
                INSERT INTO seals (sample_id, seal_number, status, owner_username, created_at, updated_at, owner_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, sampleId);
            statement.setString(2, sealNumber);
            statement.setString(3, status.name());
            statement.setString(4, ownerUsername);
            statement.setTimestamp(5, JdbcSupport.timestamp(createdAt));
            statement.setTimestamp(6, JdbcSupport.timestamp(updatedAt));
            JdbcSupport.setOwnerId(statement, 7, ownerId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Seal(keys.getLong(1), sampleId, status, sealNumber, ownerUsername, createdAt, updatedAt, ownerId);
                }
            }
            throw new ValidationException("Ошибка БД: не удалось получить id seal");
        } catch (SQLException e) {
            throw new ValidationException("Ошибка сохранения seal в БД: " + e.getMessage());
        }
    }

    @Override
    public void update(Seal seal) {
        String sql = "UPDATE seals SET seal_number = ?, status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seal.getSealNumber());
            statement.setString(2, seal.getStatus().name());
            statement.setTimestamp(3, JdbcSupport.timestamp(seal.getUpdatedAt()));
            statement.setLong(4, seal.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка обновления seal в БД: " + e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM seals WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка удаления seal из БД: " + e.getMessage());
        }
    }

    private Seal mapSeal(ResultSet resultSet) throws SQLException {
        return new Seal(
                resultSet.getLong("id"),
                resultSet.getLong("sample_id"),
                SealStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("seal_number"),
                resultSet.getString("owner_username"),
                JdbcSupport.instant(resultSet, "created_at"),
                JdbcSupport.instant(resultSet, "updated_at"),
                JdbcSupport.ownerId(resultSet)
        );
    }
}
