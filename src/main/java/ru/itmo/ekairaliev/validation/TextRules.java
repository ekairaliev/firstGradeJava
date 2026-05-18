package ru.itmo.ekairaliev.validation;

public final class TextRules {
    private TextRules() {
    }

    public static String norm(String value) {
        return value == null ? null : value.trim();
    }

    public static String notBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Ошибка: " + field + " не может быть пустым");
        }
        return value.trim();
    }

    public static String maxLen(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new ValidationException("Ошибка: " + field + " слишком длинное (макс. " + max + ")");
        }
        return value;
    }

    public static String loginLikeValue(String value, String field) {
        if (value != null && !value.matches("[A-Za-zА-Яа-яЁё0-9_\\- ]+")) {
            throw new ValidationException("Ошибка: " + field + " содержит недопустимые символы");
        }
        return value;
    }
}
