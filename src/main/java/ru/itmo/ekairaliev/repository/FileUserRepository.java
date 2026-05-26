package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.model.User;
import ru.itmo.ekairaliev.storage.UserFileStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FileUserRepository implements UserRepository {
    private final UserFileStorage userFileStorage;
    private final Path usersPath;
    private final List<User> users;

    public FileUserRepository(UserFileStorage userFileStorage, Path usersPath) {
        this.userFileStorage = userFileStorage;
        this.usersPath = usersPath;
        this.users = new ArrayList<>(userFileStorage.load(usersPath));
    }

    @Override
    public List<User> findAll() {
        return users.stream()
                .sorted(Comparator.comparingLong(User::getId))
                .toList();
    }

    @Override
    public User insert(String login, String passwordHash) {
        long nextId = users.stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
        User user = new User(nextId, login, passwordHash);
        users.add(user);
        userFileStorage.save(usersPath, findAll());
        return user;
    }
}
