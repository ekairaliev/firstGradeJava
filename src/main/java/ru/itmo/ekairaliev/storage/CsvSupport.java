package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.validation.ValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class CsvSupport {
    private CsvSupport() {
    }

    public static List<String> parseLine(String line, int lineNumber, int columnCount) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(ch);
                }
                continue;
            }

            if (ch == '"') {
                quoted = true;
            } else if (ch == ',') {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (quoted) {
            throw new ValidationException("Ошибка загрузки: незакрытая кавычка в строке " + lineNumber);
        }

        columns.add(current.toString());
        if (columns.size() != columnCount) {
            throw new ValidationException(
                    "Ошибка загрузки: в строке " + lineNumber + " ожидалось " + columnCount + " колонок, получено " + columns.size()
            );
        }
        return columns;
    }

    public static String toLine(List<String> columns) {
        List<String> escaped = new ArrayList<>(columns.size());
        for (String value : columns) {
            escaped.add(escape(value));
        }
        return String.join(",", escaped);
    }

    public static long parseLong(String raw, String field, int lineNumber) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException("Ошибка загрузки: поле " + field + " в строке " + lineNumber + " не является числом");
        }
    }

    public static Instant parseInstant(String raw, String field, int lineNumber) {
        try {
            return Instant.parse(raw);
        } catch (Exception e) {
            throw new ValidationException("Ошибка загрузки: поле " + field + " в строке " + lineNumber + " не является временем ISO-8601");
        }
    }

    public static <E extends Enum<E>> E parseEnum(String raw, Class<E> enumClass, String field, int lineNumber) {
        try {
            return Enum.valueOf(enumClass, raw);
        } catch (Exception e) {
            throw new ValidationException("Ошибка загрузки: поле " + field + " в строке " + lineNumber + " имеет недопустимое значение");
        }
    }

    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private static String escape(String value) {
        String safe = nullToEmpty(value);
        boolean needsQuotes = safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r");
        if (!needsQuotes) {
            return safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
