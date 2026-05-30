package ru.itmo.ekairaliev.db;

import java.util.Optional;

public record DatabaseConfig(String url, String user, String password) {
    public static Optional<DatabaseConfig> fromEnvironment() {
        String url = System.getenv("EKA_DB_URL");
        String user = System.getenv("EKA_DB_USER");
        String password = System.getenv("EKA_DB_PASSWORD");

        if (isBlank(url)) {
            return Optional.empty();
        }
        return Optional.of(new DatabaseConfig(
                url.trim(),
                user == null ? "" : user.trim(),
                password == null ? "" : password
        ));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
