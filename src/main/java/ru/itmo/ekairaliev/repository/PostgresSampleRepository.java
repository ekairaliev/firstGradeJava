package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.db.JdbcConnectionFactory;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PostgresSampleRepository implements SampleRepository {
    private final JdbcConnectionFactory connectionFactory;

    public PostgresSampleRepository(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Sample> findAll() {
        String sql = "SELECT id, name, hold_status, created_at, updated_at, owner_id FROM samples ORDER BY id";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Sample> samples = new ArrayList<>();
            while (resultSet.next()) {
                samples.add(mapSample(resultSet));
            }
            return samples;
        } catch (SQLException e) {
            throw new ValidationException("Ошибка чтения sample из БД: " + e.getMessage());
        }
    }

    @Override
    public Sample insert(String name, SampleHoldStatus holdStatus, Instant createdAt, Instant updatedAt, long ownerId) {
        String sql = "INSERT INTO samples (name, hold_status, created_at, updated_at, owner_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, holdStatus.name());
            statement.setTimestamp(3, JdbcSupport.timestamp(createdAt));
            statement.setTimestamp(4, JdbcSupport.timestamp(updatedAt));
            JdbcSupport.setOwnerId(statement, 5, ownerId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Sample(keys.getLong(1), name, holdStatus, createdAt, updatedAt, ownerId);
                }
            }
            throw new ValidationException("Ошибка БД: не удалось получить id sample");
        } catch (SQLException e) {
            throw new ValidationException("Ошибка сохранения sample в БД: " + e.getMessage());
        }
    }

    @Override
    public void update(Sample sample) {
        String sql = "UPDATE samples SET name = ?, hold_status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sample.getName());
            statement.setString(2, sample.getHoldStatus().name());
            statement.setTimestamp(3, JdbcSupport.timestamp(sample.getUpdatedAt()));
            statement.setLong(4, sample.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка обновления sample в БД: " + e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM samples WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Ошибка удаления sample из БД: " + e.getMessage());
        }
    }

    private Sample mapSample(ResultSet resultSet) throws SQLException {
        return new Sample(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                SampleHoldStatus.valueOf(resultSet.getString("hold_status")),
                JdbcSupport.instant(resultSet, "created_at"),
                JdbcSupport.instant(resultSet, "updated_at"),
                JdbcSupport.ownerId(resultSet)
        );
    }
}
