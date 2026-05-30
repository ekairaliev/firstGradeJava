package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.User;
import ru.itmo.ekairaliev.repository.UserRepository;
import ru.itmo.ekairaliev.validation.TextRules;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AuthService {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        replaceAll(userRepository.findAll());
    }

    public User register(String login, String password) {
        login = normalizeLogin(login);
        validatePassword(password);
        if (findByLogin(login).isPresent()) {
            throw new ValidationException("Ошибка: пользователь с логином '" + login + "' уже существует");
        }

        User user = userRepository.insert(login, hashPassword(password));
        users.put(user.getId(), user);
        currentUser = user;
        return user;
    }

    public User login(String login, String password) {
        login = normalizeLogin(login);
        validatePassword(password);

        User user = findByLogin(login)
                .orElseThrow(() -> new ValidationException("Ошибка: неверный логин или пароль"));
        if (!user.getPasswordHash().equals(hashPassword(password))) {
            throw new ValidationException("Ошибка: неверный логин или пароль");
        }

        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new ValidationException("Ошибка: сначала выполните login");
        }
        return currentUser;
    }

    public long requireCurrentUserId() {
        return requireCurrentUser().getId();
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public String currentLoginOrGuest() {
        return currentUser == null ? "guest" : currentUser.getLogin();
    }

    public List<User> getAll() {
        return users.values().stream()
                .sorted(Comparator.comparingLong(User::getId))
                .toList();
    }

    private void replaceAll(List<User> loadedUsers) {
        users.clear();
        for (User user : loadedUsers.stream().sorted(Comparator.comparingLong(User::getId)).toList()) {
            users.put(user.getId(), user);
        }
    }

    private Optional<User> findByLogin(String login) {
        return users.values().stream()
                .filter(user -> user.getLogin().equals(login))
                .findFirst();
    }

    private String normalizeLogin(String login) {
        login = TextRules.notBlank(login, "login").trim();
        TextRules.maxLen(login, 64, "login");
        TextRules.loginLikeValue(login, "login");
        return login;
    }

    private void validatePassword(String password) {
        password = TextRules.notBlank(password, "password");
        TextRules.maxLen(password, 128, "password");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }
}
