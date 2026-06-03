package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CommandException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class CommandOptions {
    private final Map<String, String> values;

    private CommandOptions(Map<String, String> values) {
        this.values = values;
    }

    static CommandOptions parse(List<String> args, Set<String> allowedOptions) {
        Map<String, String> parsedValues = new LinkedHashMap<>();
        for (int i = 0; i < args.size(); i += 2) {
            String rawOption = args.get(i);
            if (!rawOption.startsWith("--")) {
                throw new CommandException("Ошибка: ожидалась опция, получено '" + rawOption + "'");
            }

            String option = rawOption.substring(2);
            if (!allowedOptions.contains(option)) {
                throw new CommandException("Ошибка: неизвестная опция '" + rawOption + "'");
            }
            if (i + 1 >= args.size() || args.get(i + 1).startsWith("--")) {
                throw new CommandException("Ошибка: для опции '" + rawOption + "' нужно указать значение");
            }
            if (parsedValues.put(option, args.get(i + 1)) != null) {
                throw new CommandException("Ошибка: опция '" + rawOption + "' указана несколько раз");
            }
        }
        return new CommandOptions(parsedValues);
    }

    Optional<String> value(String option) {
        return Optional.ofNullable(values.get(option));
    }

    long positiveLong(String option, String fieldName) {
        String rawValue = values.get(option);
        try {
            long value = Long.parseLong(rawValue);
            if (value <= 0) {
                throw new CommandException("Ошибка: " + fieldName + " должен быть > 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new CommandException("Ошибка: " + fieldName + " должен быть числом");
        }
    }

    int positiveInt(String option, String fieldName) {
        String rawValue = values.get(option);
        try {
            int value = Integer.parseInt(rawValue);
            if (value <= 0) {
                throw new CommandException("Ошибка: " + fieldName + " должен быть > 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new CommandException("Ошибка: " + fieldName + " должен быть числом");
        }
    }

    <E extends Enum<E>> E enumValue(String option, Class<E> enumType) {
        String rawValue = values.get(option);
        try {
            return Enum.valueOf(enumType, rawValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String allowedValues = Arrays.stream(enumType.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new CommandException("Ошибка: опция --" + option + " должна быть одной из: " + allowedValues);
        }
    }

    static boolean containsIgnoreCase(String value, String fragment) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT));
    }
}
