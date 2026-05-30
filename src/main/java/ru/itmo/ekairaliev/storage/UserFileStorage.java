package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.model.User;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class UserFileStorage {
    private static final int COLUMN_COUNT = 3;
    private static final String HEADER = "id,login,passwordHash";

    public List<User> load(Path path) {
        if (Files.notExists(path)) {
            return List.of();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException("Ошибка загрузки пользователей: " + e.getMessage());
        }

        if (lines.isEmpty()) {
            return List.of();
        }
        if (!HEADER.equals(lines.get(0))) {
            throw new ValidationException("Ошибка загрузки пользователей: некорректный заголовок CSV");
        }

        List<User> users = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            List<String> columns = CsvSupport.parseLine(lines.get(i), i + 1, COLUMN_COUNT);
            users.add(new User(
                    CsvSupport.parseLong(columns.get(0), "id", i + 1),
                    columns.get(1),
                    columns.get(2)
            ));
        }
        return users;
    }

    public void save(Path path, List<User> users) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (User user : users) {
            lines.add(CsvSupport.toLine(List.of(
                    Long.toString(user.getId()),
                    user.getLogin(),
                    user.getPasswordHash()
            )));
        }

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException("Ошибка сохранения пользователей: " + e.getMessage());
        }
    }
}
