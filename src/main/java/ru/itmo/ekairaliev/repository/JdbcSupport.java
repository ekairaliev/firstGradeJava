package ru.itmo.ekairaliev.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

final class JdbcSupport {
    private JdbcSupport() {
    }

    static Instant instant(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getTimestamp(column).toInstant();
    }

    static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    static long ownerId(ResultSet resultSet) throws SQLException {
        long ownerId = resultSet.getLong("owner_id");
        return resultSet.wasNull() ? 0 : ownerId;
    }

    static void setOwnerId(PreparedStatement statement, int index, long ownerId) throws SQLException {
        if (ownerId <= 0) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, ownerId);
        }
    }
}
