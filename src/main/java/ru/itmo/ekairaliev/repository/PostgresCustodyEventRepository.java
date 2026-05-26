package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.db.JdbcConnectionFactory;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PostgresCustodyEventRepository implements CustodyEventRepository {
    private final JdbcConnectionFactory connectionFactory;

    public PostgresCustodyEventRepository(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<CustodyEvent> findAll() {
        String sql = """
                SELECT id, sample_id, from_user, to_user, location, comment, transferred_at,
                       owner_username, created_at, updated_at, owner_id
                FROM custody_events
                ORDER BY transferred_at, id
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<CustodyEvent> events = new ArrayList<>();
            while (resultSet.next()) {
                events.add(mapEvent(resultSet));
            }
            return events;
        } catch (SQLException e) {
            throw new ValidationException("Ошибка чтения custody_event из БД: " + e.getMessage());
        }
    }

    @Override
    public CustodyEvent insert(long sampleId, String fromUser, String toUser, String location, String comment,
                               Instant transferredAt, String ownerUsername, Instant createdAt, Instant updatedAt,
                               long ownerId) {
        String sql = """
                INSERT INTO custody_events
                    (sample_id, from_user, to_user, location, comment, transferred_at,
                     owner_username, created_at, updated_at, owner_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, sampleId);
            statement.setString(2, fromUser);
            statement.setString(3, toUser);
            statement.setString(4, location);
            statement.setString(5, comment);
            statement.setTimestamp(6, JdbcSupport.timestamp(transferredAt));
            statement.setString(7, ownerUsername);
            statement.setTimestamp(8, JdbcSupport.timestamp(createdAt));
            statement.setTimestamp(9, JdbcSupport.timestamp(updatedAt));
            JdbcSupport.setOwnerId(statement, 10, ownerId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new CustodyEvent(
                            keys.getLong(1),
                            sampleId,
                            fromUser,
                            toUser,
                            location,
                            comment,
                            transferredAt,
                            ownerUsername,
                            createdAt,
                            updatedAt,
                            ownerId
                    );
                }
            }
            throw new ValidationException("Ошибка БД: не удалось получить id custody_event");
        } catch (SQLException e) {
            throw new ValidationException("Ошибка сохранения custody_event в БД: " + e.getMessage());
        }
    }

    @Override
    public void update(CustodyEvent event) {
        String sql = """
                UPDATE custody_events
                SET from_user = ?, to_user = ?, location = ?, comment = ?, updated_at = ?
                WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.getFromUser());
            statement.setString(2, event.getToUser());
            statement.setString(3, event.getLocation());
            statement.setString(4, event.getComment());
            statement.setTimestamp(5, JdbcSupport.timestamp(event.getUpdatedAt()));
            statement.setLong(6, event.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка обновления custody_event в БД: " + e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM custody_events WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка удаления custody_event из БД: " + e.getMessage());
        }
    }

    private CustodyEvent mapEvent(ResultSet resultSet) throws SQLException {
        return new CustodyEvent(
                resultSet.getLong("id"),
                resultSet.getLong("sample_id"),
                resultSet.getString("from_user"),
                resultSet.getString("to_user"),
                resultSet.getString("location"),
                resultSet.getString("comment"),
                JdbcSupport.instant(resultSet, "transferred_at"),
                resultSet.getString("owner_username"),
                JdbcSupport.instant(resultSet, "created_at"),
                JdbcSupport.instant(resultSet, "updated_at"),
                JdbcSupport.ownerId(resultSet)
        );
    }
}
