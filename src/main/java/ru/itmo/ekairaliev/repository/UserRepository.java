package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User insert(String login, String passwordHash);
}
